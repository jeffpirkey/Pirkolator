/*******************************************************************************
 * Copyright 2013 Reality Warp Software
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.rws.pirkolator.core.engine;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.rws.utility.common.Preconditions.notNull;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * The class provides group management for subscriptions to provide for competing 
 * consumer architectural pattern in pub/sub. Receiving queues are used to deliver 
 * messages to each ISubscriber instance - these are the queues that each 
 * ISubscriber are listening to for messages.
 *
 * @author jpirkey
 * @since 0.1.0
 */
public class SubscriptionManager {

    static final Logger LOG = notNull (LoggerFactory.getLogger (SubscriptionManager.class));

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    /** Subscription **/
    private final Subscription subscription;

    private final Set<UUID> subscriberIdSet = Sets.newConcurrentHashSet ();

    private final IQueueGroupManager queueGroup;

    /** <p>Deliver messages from queues to subscription groups
     * <br/><b>Key = subscriber {@link UUID}</b> **/
    private final Map<UUID, MessageCarrier> messageCarrierMap = new ConcurrentHashMap<> ();

    /** <p>Future for Consumer for life-cycle management
     * <br/><b>Key = subscriber {@link UUID}</b> **/
    private final Map<UUID, Future<?>> messageCarrierFutureMap = new ConcurrentHashMap<> ();

    /** Registration management **/
    final ReentrantReadWriteLock deliveryLock = new ReentrantReadWriteLock (true);

    private final SystemResourceManager resourceManager;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public SubscriptionManager (final SystemResourceManager resourceManager, final ISubscriber subscriber) {

        super ();

        this.resourceManager = resourceManager;
        checkNotNull (subscriber);

        subscription = new Subscription ();
        subscription.setDescription (subscriber.getSubscription ().getGroupName () + " Group subscription");
        subscription.setFilterSet (subscriber.getSubscription ().getFilterSet ());
        subscription.setGroupName (subscriber.getSubscription ().getGroupName ());

        final MessageQueue queue = init (subscriber);
        queueGroup = new DefaultMessageQueueGroup (queue, subscriber.getSubscription ().getGroupName ());
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public Subscription getSubscription () {

        return subscription;
    }

    public Set<UUID> getSubscriberIdSet () {

        return ImmutableSet.copyOf (subscriberIdSet);
    }

    public IQueueGroupManager getSubscriberReceivingQueueGroupManager () {

        return queueGroup;
    }

    // *************************************************************************
    // ** Subscriber methods
    // *************************************************************************

    public void addSubscriber (final ISubscriber subscriber) {

        deliveryLock.writeLock ().lock ();

        try {
            subscriberIdSet.add (subscriber.getId ());

            // Check for an existing carrier for the subscriber
            MessageCarrier carrier = messageCarrierMap.get (subscriber.getId ());
            if (carrier == null) {
                // Create a message consumer to deliver messages from 
                // subscription group queues to the subscriber
                carrier = new MessageCarrier (subscriber);

                // Add to map
                messageCarrierMap.put (subscriber.getId (), carrier);
                queueGroup.addQueue (carrier.getSubscriberReceivingQueue ());

            } else {
                LOG.info ("Using existing MessageCarrier for subscriber [name={}; id={}]", subscriber.getName (),
                        subscriber.getId ());
            }

            // Now we check if we are already executing
            final Future<?> future = messageCarrierFutureMap.get (subscriber.getId ());
            if (future == null || future.isCancelled ()) {

                // Start the delegate and save the future for later cancellations
                final Future<?> carrierFuture = resourceManager.getCachedThreadExecutor ().submit (carrier);
                messageCarrierFutureMap.put (subscriber.getId (), carrierFuture);
            }
        } finally {
            deliveryLock.writeLock ().unlock ();
        }
    }

    private final MessageQueue init (final ISubscriber subscriber) {

        deliveryLock.writeLock ().lock ();

        try {
            subscriberIdSet.add (subscriber.getId ());

            // Check for an existing carrier for the subscriber
            MessageCarrier carrier = messageCarrierMap.get (subscriber.getId ());
            if (carrier == null) {
                // Create a message consumer to deliver messages from 
                // subscription group queues to the subscriber
                carrier = new MessageCarrier (subscriber);

                // Add to map
                messageCarrierMap.put (subscriber.getId (), carrier);

            } else {
                LOG.info ("Using existing MessageCarrier for subscriber [name={}; id={}]", subscriber.getName (),
                        subscriber.getId ());
            }

            // Now we check if we are already executing
            final Future<?> future = messageCarrierFutureMap.get (subscriber.getId ());
            if (future == null || future.isCancelled ()) {

                // Start the delegate and save the future for later cancellations
                final Future<?> carrierFuture = resourceManager.getCachedThreadExecutor ().submit (carrier);
                messageCarrierFutureMap.put (subscriber.getId (), carrierFuture);
            }

            return carrier.getSubscriberReceivingQueue ();
        } finally {
            deliveryLock.writeLock ().unlock ();
        }
    }

    public void removeSubscriber (final UUID subscriberId) {

        deliveryLock.writeLock ().lock ();

        try {
            subscriberIdSet.remove (subscriberId);
        } finally {
            deliveryLock.writeLock ().unlock ();
        }
    }
}