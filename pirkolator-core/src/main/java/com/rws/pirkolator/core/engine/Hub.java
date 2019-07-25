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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.rws.pirkolator.core.engine.channel.AsyncPubSubChannel;
import com.rws.pirkolator.core.engine.exception.PublicationNotFoundException;
import com.rws.pirkolator.core.engine.listener.IShutdownListener;
import com.rws.pirkolator.core.grid.IGrid;
import com.rws.pirkolator.core.registry.StatusRegistry;
import com.rws.pirkolator.core.registry.SystemRegistry;
import com.rws.pirkolator.core.transform.Transformer;
import com.rws.pirkolator.model.Message;
import com.rws.pirkolator.model.SystemInfo;
import com.rws.utility.common.Globals;
import com.rws.utility.common.UUIDs;

/**
 * This final class provides all the wiring for a SystemInfo instance.  During Post-Construct, this class
 * identifies the IPubSub implementations provided in the Spring configuration.  Each IPubSub in turn
 * registers its publishers or subscribers with the hub.  This allows for both local and remote hubs 
 * to wire up during the SystemInfo instances start-up.
 * 
 * @author jpirkey
 * @since 1.0.0
 */
public final class Hub implements ApplicationContextAware, ILocalPubSubRegistry, IDistributedPubSubRegistry {

    public final static String PROP_HUB_IDENTIFIER = "hub-identifier";

    private final static Logger LOG = notNull (LoggerFactory.getLogger (Hub.class));

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    /** Spring **/
    @Nullable
    private ApplicationContext applicationContext;

    @Resource
    @Nullable
    private SystemInfo systemInfo;

    @Resource
    @Nullable
    private SystemReady systemReady;

    @Resource
    @Nullable
    private SystemRegistry systemRegistry;

    @Resource
    @Nullable
    private StatusRegistry statusRegistry;

    @Resource
    @Nullable
    private Transformer transformer;

    @Resource
    @Nullable
    private SystemResourceManager resourceManager;

    /** <p>Map of {@link IPubSub} implementations.
     * <br/><b>Key = pub/sub {@link UUID}</b> **/
    private final Map<UUID, IPubSub> pubSubMap = new ConcurrentHashMap<> ();

    /** <p>Local publishers.
     * <br/><b>Key = publisher {@link UUID}</b> **/
    private final Map<UUID, IPublisher> localPublisherMap = new ConcurrentHashMap<> ();

    /** <p>Map of queues used by an {@link IPublisher} instance to publish messages.
     * <br/><b>Key = publisher {@link UUID}</b> **/
    private final Map<UUID, PublicationManager> publicationManagerMap = new ConcurrentHashMap<> ();

    /** <p>Map of the local {@link MessageDistributor} instances
     * <br/><b>Key = publisher {@link UUID}</b> **/
    private final Map<UUID, MessageDistributor> messageDistributorMap = new ConcurrentHashMap<> ();

    /** <p>Local subscribers.
     * <br/><b>Key = subscriber {@link UUID}</b> **/
    private final Map<UUID, ISubscriber> localSubscriberMap = new ConcurrentHashMap<> ();

    /** <p>Map of managers for each {@link ISubscriber} instance.
     * <br/><b>Key = group name {@link String}</b> **/
    private final Map<String, SubscriptionManager> subscriptionManagerMap = new ConcurrentHashMap<> ();

    /** registration **/
    private final ReentrantReadWriteLock registrationLock = new ReentrantReadWriteLock (true);

    private final IMessageCopier copier;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public Hub (final IMessageCopier copier) {

        super ();

        this.copier = copier;
    }

    // *************************************************************************
    // ** Pub/Sub properties
    // *************************************************************************

    public Set<IGrid> getPubSubGrids () {

        final Set<IGrid> set = Sets.newConcurrentHashSet ();
        for (final IPubSub pubSub : pubSubMap.values ()) {
            set.add (pubSub.getGrid ());
        }
        return set;
    }

    ApplicationContext getApplicationContext () {

        return notNull (applicationContext, "Spring Application Context is undefined."
                + " This indicates that Spring has not been initialized correctly.");
    }

    StatusRegistry getStatusRegistry () {

        return notNull (statusRegistry, "The Status Registry instance is undefined."
                + " This indicates that Spring has not been initialized correctly. Check the Spring configuration.");
    }

