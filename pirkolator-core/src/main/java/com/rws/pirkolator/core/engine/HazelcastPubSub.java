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

import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nullable;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MapEvent;
import com.rws.pirkolator.core.grid.HazelcastGrid;
import com.rws.pirkolator.core.grid.IGrid;
import com.rws.pirkolator.model.AbstractSystemIdentifiable;
import com.rws.pirkolator.model.Message;
import com.rws.pirkolator.model.SystemInfo;
import com.rws.utility.common.UUIDs;

/**
 * This class provides a default distributed pub/sub using Hazelcast to provide
 * pub/sub between distributed or scaled systems using the SystemInfo.
 * 
 * @author jpirkey
 *
 */
public final class HazelcastPubSub extends AbstractSystemIdentifiable implements IDistributedPubSub {

    final static Logger LOG = notNull (LoggerFactory.getLogger (HazelcastPubSub.class));

    /**
     * Maps hazelcast publisher ids to publications *
     */
    protected static final String PUBLICATION_MAP = "#PublicationMap";

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    /** System Info **/
    @Resource
    @Nullable
    SystemInfo systemInfo;

    /** Hazelcast **/
    private final HazelcastGrid grid;

    @Nullable
    private HazelcastPublicationListener publicationListener;

    /** Registry **/
    @Nullable
    private IDistributedPubSubRegistry registry;

    private final ReentrantReadWriteLock registrationLock = new ReentrantReadWriteLock (true);

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public HazelcastPubSub (final HazelcastGrid grid) {

        super (UUIDs.generateUUID (), "Hazelcast Pub/Sub");

        this.grid = grid;
    }

    // *************************************************************************
    // ** Method properties
    // *************************************************************************

    @Override
    public IGrid getGrid () {

        return grid;
    }

    @Override
    public MessageQueue prepareQueue (final MessageQueue queue) {

        final ITopic<Message> topic = grid.getInstance ().getTopic (queue.getQueueName ());
        return new TopicMessageQueue (checkNotNull (topic), queue.getQueueName (), "topic"
                + queue.getQueueDescriptor ());
    }

    SystemInfo getSystemInfo () {

        return notNull (systemInfo, "The System Info instance is undefined. This indicates"
                + " that Spring has not been initialized correctly. Check the Spring configuration.");
    }

    IDistributedPubSubRegistry getDistributedPubSubRegistry () {

        return notNull (registry, "The Distributed Pub/Sub Registry instance is undefined."
                + " This indicates that Spring has not been initialized correctly. Check the Spring configuration.");
    }

    // *************************************************************************
    // ** Publication methods
    // *************************************************************************

    @Override
    public void preparePublication (final UUID publisherId, final String publisherName, final Publication publication) {

        registrationLock.writeLock ().lock ();

        try {

            GlobalPublication globalPub = getHazelcastPublicationMap ().get (publisherId);
            // Have we already added this publication?
            if (globalPub == null) {
                globalPub = new GlobalPublication (getSystemInfo ().getId (), publisherId, publisherName, publication);
                addHazelcastPublication (globalPub);
            } else {
                LOG.info ("Publisher {} already registered with Hazelcast", publisherId);
            }
        } finally {
            registrationLock.writeLock ().unlock ();
        }
    }

    public boolean containsPublisher (final UUID id) {

        // FIXME jpirkey For some reason the Hazelcast containsKey isn't working as expected
        for (final UUID tmp : getHazelcastPublicationMap ().keySet ()) {
            if (id.equals (tmp)) {
                return true;
            }
        }

        return getHazelcastPublicationMap ().containsKey (id);
    }

    public boolean containsPublication (final String id) {

        // FIXME jpirkey Fix contains check using Hazelcast Predicate
        //final EntryObject e = new PredicateBuilder().getEntryObject();
        //final Predicate<UUID, GlobalPublication> predicate = e.get("publication").get ("id").equal (id);

        for (final GlobalPublication tmp : getHazelcastPublicationMap ().values ()) {
            if (id.equals (tmp.getPublication ().getId ())) {
                return true;
            }
        }

        return false;
    }

    public Publication getPublication (final String id) {

        final GlobalPublication gp = getHazelcastPublicationMap ().get (id);
        checkNotNull (gp);
        return checkNotNull (gp.getPublication ());
    }

    // *************************************************************************
    // ** Life-cycle methods
    // *************************************************************************

    @Override
    public void prepare (final IDistributedPubSubRegistry myRegistry) {

        registry = myRegistry;

        for (final Entry<UUID, GlobalPublication> entry : getHazelcastPublicationMap ().entrySet ()) {

            if (LOG.isDebugEnabled ()) {
                LOG.debug ("Preparing to register {} publication from the Hazelcast Grid...", entry.getValue ()
                        .getPublication ().getLabel ());
            }

            // Only register publications if the Publisher is from another HUB
            if (!getSystemInfo ().getId ().equals (entry.getValue ().getSystemId ())) {
                final UUID tmp = entry.getKey ();
                final GlobalPublication globalPub = entry.getValue ();
                getDistributedPubSubRegistry ().registerDistributedPublisher (getId (), checkNotNull (tmp),
                        globalPub.getPublisherName (), globalPub.getPublication ());

                if (LOG.isInfoEnabled ()) {
                    LOG.info ("Registered {} publication from the Hazelcast Grid", entry.getValue ().getPublication ()
                            .getLabel ());
                }
            } else {
                if (LOG.isInfoEnabled ()) {
                    LOG.debug ("Skipped registration of a Publication locally owned by this Hub on the Hazelcast Grid",
                            entry.getValue ().getPublication ().getLabel ());
                }
            }
        }

        // Add listeners
        publicationListener = new HazelcastPublicationListener ();
        getHazelcastPublicationMap ().addEntryListener (publicationListener, true);
    }

