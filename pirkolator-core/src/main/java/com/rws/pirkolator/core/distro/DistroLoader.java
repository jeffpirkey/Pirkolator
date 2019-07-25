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

import static com.rws.utility.common.Preconditions.notNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.rws.pirkolator.core.data.access.IDaoChannel;
import com.rws.pirkolator.core.data.engine.Dal;
import com.rws.pirkolator.core.grid.HazelcastGrid;
import com.rws.pirkolator.model.AbstractSystemIdentifiable;
import com.rws.pirkolator.model.DaoSource;
import com.rws.pirkolator.schema.IDaoSource;
import com.rws.utility.common.UUIDs;

/**
 * This class loads managed objects of defined type from a {@link DaoSource} and places the objects into
 * a  '[classname]-available-objects' distributed queue.  This class uses the 
 * user provided registry name to ensure that only one DistroLoader instance per
 * cluster performs the loading. 
 * 
 * @author jpirkey
 * @since 0.1.0
 */
public class DistroLoader<T, ID extends Serializable> extends AbstractSystemIdentifiable {

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    final Logger LOG = notNull (LoggerFactory.getLogger (getClass ()));

    /** Spring **/
    @Resource
    @Nullable
    private HazelcastGrid grid;

    @Resource
    @Nullable
    private Dal dal;

    /** define **/
    @Nullable
    private IDaoChannel<T, ID> daoChannel;
    private boolean enabledOnStart = true;
    private final Class<T> objectType;
    private final Class<ID> idType;
    private final IDaoSource source;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public DistroLoader (final IDaoSource source, final Class<T> objType, final Class<ID> idType) {

        super (UUIDs.generateUUID (), source.getLabel () + " Distributed Loader");

        this.objectType = objType;
        this.idType = idType;
        this.source = source;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public final boolean isEnabledOnStart () {

        return enabledOnStart;
    }

    public final void setEnabledOnStart (final boolean value) {

        enabledOnStart = value;
    }

    public final Class<T> getObjectType () {

        return objectType;
    }

    public final Class<ID> getIdType () {

        return idType;
    }

    Dal getDal () {

        return notNull (dal, "Dal is undefined for {}."
                + " This is an autowired @Resource, so check your Spring configurations", getName ());
    }

    HazelcastGrid getGrid () {

        return notNull (grid, "HazelcastGrid is undefined in the RepoManager."
                + " This is an autowired @Resource and indicates that Spring"
                + " has not been configured or initialized correctly."
                + " Check your Spring configuration to ensure that the HazelcastGrid is included correctly.");
    }

    IDaoChannel<T, ID> getDaoChannel () {

        return notNull (daoChannel, "Dao Channel is undefined for analytic."
                + " This indicates that the analytic has not be properly prepared by Spring.");
    }

    // *************************************************************************
    // ** Life-cycle methods 
    // *************************************************************************

    @PostConstruct
    private final void postConstruct () {

        doPreStartLoad ();

        daoChannel = getDal ().getChannel (source, objectType, idType);

        if (enabledOnStart) {
            load ();
        }

        doPostStartLoad ();
    }

    public final void doPreStartLoad () {

        // By default do nothing
    }

    public final void doPostStartLoad () {

        //By default do nothing
    }

    // *************************************************************************
    // ** Member methods 
    // *************************************************************************

    public final int load () {

        final String queueName = objectType.getName () + "-available-objects";
        if (LOG.isDebugEnabled ()) {
            LOG.debug ("Checking and loading {} object types from {} into available queue",
                    objectType.getSimpleName (), source.getLabel ());
        }

        final IQueue<T> availableQueue = getGrid ().getInstance ().getQueue (queueName);

        final List<T> list = new ArrayList<> ();
        final Iterable<? extends T> iterable = getDaoChannel ().findAll ();
        Iterables.addAll (list, iterable);

        if (list.isEmpty ()) {
            LOG.info ("No objects in {}", source.getLabel ());
        } else {
            for (final T id : list) {
                availableQueue.add (id);
            }

            LOG.info ("{} objects loaded from {} into {} queue", list.size (), source.getLabel (), queueName);
        }
        return list.size ();
    }

    public final int reset () {

        final IMap<String, Class<T>> map = getGrid ().getMap (objectType.getName () + "-available-managers");
        for (final String id : map.keySet ()) {
            final String commandQueue = "command-" + id;
            final IQueue<String> queue = getGrid ().getQueue (commandQueue);
            try {
                queue.put ("reset");
                if (LOG.isDebugEnabled ()) {
                    LOG.debug ("Issued reset command to manager ID {}", id);
                }
            } catch (final InterruptedException ex) {
                LOG.warn ("Interrupted sending reset command");
            }
        }

        return load ();
    }
}