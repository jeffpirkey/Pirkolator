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
package com.rws.pirkolator.core.distro;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.rws.utility.common.Preconditions.notNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.rws.pirkolator.core.engine.AbstractSubscriber;
import com.rws.pirkolator.core.engine.Subscription;
import com.rws.pirkolator.core.engine.SystemResourceManager;
import com.rws.pirkolator.core.grid.HazelcastGrid;
import com.rws.pirkolator.model.filter.TypeFilter;
import com.rws.pirkolator.schema.IIdentifiable;
import com.rws.utility.common.UUIDs;

/**
 * This class loads object instances into memory from the 
 * available-objects hazelcast grid for the specific object type.
 * 
 * @author jpirkey
 */
public class DistroManager<T extends IIdentifiable> extends AbstractSubscriber {

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    final Logger LOG = notNull (LoggerFactory.getLogger (getClass ()));

    /** spring **/
    @Resource
    @Nullable
    HazelcastGrid grid;

    @Resource
    @Nullable
    SystemResourceManager resourceManager;

    /** define **/
    final Map<String, T> objectMap;
    final Class<T> objectType;
    final AtomicInteger capacity = new AtomicInteger (100);
    final AtomicInteger size = new AtomicInteger (0);

    /** life-cycle **/
    final AtomicBoolean ready = new AtomicBoolean (false);

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public DistroManager (final int capacity, final Class<T> dType) {

        super (UUIDs.generateUUID (), dType.getName () + " Distributed Manager");

        this.capacity.set (capacity);
        objectType = dType;
        objectMap = new ConcurrentHashMap<> (capacity);
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public final int size () {

        return size.get ();
    }

    public final Class<T> getObjectType () {

        return objectType;
    }

    public final Set<T> getManagedObjectSet () {

        final Collection<? extends T> tmp = objectMap.values ();
        return ImmutableSet.copyOf (checkNotNull (tmp));
    }

    HazelcastGrid getGrid () {

        return notNull (grid, "HazelcastGrid is undefined in the RepoManager."
                + " This is an autowired @Resource and indicates that Spring"
                + " has not been configured or initialized correctly."
                + " Check your Spring configuration to ensure that the HazelcastGrid is included correctly");
    }

    SystemResourceManager getResourceManager () {

        return notNull (resourceManager, "SystemResourceManager is undefined in the DistroManager."
                + " This is an autowired @Resource and indicates that Spring"
                + " has not been configured or initialized correctly."
                + " Check your Spring configuration to ensure that the SsytemResourceManager is included correctly");
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    public final void reset () {

        size.set (0);
        synchronized (objectMap) {
            objectMap.clear ();
        }
        final Map<String, AtomicInteger> distroMap = getGrid ().getMap ("distroRegistry");
        distroMap.put (getId ().toString (), new AtomicInteger (size ()));

        if (LOG.isDebugEnabled ()) {
            LOG.debug ("Reset objects in DistroManager");
        }
    }

    // *************************************************************************
    // ** Life-cycle 
    // *************************************************************************

    @PostConstruct
    private final void postConstruct () {

        getResourceManager ().getSingleThreadExecutor ("distro-manager-command-runner").execute (new CommandRunner ());

        // Put available manager token
        final IMap<String, Class<T>> map = getGrid ().getMap (objectType.getName () + "-available-managers");
        map.put (getId ().toString (), objectType);

        if (size.get () < capacity.get ()) {

            getResourceManager ().getSingleThreadExecutor ("distro-manager-object-loader")
                    .execute (new ObjectLoader ());
        }

        final Map<String, AtomicInteger> distroMap = getGrid ().getMap ("distroRegistry");
        distroMap.put (getId ().toString (), new AtomicInteger (size ()));
    }

    @Override
    public void doConstruct (final Subscription sub) {

        sub.addFilter (new TypeFilter (objectType));
    }

    // *************************************************************************
    // ** Member classes 
    // *************************************************************************

    class CommandRunner implements Runnable {

        @Override
        public void run () {

            final AtomicBoolean running = new AtomicBoolean (true);

            final String commandQueue = "command-" + getId ().toString ();
            final IQueue<String> queue = getGrid ().getQueue (commandQueue);
            while (running.get ()) {

                try {
                    final String cmd = queue.take ();
                    if ("reset".equals (cmd)) {
                        reset ();
                    }
                } catch (final InterruptedException ex) {
                    LOG.warn ("Distributed Manager queue runner interrupted");
                    running.set (false);
                }
            }
        }
    }

    class ObjectLoader implements Runnable {

        @Override
        public void run () {

            final AtomicBoolean loading = new AtomicBoolean (true);

            final String queueName = objectType.getName () + "-available-objects";
            if (LOG.isDebugEnabled ()) {
                LOG.debug ("Loading objects from {} queue", queueName);
            }
            while (loading.get ()) {
                final IQueue<T> queue = getGrid ().getInstance ().getQueue (queueName);
                try {

                    final T newObj = queue.take ();
                    try {
                        notNull (grid).getExclusiveMapLock ("correlateUpdate").lock ();
                        checkNotNull (newObj);

                        if (LOG.isTraceEnabled ()) {
                            LOG.trace ("Loaded {}", newObj);
                        }

                        objectMap.put (newObj.getId (), newObj);
                        size.incrementAndGet ();

                        final Map<String, AtomicInteger> map = getGrid ().getMap ("distroRegistry");
                        map.put (getId ().toString (), new AtomicInteger (size ()));

                        if (size.get () >= capacity.get ()) {
                            loading.set (false);
                        }

                        Thread.yield ();
                    } finally {
                        notNull (grid).getExclusiveMapLock ("correlateUpdate").unlock ();
                    }
                } catch (final InterruptedException ex) {
                    LOG.warn ("Interrupted loading objects from {} queue", queueName);
                    loading.set (false);
                }
            }
        }
    }

}
