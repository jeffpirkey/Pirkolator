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

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.rws.pirkolator.core.engine.listener.IShutdownListener;
import com.rws.pirkolator.core.grid.IGrid;
import com.rws.pirkolator.core.grid.LocalGrid;
import com.rws.pirkolator.model.AbstractSystemIdentifiable;
import com.rws.utility.common.Globals;
import com.rws.utility.common.UUIDs;

/**
 * This pub/sub manages the Spring loaded and dynamically allocated subscribers
 * and publishers for this instance of the SystemInfo.  These are the subscribers
 * and publishers defined in the Spring context used by this SystemInfo.
 *
 * @author jpirkey
 * @since 1.0.0
 */
public final class LocalPubSub extends AbstractSystemIdentifiable implements ApplicationContextAware, ILocalPubSub {

    private final static Logger LOG = notNull (LoggerFactory.getLogger (LocalPubSub.class));

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    /** spring **/
    @Nullable
    private ApplicationContext applicationContext;

    /** <p>Local publishers.
     * <br/><b>Key = publisher {@link UUID}</b> **/
    private final Map<UUID, IPublisher> publisherMap = new ConcurrentHashMap<> ();

    /** Subscribers **/
    private final Map<UUID, ISubscriber> subscriberMap = new ConcurrentHashMap<> ();

    /** define **/
    private final LocalGrid grid;

    /** concurrency management **/
    private final ReentrantReadWriteLock registrationLock = new ReentrantReadWriteLock (true);

    /** Hub **/
    @Nullable
    private ILocalPubSubRegistry registry;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public LocalPubSub (final LocalGrid grid) {

        super (UUIDs.generateUUID (), "Local Pub/Sub");

        this.grid = grid;
    }

    // *************************************************************************
    // ** Definition properties
    // *************************************************************************

    @Override
    public IGrid getGrid () {

        return grid;
    }

    ApplicationContext getApplicationContext () {

        return notNull (applicationContext, "Spring Application Context is undefined. "
                + "This indicates that Spring has not been initialized correctly.");
    }

    ILocalPubSubRegistry getPubSubRegistry () {

        return notNull (registry, "The Local Pub/Sub Registry instance is undefined. "
                + "This indicates that Spring has not been initialized correctly." + " Check the Spring configuration.");
    }

    // *************************************************************************
    // ** Spring properties
    // *************************************************************************

    @Override
    public void setApplicationContext (final @Nullable ApplicationContext value) throws BeansException {

        applicationContext = value;
    }

    // *************************************************************************
    // ** Publisher methods
    // *************************************************************************

    public void registerPublisher (final IPublisher publisher) {

        getPubSubRegistry ().registerPublisher (getId (), publisher);
    }

    public void addPublisher (final IPublisher publisher) {

        if (LOG.isDebugEnabled ()) {
            LOG.debug ("Adding publisher to Local Pub/Sub [name={}; id={}]", publisher.getName (), publisher.getId ());
        }

        // TODO jpirkey validate publication
        registrationLock.writeLock ().lock ();
        try {
            // Add publisher to local map
            publisherMap.put (publisher.getId (), publisher);

            // Register with HUB
            final ILocalPubSubRegistry tmp = registry;
            if (tmp != null) {
                tmp.registerPublisher (getId (), publisher);
            }
            
        } finally {
            registrationLock.writeLock ().unlock ();
        }

        if (LOG.isInfoEnabled ()) {
            LOG.info ("Added publisher to Local Pub/Sub [name={}; id={}]", publisher.getName (), publisher.getId ());
        }
    }

    public void removePublisher (final IPublisher publisher) {

        if (publisherMap.containsKey (publisher.getId ())) {

            if (LOG.isDebugEnabled ()) {
                LOG.debug ("Removing publisher from Local Pub/Sub [name={}; id={}]", publisher.getName (),
                        publisher.getId ());
            }

            registrationLock.writeLock ().lock ();
            try {
                // Remove locally
                publisherMap.remove (publisher.getId ());

                // Remove from HUB
                getPubSubRegistry ().unregisterPublisher (publisher);
            } finally {
                registrationLock.writeLock ().unlock ();
            }
        } else {
            LOG.warn ("Attempt to remove publisher that is not a member of the Local Pub/Sub [name={}]",
                    publisher.getName ());
        }

        if (LOG.isInfoEnabled ()) {
            LOG.info ("Unregistered publisher from Local Pub/Sub [name={}; id={}]", publisher.getName (),
                    publisher.getId ());
        }
    }

    public boolean containsPublisher (final UUID id) {

        return publisherMap.containsKey (id);
    }