    SystemInfo getSystemInfo () {

        return notNull (systemInfo, "The System Info is undefined."
                + " This indicates that Spring has not been initialized correctly. Check the Spring configuration.");
    }

    SystemRegistry getSystemRegistry () {

        return notNull (systemRegistry, "The System Registry is undefined."
                + " This indicates that Spring has not been initialized correctly. Check the Spring configuration.");
    }

    SystemReady getSystemReady () {

        return notNull (systemReady, "The System Ready instance is undefined."
                + " This indicates that Spring has not been initialized correctly. Check the Spring configuration.");
    }

    SystemResourceManager getSystemResourceManager () {

        return notNull (resourceManager, "The System Resource Manager instance is undefined."
                + " This indicates that Spring has not been initialized correctly. Check the Spring configuration.");
    }

    Transformer getTransformer () {

        return notNull (transformer, "The Transformer instance is "
                + " This indicates that Spring has not been initialized correctly. Check the Spring configuration.");
    }

    // *************************************************************************
    // ** Spring properties
    // *************************************************************************

    @Override
    public void setApplicationContext (final @Nullable ApplicationContext value) throws BeansException {

        applicationContext = value;
    }

    // *************************************************************************
    // ** Pub/Sub methods
    // *************************************************************************

    public Set<IPubSub> getPubSubSet () {

        final Collection<IPubSub> tmp = pubSubMap.values ();
        return ImmutableSet.copyOf (checkNotNull (tmp));
    }

    public void addPubSub (final IPubSub bean) {

        try {
            pubSubMap.put (bean.getId (), bean);
        } catch (final Exception ex) {
            LOG.error (ex.getLocalizedMessage (), ex);
        }
    }

    // *************************************************************************
    // ** Subscriber registration methods
    // *************************************************************************

    /**
     * <p>Register an {@link ISubscriber} from a Pub/Sub with this Hub.
     * <p><b><i>** This method write locks the registration token.</i></b> 
     */
    @Override
    public void registerSubscriber (final UUID pubSubId, final ISubscriber subscriber) {

        if (LOG.isDebugEnabled ()) {
            LOG.debug ("HUB is starting registration of subscriber [name={}; id={}]", subscriber.getName (),
                    subscriber.getId ());
        }

        registrationLock.writeLock ().lock ();

        try {

            if (localSubscriberMap.containsKey (subscriber.getId ())) {
                LOG.error ("Subscriber already exists in the HUB [name={}; id={}]", subscriber.getName (),
                        subscriber.getId ());
                return;
            }

            final Subscription subscription = subscriber.getSubscription ();

            if (subscription.getFilterSet ().isEmpty ()) {
                LOG.warn ("No Filters defined for Subscription on subscriber [name={}; id={}]", subscriber.getName (),
                        subscriber.getId ());
            }

            SubscriptionManager subscriptionManager = subscriptionManagerMap.get (subscription.getGroupName ());
            if (subscriptionManager == null) {
                subscriptionManager = new SubscriptionManager (getSystemResourceManager (), subscriber);
                subscriptionManagerMap.put (subscription.getGroupName (), subscriptionManager);
            } else {
                subscriptionManager.addSubscriber (subscriber);
            }

            // Update Subscriptions
            for (final PublicationManager pubMgr : publicationManagerMap.values ()) {
                if (subscription.match (pubMgr.getPublication ())) {
                    pubMgr.addReceivingQueueGroup (subscriptionManager.getSubscriberReceivingQueueGroupManager ());
                    if (LOG.isDebugEnabled ()) {
                        LOG.debug ("Matched publisher {} to subscriber {}", pubMgr.getPublisherName (),
                                subscriber.getName ());
                    }
                }
            }

            localSubscriberMap.put (subscriber.getId (), subscriber);

            getSystemRegistry ().register (subscriber);
            getStatusRegistry ().register (subscriber);

        } finally {
            registrationLock.writeLock ().unlock ();
        }

        if (LOG.isInfoEnabled ()) {
            LOG.info ("HUB completed registration of subscriber [name={}; id={}]", subscriber.getName (),
                    subscriber.getId ());
        }
    }

