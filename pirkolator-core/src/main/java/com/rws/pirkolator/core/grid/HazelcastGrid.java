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
package com.rws.pirkolator.core.grid;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.rws.utility.common.Preconditions.notNull;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ITopic;
import com.hazelcast.transaction.TransactionContext;
import com.rws.pirkolator.core.engine.listener.IShutdownListener;
import com.rws.pirkolator.core.grid.exception.AcquireLockException;
import com.rws.pirkolator.model.AbstractSystemIdentifiable;
import com.rws.utility.common.UUIDs;

/**
 * This class provides a Data Grid implementation around Hazelcast.
 * 
 * @author jpirkey
 * @since 0.1.0
 */
public class HazelcastGrid extends AbstractSystemIdentifiable implements IGrid, ApplicationContextAware {

    private final static Logger LOG = notNull (Logger.getLogger (HazelcastGrid.class));

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    @Resource
    @Nullable
    private HazelcastInstance hazelcastInstance;
    private final Set<IShutdownListener> shutdown = Sets.newConcurrentHashSet ();
    @Nullable
    private ApplicationContext applicationContext;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public HazelcastGrid () {

        this (UUIDs.generateUUID (), "Hazelcast Grid");
    }

    public HazelcastGrid (final UUID id, final String name) {

        super (id, name);

        // Set property to default hazelcast logging as log4j
        if (System.getProperty ("hazelcast.logging.type") == null) {
            System.setProperty ("hazelcast.logging.type", "log4j");
        }
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public HazelcastInstance getInstance () {

        return notNull (hazelcastInstance, "Hazelcast Instance has not been defined. "
                + "This may occur when trying to access the Hazelcast Grid before it has been initialized.");
    }

    ApplicationContext getApplicationContext () {

        return notNull (applicationContext, "Spring Application Context is undefined. "
                + "This indicates that Spring has not been initialized correctly.");
    }

    @Override
    public void setApplicationContext (final @Nullable ApplicationContext arg0) throws BeansException {

        applicationContext = checkNotNull (arg0);
    }

    public TransactionContext beginTransaction () {
        
        final TransactionContext context = getInstance().newTransactionContext ();
        context.beginTransaction ();
        
        return context;
    }
    
    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    public void registerShutdown (final IShutdownListener sl) {

        shutdown.add (sl);
    }

    // *************************************************************************
    // ** Map methods
    // *************************************************************************

    @Override
    public <K extends Serializable, V extends Serializable> IMap<K, V> getMap (final String mapName) {

        final IMap<K, V> map = getInstance ().getMap (mapName);
        return checkNotNull (map);
    }

    @Override
    public void lockMapEntry (final String mapName, final Serializable key) {

        getInstance ().getMap (mapName).lock (key);
    }

    @Override
    public void unlockMapEntry (final String mapName, final Serializable key) {

        getInstance ().getMap (mapName).unlock (key);
    }

    @Override
    public Lock getExclusiveMapLock (final String key) {

        final Lock lock = getInstance ().getLock (key);
        if (lock != null) {
            return lock;
        }

        throw new AcquireLockException ("Unable to acquire lock on Hazelcast map");
    }

    @Override
    public void destroyMap (final String mapName) {

        // TODO jpirkey - do we need to lock the map first?
        getInstance ().getMap (mapName).destroy ();
    }

    // *************************************************************************
    // ** Queue methods
    // *************************************************************************

    @Override
    public <T extends Serializable> IQueue<T> getQueue (final String name) {

        final IQueue<T> queue = getInstance ().getQueue (name);
        return checkNotNull (queue);
    }

    @Override
    public void destroyQueue (final String name) {

        getInstance ().getQueue (name).destroy ();
    }

    @Override
    public Lock getExclusiveQueueLock (final String key) {

        final Lock lock = getInstance ().getLock (key);
        if (lock != null) {
            return lock;
        }

        throw new AcquireLockException ("Unable to acquire lock on Hazelcast queue");
    }

    // *************************************************************************
    // ** Topic methods
    // *************************************************************************

    public <T extends Serializable> ITopic<T> getTopic (final String name) {

        final ITopic<T> topic = getInstance ().getTopic (name);
        return checkNotNull (topic);
    }

    public void destroyTopic (final String name) {

        getInstance ().getTopic (name).destroy ();
    }

    public Lock getTopicLock (final String key) {

        final Lock lock = getInstance ().getLock (key);
        if (lock != null) {
            return lock;
        }

        throw new AcquireLockException ("Unable to acquire lock on Hazelcast topic");
    }

    // *************************************************************************
    // ** List methods
    // *************************************************************************

    @Override
    public <T extends Serializable> List<T> getList (final String name) {

        final List<T> list = getInstance ().getList (name);
        if (list != null) {
            return list;
        }

        return Lists.newArrayList ();
    }

    @Override
    public void destroyList (final String name) {

        getInstance ().getList (name).destroy ();
    }

    @Override
    public Lock getExclusiveListLock (final String key) {

        final Lock lock = getInstance ().getLock (key);
        if (lock != null) {
            return lock;
        }

        throw new AcquireLockException ("Unable to acquire lock on Hazelcast list");
    }

    // *************************************************************************
    // ** Life-cycle methods
    // *************************************************************************

    @PreDestroy
    public final void preDestroy () {

        if (LOG.isDebugEnabled ()) {
            LOG.info ("HazelcastGrid is shutting down.");
        }

        for (final IShutdownListener sl : shutdown) {
            try {
                sl.shutdown ();
            } catch (final HazelcastInstanceNotActiveException ex) {
                LOG.warn ("Not removing shutdown listener. Hazelcast instance has already been shutdown.");
            }
        }

        if (hazelcastInstance != null) {
            Hazelcast.shutdownAll ();
            hazelcastInstance = null;
        }

        if (LOG.isInfoEnabled ()) {
            LOG.info ("HazelcastGrid is shutdown.");
        }
    }
}