    public Optional<IPublisher> getPublisher (final UUID id) {

        return Optional.fromNullable (publisherMap.get (id));
    }

    public Collection<IPublisher> getPublishers () {

        final Collection<IPublisher> set = publisherMap.values ();
        checkNotNull (set);
        return ImmutableSet.copyOf (set);
    }

    // *************************************************************************
    // ** Publication methods 
    // *************************************************************************

    public boolean containsPublication (final String id) {

        for (final IPublisher publisher : publisherMap.values ()) {
            if (id.equals (publisher.getPublication ().getId ())) {
                return true;
            }
        }

        return false;
    }

    public Optional<Publication> getPublication (final String id) {

        Publication pub = null;
        for (final IPublisher publisher : publisherMap.values ()) {
            if (id.equals (publisher.getPublication ().getId ())) {
                // TODO jpirkey add duplicate publication check
                pub = publisher.getPublication ();
            }
        }

        return Optional.fromNullable (pub);
    }

    // *************************************************************************
    // ** Subscriber methods
    // *************************************************************************

    public void registerSubscriber (final ISubscriber subscriber) {

        getPubSubRegistry ().registerSubscriber (getId (), subscriber);
    }

    public void addSubscriber (final ISubscriber subscriber) {

        if (LOG.isDebugEnabled ()) {
            LOG.debug ("Adding subscriber to Local Pub/Sub [name={}, id={}]", subscriber.getName (),
                    subscriber.getId ());
        }

        registrationLock.writeLock ().lock ();

        try {
            if (subscriberMap.containsKey (subscriber.getId ())) {
                LOG.error ("Subscriber already exists in the Local Pub/Sub [name={}; id={}]", subscriber.getName (),
                        subscriber.getId ());
                return;
            }

            final Subscription subscription = subscriber.getSubscription ();
            if (subscription.getFilterSet ().isEmpty ()) {
                LOG.warn ("No filters defined for Subscription - skipping registration");
                return;
            }

            // Add local
            subscriberMap.put (subscriber.getId (), subscriber);

            // Register with HUB
            final ILocalPubSubRegistry tmp = registry;
            if (tmp != null) {
                tmp.registerSubscriber (getId (), subscriber);
            }

        } finally {
            registrationLock.writeLock ().unlock ();
        }

        if (LOG.isInfoEnabled ()) {
            LOG.info ("Added subscriber to Local Pub/Sub [name={}; id={}]", subscriber.getName (), subscriber.getId ());
        }
    }

    public boolean containsSubscriber (final ISubscriber subscriber) {

        return containsSubscriber (subscriber.getId ());
    }

    public boolean containsSubscriber (final UUID id) {

        return subscriberMap.containsKey (id);
    }

    public Optional<ISubscriber> getSubscriber (final UUID id) {

        return Optional.fromNullable (subscriberMap.get (id));
    }

    public Collection<ISubscriber> getSubscribers () {

        final Collection<ISubscriber> set = subscriberMap.values ();
        checkNotNull (set);
        return ImmutableSet.copyOf (set);
    }

    public void removeSubscriber (final ISubscriber subscriber) {

        // Check is subscriber owned by this pub/sub
        if (subscriberMap.containsKey (subscriber.getId ())) {

            if (LOG.isDebugEnabled ()) {
                LOG.debug ("Removing subscriber from Local Pub/Sub [name={}; id={}]", subscriber.getName (),
                        subscriber.getId ());
            }

            registrationLock.writeLock ().lock ();
            try {
                // Remove locally
                subscriberMap.remove (subscriber.getId ());

                // Remove from HUB
                getPubSubRegistry ().unregisterSubscriber (subscriber);
            } finally {
                registrationLock.writeLock ().unlock ();
            }

            if (LOG.isInfoEnabled ()) {
                LOG.info ("Removed subscriber from Local Pub/Sub [name={}; id={}]", subscriber.getName (),
                        subscriber.getId ());
            }

        } else {
            if (LOG.isInfoEnabled ()) {
                LOG.info ("Subscriber not a member of the Local Pub/Sub [id={}]", subscriber.getId ());
            }
        }
    }

    // *************************************************************************
    // ** Subscription methods 
    // *************************************************************************

    public boolean containsSubscription (final String id) {

        for (final ISubscriber subscriber : subscriberMap.values ()) {
            if (id.equals (subscriber.getSubscription ().getId ())) {
                return true;
            }
        }

        return false;
    }