    /**
     * <p>Unregister an {@link ISubscriber} and associated components from this Hub.
     * <p><b><i>** This method write locks the registration token.</i></b> 
     */
    @Override
    public void unregisterSubscriber (final ISubscriber subscriber) {

        if (LOG.isDebugEnabled ()) {
            LOG.debug ("HUB is starting unregistration of subscriber [name={}; id={}]", subscriber.getName (),
                    subscriber.getId ());
        }

        registrationLock.writeLock ().lock ();

        try {
            if (subscriber instanceof IShutdownListener) {
                ((IShutdownListener) subscriber).shutdown ();
            }

            localSubscriberMap.remove (subscriber.getId ());
            subscriptionManagerMap.remove (subscriber.getId ());

            getSystemRegistry ().unregister (subscriber);
            getStatusRegistry ().unregister (subscriber);
        } finally {
            registrationLock.writeLock ().unlock ();
        }

        if (LOG.isInfoEnabled ()) {
            LOG.info ("HUB completed unregistration of subscriber [name={}; id={}]", subscriber.getName (),
                    subscriber.getId ());
        }
    }

    public boolean containsSubscriber (final UUID id) {

        return localSubscriberMap.containsKey (id);
    }

    // *************************************************************************
    // ** Subscription methods 
    // *************************************************************************

    public boolean containsSubscriptionGroup (final String groupName) {

        for (final SubscriptionManager mgr : subscriptionManagerMap.values ()) {
            if (mgr.getSubscription ().getGroupName ().equals (groupName)) {
                return true;
            }
        }

        return false;
    }

    // *************************************************************************
    // ** Publisher methods
    // *************************************************************************

    /**
     * <p>Register an {@link IPublisher} from a Pub/Sub with this Hub.
     * <p><b><i>** This method write locks the registration token.</i></b> 
     */
    @Override
    public void registerPublisher (final UUID pubSubId, final IPublisher publisher) {

        if (LOG.isTraceEnabled ()) {
            LOG.trace ("HUB is starting registration of publisher [name={}; id={}]", publisher.getName (),
                    publisher.getId ());
        }

        if (publisher.getPublication ().getFilterSet ().isEmpty ()) {
            LOG.warn ("No Filters defined for publisher [name={}; id={}]", publisher.getName (), publisher.getId ());
        }

        if (localPublisherMap.containsKey (publisher.getId ())) {
            LOG.warn ("HUB already contains publisher [name={}; id={}]", publisher.getName (), publisher.getId ());
            return;
        }

        registrationLock.writeLock ().lock ();

        try {
            // Build PublicationManager
            PublicationManager publicationManager = publicationManagerMap.get (publisher.getId ());
            if (publicationManager == null) {
                publicationManager =
                        new PublicationManager (getSystemResourceManager (), publisher, getSystemInfo ().getName ());
                publicationManagerMap.put (publisher.getId (), publicationManager);
            }

            // Add message queue to the publication manager from each Pub/Sub
            for (final IPubSub pubSub : pubSubMap.values ()) {

                // Add queues from the Distributed Pub/Subs for the publisher to use
                if (pubSub instanceof IDistributedPubSub) {
                    ((IDistributedPubSub) pubSub).preparePublication (publisher.getId (), publisher.getName (),
                            publisher.getPublication ());

                    final BlockingQueue<Message> mq = pubSub.getGrid ().getQueue (UUIDs.toString (publisher.getId ()));

                    final String queueDescriptor = "queue-" + pubSub.getName () + "-" + publisher.getName ();
                    final MessageQueue queue =
                            new MessageQueue (mq, UUIDs.toString (publisher.getId ()), queueDescriptor);

                    publicationManager.addDistributedQueue (pubSub.prepareQueue (queue));
                }
            }

            // Update local subscriptions
            for (final SubscriptionManager subMgr : subscriptionManagerMap.values ()) {
                if (subMgr.getSubscription ().match (publicationManager.getPublication ())) {
                    publicationManager.addReceivingQueueGroup (subMgr.getSubscriberReceivingQueueGroupManager ());
                }
            }

            // MessageDistributor for Channel to Publisher
            MessageDistributor messageDistro = messageDistributorMap.get (publisher.getId ());
            if (messageDistro == null) {
                messageDistro =
                        new MessageDistributor (copier, getSystemResourceManager (), publicationManager,
                                getSystemInfo ().getName ());
                messageDistributorMap.put (publisher.getId (), messageDistro);
            }

            // Define Asynchronous Pub/Sub Channel used by the Publisher
            publisher.prepare (new AsyncPubSubChannel (messageDistro, getTransformer ()));

            // Add publisher to map
            localPublisherMap.put (publisher.getId (), publisher);

            // System Register
            getSystemRegistry ().register (publisher);

            // Status Register
            getStatusRegistry ().register (publisher);
        } finally {
            registrationLock.writeLock ().unlock ();
        }

        if (LOG.isInfoEnabled ()) {
            LOG.info ("HUB completed registration of publisher [name={}; id={}]", publisher.getName (),
                    publisher.getId ());
        }
    }

