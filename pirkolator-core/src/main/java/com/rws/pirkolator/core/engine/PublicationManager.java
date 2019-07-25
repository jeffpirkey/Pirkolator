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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.rws.utility.common.UUIDs;

/**
 * The PublicationManager handles maintaining the queues that are associated
 * with an individual Publication.  
 *
 * @author jpirkey
 * @since 0.1.0
 */
public class PublicationManager {

    static final Logger LOG = notNull (LoggerFactory.getLogger (PublicationManager.class));

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    /** define **/
    private final UUID publisherId;
    private final String publisherName;
    private final Publication publication;

    private final Set<MessageQueue> distributedQueueSet = Sets.newConcurrentHashSet ();

    private final MessageRouter router;

    /** Registration management **/
    private final ReentrantReadWriteLock changeLock = new ReentrantReadWriteLock (true);

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public PublicationManager (final SystemResourceManager resourceManager, final IPublisher publisher,
            final String systemName) {

        this (resourceManager, publisher.getId (), publisher.getName (), publisher.getPublication (), systemName);
    }

    public PublicationManager (final SystemResourceManager resourceManager, final UUID publisherId,
            final String publisherName, final Publication publication, final String systemName) {

        super ();

        this.publisherId = publisherId;
        this.publisherName = publisherName;
        this.publication = publication;
        router = new MessageRouter (resourceManager, systemName);
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public UUID getPublisherId () {

        return publisherId;
    }

    public String getPublisherName () {

        return publisherName;
    }

    public Publication getPublication () {

        return publication;
    }

    public boolean containsSubscribers () {

        return !(router.subscriberReceivingQueueMap.isEmpty ());
    }

    // *************************************************************************
    // ** Receiving Queue methods
    // *************************************************************************

    public Collection<IQueueGroupManager> getReceivingQueueGroupSet () {

        return router.getReceivingQueueGroupSet ();
    }

    public void addReceivingQueueGroup (final IQueueGroupManager receivingQueueGroup) {

        router.addReceivingQueueGroup (receivingQueueGroup);
    }

    public void removeReceivingQueue (final UUID subscriberId) {

        router.removeReceivingQueue (UUIDs.toString (subscriberId));
    }

    public void removeReceivingQueueCollection (final Collection<UUID> subscriberIdCollection) {

        for (final UUID id : subscriberIdCollection) {
            removeReceivingQueue (checkNotNull (id));
        }
    }

    // *************************************************************************
    // ** Distributed Queue methods 
    // *************************************************************************

    public Set<MessageQueue> getDistributedQueueSet () {

        return distributedQueueSet;
    }

    public void addDistributedQueue (final MessageQueue queue) {

        changeLock.writeLock ().lock ();
        try {
            distributedQueueSet.add (queue);
        } finally {
            changeLock.writeLock ().unlock ();
        }
    }

    public void removeDistributedQueue (final MessageQueue queue) {

        changeLock.writeLock ().lock ();
        try {
            distributedQueueSet.add (queue);
        } finally {
            changeLock.writeLock ().unlock ();
        }
    }

    public void addPollingQueue (final MessageQueue queue) {

        changeLock.writeLock ().lock ();
        try {
            router.addPollingQueue (queue);
        } finally {
            changeLock.writeLock ().unlock ();
        }
    }

    public Set<MessageQueue> getPollingQueueSet () {

        changeLock.readLock ().lock ();
        try {
            return new HashSet<> (router.getPollingQueueSet ());
        } finally {
            changeLock.readLock ().unlock ();
        }
    }
}