    public Optional<Subscription> getSubscription (final String id) {

        Subscription sub = null;
        for (final ISubscriber subscriber : subscriberMap.values ()) {
            if (id.equals (subscriber.getSubscription ().getId ())) {
                // TODO jpirkey add duplicate subscription check
                sub = subscriber.getSubscription ();
            }
        }

        return Optional.fromNullable (sub);
    }

    // *************************************************************************
    // ** Utility methods
    // *************************************************************************

    public String toStatusString () {

        final StringBuilder builder = new StringBuilder (Globals.NEW_LINE);

        builder.append ("Local Pub/Sub ID = ").append (getId ().toString ()).append (Globals.NEW_LINE)
                .append (Globals.NEW_LINE);

        builder.append ("<< Publishers >>").append (Globals.NEW_LINE);
        final Collection<IPublisher> publishers = getPublishers ();
        if (publishers.isEmpty ()) {
            builder.append ("-- No local publishers").append (Globals.NEW_LINE);
        } else {
            for (final IPublisher pub : getPublishers ()) {
                if (pub != null) {
                    builder.append ("-- ").append (pub.getName ());
                    builder.append (" [id=").append (pub.getId ()).append ("]");
                    builder.append (Globals.NEW_LINE);
                }
            }
        }

        builder.append (Globals.NEW_LINE).append ("<< Subscribers >>").append (Globals.NEW_LINE);

        final Collection<ISubscriber> subscribers = getSubscribers ();
        if (subscribers.isEmpty ()) {
            builder.append ("-- No local subscribers").append (Globals.NEW_LINE);
        } else {
            for (final ISubscriber sub : getSubscribers ()) {
                if (sub != null) {
                    builder.append ("-- ").append (sub.getName ());
                    builder.append (" [id = ").append (sub.getId ());
                    builder.append (", group = ").append (sub.getSubscription ().getGroupName ()).append ("]");
                    builder.append (Globals.NEW_LINE);
                }
            }
        }

        final String tmp = builder.toString ();
        return checkNotNull (tmp);
    }

    // *************************************************************************
    // ** Life-cycle
    // *************************************************************************

    @Override
    public void prepare (final ILocalPubSubRegistry myRegistry) {

        registry = myRegistry;

        // Register static publishers - configured in Spring at start-up
        for (final IPublisher publisher : publisherMap.values ()) {
            if (publisher != null) {
                myRegistry.registerPublisher (getId (), publisher);
            } else {
                LOG.warn ("Attempted to add a null Publisher during preparation.");
            }
        }

        // Register static subscribers - configured in Spring at start-up
        for (final ISubscriber subscriber : subscriberMap.values ()) {
            if (subscriber != null) {
                myRegistry.registerSubscriber (getId (), subscriber);
            } else {
                LOG.warn ("Attempted to add a null Subscriber during preparation.");
            }
        }

        if (LOG.isDebugEnabled ()) {
            LOG.info (toStatusString ());
        }
    }

    @Override
    public MessageQueue prepareQueue (final MessageQueue queue) {

        return queue;
    }

    @PostConstruct
    void postConstruct () {

        // Local subscribers and publishers
        for (final ISubscriber bean : getApplicationContext ().getBeansOfType (ISubscriber.class).values ()) {
            try {
                if (bean != null) {
                    addSubscriber (bean);
                } else {
                    LOG.warn ("Attempted to add a null subscriber from the Spring context.");
                }
            } catch (final Exception ex) {
                if (LOG.isDebugEnabled ()) {
                    LOG.warn (ex.getLocalizedMessage (), ex);
                } else {
                    LOG.warn ("Unable to add subscriber - {}", ex.getLocalizedMessage ());
                }
            }
        }

        for (final IPublisher bean : getApplicationContext ().getBeansOfType (IPublisher.class).values ()) {
            try {
                if (bean != null) {
                    addPublisher (bean);
                } else {
                    LOG.warn ("Attempted to add a null publisher from the Spring context.");
                }
            } catch (final Exception ex) {
                if (LOG.isDebugEnabled ()) {
                    LOG.warn (ex.getLocalizedMessage (), ex);
                } else {
                    LOG.warn ("Unable to add publisher - {}", ex.getLocalizedMessage ());
                }
            }
        }
    }

    @PreDestroy
    void preDestroy () {

        for (final IPublisher pub : publisherMap.values ()) {

            if (pub instanceof IShutdownListener) {
                ((IShutdownListener) pub).shutdown ();
            }
        }

        for (final ISubscriber sub : subscriberMap.values ()) {

            // Only subscribers that aren't publishers
            if (!(sub instanceof IPublisher) && sub instanceof IShutdownListener) {
                ((IShutdownListener) sub).shutdown ();
            }
        }
    }
}