    /**
     * <p>Register a {@link Publication} from a Pub/Sub with this Hub.
     * <p><b><i>** This method write locks the registration token.</i></b> 
     */
    @Override
    public void registerDistributedPublisher (final UUID pubSubId, final UUID publisherId, final String publisherName,
            final Publication publication) {

        registrationLock.writeLock ().lock ();

        try {
            final IPubSub pubSub = pubSubMap.get (pubSubId);
            // Build PublicationManager
            PublicationManager publicationManager = publicationManagerMap.get (publisherId);
            if (publicationManager == null) {
                publicationManager =
                        new PublicationManager (getSystemResourceManager (), publisherId, publisherName
                                + " [distributed-proxy]", publication, getSystemInfo ().getName ());
                publicationManagerMap.put (publisherId, publicationManager);
            }

            final BlockingQueue<Message> mq = pubSub.getGrid ().getQueue (UUIDs.toString (publisherId));

            final String queueDescriptor = "queue-" + pubSub.getName () + "-" + publisherId.toString ();
            MessageQueue queue = new MessageQueue (mq, UUIDs.toString (publisherId), queueDescriptor);
            queue = pubSub.prepareQueue (queue);
            publicationManager.addPollingQueue (queue);

            // Update local subscriptions
            if (subscriptionManagerMap.isEmpty ()) {
                if (LOG.isDebugEnabled ()) {
                    LOG.debug ("No subscription managers defined on {}", getSystemInfo ().getName ());
                }
            } else {
                if (LOG.isDebugEnabled ()) {
                    LOG.debug ("Updating subscription managers on {} with distributed publisher {} from {}",
                            getSystemInfo ().getName (), publisherId, pubSub.getName ());
                }
                for (final SubscriptionManager subMgr : subscriptionManagerMap.values ()) {
                    if (subMgr.getSubscription ().match (publicationManager.getPublication ())) {
                        publicationManager.addReceivingQueueGroup (subMgr.getSubscriberReceivingQueueGroupManager ());
                        if (LOG.isDebugEnabled ()) {
                            LOG.debug (
                                    "Added receiving queue group for match on subscription {} for distributed publisher {} from {}",
                                    subMgr.getSubscription ().getLabel (), publisherId, pubSub.getName ());
                        }
                    }
                }
                if (LOG.isDebugEnabled ()) {
                    LOG.debug ("Completed update of subscription managers on {} with distributed publisher {} from {}",
                            getSystemInfo ().getName (), publisherId, pubSub.getName ());
                }
            }

            if (LOG.isInfoEnabled ()) {
                LOG.info ("HUB completed registration of distributed publisher [id={}]", publisherId);
            }

        } finally {
            registrationLock.writeLock ().unlock ();
        }
    }

    @Override
    public void unregisterDistributedPublisher (final UUID publisherId) {

        registrationLock.writeLock ().lock ();

        try {
            final PublicationManager pubMgr = publicationManagerMap.remove (publisherId);
            if (pubMgr != null) {
                for (final SubscriptionManager subMgr : subscriptionManagerMap.values ()) {
                    if (subMgr.getSubscription ().match (pubMgr.getPublication ())) {
                        pubMgr.removeReceivingQueueCollection (subMgr.getSubscriberIdSet ());
                    }
                }
            }
        } finally {
            registrationLock.writeLock ().unlock ();
        }
    }