    // *************************************************************************
    // ** Hazelcast utilities
    // *************************************************************************

    void addHazelcastPublication (final GlobalPublication globalPublication) {

        lockHazelcastPublicationMap (globalPublication.getPublisherId ());
        try {
            // Add to the distributed publication map
            // TODO jpirkey Should we update things on a hazelcast registration event?
            if (getHazelcastPublicationMap ().putIfAbsent (globalPublication.getPublisherId (), globalPublication) != null) {
                LOG.warn ("Publication already added to hazelcast pub/sub [publication={}]",
                        globalPublication.toString ());
            }

            if (LOG.isInfoEnabled ()) {
                LOG.info ("Publication added to distributed Hazelcast Publication Map [{}]",
                        globalPublication.toString ());
            }
        } finally {
            unlockHazelcastPublicationMap (globalPublication.getPublisherId ());
        }
    }

    void removeHazelcastPublication (final UUID publisherId) {

        // Remove from Hazelcast
        lockHazelcastPublicationMap (publisherId);
        GlobalPublication removedPublication;
        try {

            final GlobalPublication tmp = getHazelcastPublicationMap ().get (publisherId);
            if (getSystemInfo ().getId ().equals (tmp.getSystemId ())) {
                LOG.warn ("Can't remove publisher {}.  It is not a member of this HUB.", publisherId);
                return;
            }

            removedPublication = getHazelcastPublicationMap ().remove (publisherId);
            if (removedPublication == null) {
                LOG.info ("Publication already removed or not registered with Hazelcast Pub/Sub");
            } else {
                LOG.info ("Removed publication from Hazelcast Pub/Sub [name={}]", removedPublication.getPublication ()
                        .getLabel ());
            }
        } finally {
            unlockHazelcastPublicationMap (publisherId);
        }
    }

    /**
     * Key = Publisher UUID<br/>
     * Value = GlobalPublication
     * 
     * @return {@link IMap}
     */
    IMap<UUID, GlobalPublication> getHazelcastPublicationMap () {

        return grid.getMap (PUBLICATION_MAP);
    }

    void lockHazelcastPublicationMap (final UUID id) {

        grid.lockMapEntry (PUBLICATION_MAP, id);
    }

    void unlockHazelcastPublicationMap (final UUID id) {

        grid.unlockMapEntry (PUBLICATION_MAP, id);
    }

    // *************************************************************************
    // ** Member Classes
    // *************************************************************************

    private class HazelcastPublicationListener implements EntryListener<UUID, GlobalPublication> {

        HazelcastPublicationListener () {

            super ();
        }

        @Override
        public void entryAdded (final @Nullable EntryEvent<UUID, GlobalPublication> event) {

            checkNotNull (event);

            if (LOG.isDebugEnabled ()) {
                LOG.debug ("Registering Publication from Hazelcast {}", event.getValue ());
            }

            // Only register publication if Publisher is from another HUB
            if (!getSystemInfo ().getId ().equals (event.getValue ().getSystemId ())) {
                final UUID tmp = event.getKey ();
                final GlobalPublication globalPub = event.getValue ();
                getDistributedPubSubRegistry ().registerDistributedPublisher (getId (), checkNotNull (tmp),
                        globalPub.getPublisherName (), globalPub.getPublication ());
            } else if (LOG.isDebugEnabled ()) {
                LOG.debug ("Publication is a member of this HUB, so registration is not needed.");
            }
        }

        @Override
        public void entryRemoved (final @Nullable EntryEvent<UUID, GlobalPublication> event) {

            checkNotNull (event);
            final UUID tmp = event.getKey ();
            getDistributedPubSubRegistry ().unregisterDistributedPublisher (checkNotNull (tmp));
        }

        @Override
        public void entryUpdated (final @Nullable EntryEvent<UUID, GlobalPublication> event) {

            checkNotNull (event);

            if (LOG.isDebugEnabled ()) {
                LOG.debug ("Upating Publication from Hazelcast {}", event.getValue ());
            }
            // Only register publication if Publisher is from another HUB
            if (!getSystemInfo ().getId ().equals (event.getValue ().getSystemId ())) {
                final UUID tmp = event.getKey ();
                final GlobalPublication globalPub = event.getValue ();
                getDistributedPubSubRegistry ().registerDistributedPublisher (getId (), checkNotNull (tmp),
                        globalPub.getPublisherName (), globalPub.getPublication ());
            } else if (LOG.isDebugEnabled ()) {
                LOG.debug ("Publication is a member of this HUB, so registration is not needed.");
            }
        }

        @Override
        public void entryEvicted (final @Nullable EntryEvent<UUID, GlobalPublication> event) {

            // Do nothing
        }

        @Override
        public void mapCleared (final @Nullable MapEvent arg0) {

            // TODO Auto-generated method stub

        }

        @Override
        public void mapEvicted (final @Nullable MapEvent arg0) {

            // TODO Auto-generated method stub

        }
    }

}