    /**
     * <p>Unregister a publisher and associated objects from the Hub.
     * <p><b><i>** This method write locks the registration token.</i></b> 
     */
    @Override
    public void unregisterPublisher (final IPublisher publisher) {

        if (LOG.isTraceEnabled ()) {
            LOG.trace ("HUB is starting unregistration of publisher [name={}; id={}]", publisher.getName (),
                    publisher.getId ());
        }

        registrationLock.writeLock ().lock ();

        try {
            final MessageDistributor messageDistro = messageDistributorMap.remove (publisher.getId ());
            if (messageDistro != null) {
                messageDistro.destroy ();
            }

            localPublisherMap.remove (publisher.getId ());
            publicationManagerMap.remove (publisher.getId ());

            getSystemRegistry ().unregister (publisher);
            getStatusRegistry ().unregister (publisher);

        } finally {
            registrationLock.writeLock ().lock ();
        }

        if (LOG.isInfoEnabled ()) {
            LOG.info ("HUB completed unregistration of publisher [name={}; id={}]", publisher.getName (),
                    publisher.getId ());
        }
    }

    public boolean containsPublisher (final UUID id) {

        return publicationManagerMap.containsKey (id);
    }

    // *************************************************************************
    // ** Publication methods 
    // *************************************************************************

    public boolean containsPublication (final String id) {

        for (final PublicationManager pubMgr : publicationManagerMap.values ()) {
            if (pubMgr.getPublication ().getId ().equals (id)) {
                return true;
            }
        }

        return false;
    }

    public Publication getPublication (final String id) {

        for (final PublicationManager pubMgr : publicationManagerMap.values ()) {
            if (pubMgr.getPublication ().getId ().equals (id)) {
                return pubMgr.getPublication ();
            }
        }

        throw new PublicationNotFoundException ("Publisher with id=" + id + " was not found in the Hub.");
    }

    // *************************************************************************
    // ** Utility methods
    // *************************************************************************

    public String toStatusString () {

        final StringBuilder builder = new StringBuilder (Globals.NEW_LINE);

        builder.append ("System Name = ").append (getSystemInfo ().getName ())
                .append (Globals.NEW_LINE).append (Globals.NEW_LINE);

        builder.append ("System ID = ").append (getSystemInfo ().getId ().toString ()).append (Globals.NEW_LINE)
                .append (Globals.NEW_LINE);

        builder.append ("Pub/Subs").append (Globals.NEW_LINE);
        for (final IPubSub pubSub : pubSubMap.values ()) {
            builder.append ("-- ").append (pubSub.getId ()).append (", type=")
                    .append (pubSub.getClass ().getSimpleName ()).append (Globals.NEW_LINE);
        }

        builder.append (Globals.NEW_LINE);
        builder.append ("<< Publication Managers >>");
        if (publicationManagerMap.isEmpty ()) {
            builder.append (Globals.NEW_LINE).append ("-- No Publication Managers").append (Globals.NEW_LINE);
        } else {
            for (final PublicationManager pubMgr : publicationManagerMap.values ()) {
                builder.append (Globals.DASH_LINE);
                builder.append (pubMgr.getPublisherName ()).append (" [id=").append (pubMgr.getPublisherId ())
                        .append ("]").append (Globals.NEW_LINE);
                builder.append ("Publishing To Queues").append (Globals.NEW_LINE);
                final Set<MessageQueue> publishingQueueSet = pubMgr.getDistributedQueueSet ();
                if (publishingQueueSet.isEmpty ()) {
                    builder.append ("-- No publishing queues").append (Globals.NEW_LINE);
                } else {
                    for (final MessageQueue queue : publishingQueueSet) {
                        builder.append ("-- ").append (queue.getQueueName ()).append (" on ")
                                .append (queue.getQueueDescriptor ()).append (Globals.NEW_LINE);
                    }
                }
                builder.append ("Polling Queues").append (Globals.NEW_LINE);
                final Set<MessageQueue> pollingQueueSet = pubMgr.getPollingQueueSet ();
                if (pollingQueueSet.isEmpty ()) {
                    builder.append ("-- No polling queues").append (Globals.NEW_LINE);
                } else {
                    for (final MessageQueue queue : pollingQueueSet) {
                        builder.append ("-- ").append (queue.getQueueName ()).append (" on ")
                                .append (queue.getQueueDescriptor ()).append (Globals.NEW_LINE);
                    }
                }
                builder.append ("Queues for Local Subscribers").append (Globals.NEW_LINE);
                final Collection<IQueueGroupManager> subscribingQueueGroupSet = pubMgr.getReceivingQueueGroupSet ();
                if (subscribingQueueGroupSet.isEmpty ()) {
                    builder.append ("-- No queues for publisher").append (Globals.NEW_LINE);
                } else {
                    for (final IQueueGroupManager queueGroup : subscribingQueueGroupSet) {
                        for (final MessageQueue queue : queueGroup.getAllQueueSet ()) {
                            builder.append ("-- ").append (queue.getQueueName ()).append (" on ")
                                    .append (queue.getQueueDescriptor ()).append (Globals.NEW_LINE);
                        }
                    }
                }
            }
        }

        builder.append (Globals.NEW_LINE);
        builder.append ("<< Subscription Managers >>").append (Globals.NEW_LINE);
        if (subscriptionManagerMap.isEmpty ()) {
            builder.append ("-- No Subscription Managers").append (Globals.NEW_LINE);
        } else {
            for (final SubscriptionManager subMgr : subscriptionManagerMap.values ()) {
                builder.append ("Group ").append (subMgr.getSubscription ().getGroupName ())
                        .append (" Subscription Manager").append (Globals.NEW_LINE);
                builder.append ("   Receiving Queues").append (Globals.NEW_LINE);

                final Set<MessageQueue> receivingQueueSet =
                        subMgr.getSubscriberReceivingQueueGroupManager ().getAllQueueSet ();
                if (receivingQueueSet.isEmpty ()) {
                    builder.append ("  -- No receiving queues").append (Globals.NEW_LINE);
                } else {
                    for (final MessageQueue queue : receivingQueueSet) {
                        builder.append ("  -- ").append (queue.getQueueName ()).append (" on ")
                                .append (queue.getQueueDescriptor ()).append (Globals.NEW_LINE);
                    }
                }
            }
        }

        final String tmp = builder.toString ();
        return checkNotNull (tmp);
    }

    // *************************************************************************
    // ** Life-cycle
    // *************************************************************************

    @PostConstruct
    void postConstruct () {

        if (applicationContext != null) {

            // First add each pub/sub
            for (final IPubSub pubSub : getApplicationContext ().getBeansOfType (IPubSub.class).values ()) {
                if (pubSub != null) {
                    addPubSub (pubSub);
                } else {
                    LOG.warn ("Attempted to add a null IPubSub form Spring.");
                }
            }

            // Load Spring defined Local Pub/Subs
            for (final ILocalPubSub pubSub : getApplicationContext ().getBeansOfType (ILocalPubSub.class).values ()) {
                pubSub.prepare (this);
            }

            // Load spring defined Distributed Pub/Subs
            for (final IDistributedPubSub pubSub : getApplicationContext ().getBeansOfType (IDistributedPubSub.class)
                    .values ()) {
                pubSub.prepare (this);
            }

        } else {
            getStatusRegistry ().setSystemException ();
        }

        getSystemReady ().readyHub ();
        getStatusRegistry ().setSystemInitialized ();

        if (LOG.isInfoEnabled ()) {
            LOG.info ("{}{} HUB has been readied{}{}{}", Globals.DASH_LINE, getSystemInfo ().getName (),
                    Globals.NEW_LINE, toStatusString (), Globals.DASH_LINE);
        }
    }

    @PreDestroy
    void preDestroy () {

        getStatusRegistry ().setSystemStopping ();

        final Collection<IPublisher> pubRemoveList = new ArrayList<> (localPublisherMap.values ());
        for (final IPublisher publisher : pubRemoveList) {
            if (publisher != null) {
                unregisterPublisher (publisher);
            } else {
                LOG.warn ("Attempted to unregister a null publisher");
            }
        }

        final Collection<ISubscriber> subRemoveList = new ArrayList<> (localSubscriberMap.values ());
        for (final ISubscriber subscriber : subRemoveList) {
            if (subscriber != null) {
                unregisterSubscriber (subscriber);
            } else {
                LOG.warn ("Attempted to unregister a null subscriber");
            }
        }

        if (LOG.isInfoEnabled ()) {
            LOG.info ("{}{} HUB has been shutdown{}", Globals.DASH_LINE, getSystemInfo ().getName (),
                    Globals.DASH_LINE);
        }

        getStatusRegistry ().setSystemStopped ();
    }
}
