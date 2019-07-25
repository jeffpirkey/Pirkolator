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
package com.rws.pirkolator.core.pubsub;

import static com.rws.utility.common.Preconditions.notNull;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;
import com.rws.pirkolator.core.engine.AbstractPublisher;
import com.rws.pirkolator.core.engine.Publication;
import com.rws.pirkolator.core.grid.HazelcastGrid;
import com.rws.pirkolator.model.event.ChangeEventType;
import com.rws.pirkolator.model.filter.TypeFilter;
import com.rws.pirkolator.schema.IIdentifiable;
import com.rws.utility.common.Preconditions;
import com.rws.utility.common.UUIDs;

/**
 * Abstract class that listens for changes to a store and then publishes the change event
 * 
 * @author jpirkey
 *
 */
public class GridMapChangePublisher extends AbstractPublisher {

    final static Logger LOG = notNull (LoggerFactory.getLogger (GridMapChangePublisher.class));

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    @Resource
    @Nullable
    private HazelcastGrid grid;

    private final Map<String, String> registeredListenerNameMap = Maps.newConcurrentMap ();
    private final Map<String, Class<? extends IIdentifiable>> typeMap = Maps.newConcurrentMap ();

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    protected GridMapChangePublisher (final Map<String, Class<? extends IIdentifiable>> map) {

        super (UUIDs.generateUUID (), "Grid Map Manager");

        typeMap.putAll (map);
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    HazelcastGrid getHazelcastGrid () {

        return Preconditions.notNull (grid);
    }

    // *************************************************************************
    // ** Life-cycle methods 
    // *************************************************************************

    @Override
    public void doConstruct (final Publication pub) {

        for (final Class<? extends IIdentifiable> type : typeMap.values ()) {
            pub.addFilter (new TypeFilter (type));
        }
    }

    @PostConstruct
    public void postConstruct () {

        for (final String mapName : typeMap.keySet ()) {
            final IMap<String, IIdentifiable> map = getHazelcastGrid ().getInstance ().getMap (mapName);
            registeredListenerNameMap.put (mapName, map.addEntryListener (new MapListener (), true));
        }
    }

    @PreDestroy
    public void preDestroy () {

        for (final Entry<String, String> entry : registeredListenerNameMap.entrySet ()) {
            final IMap<String, ?> map = getHazelcastGrid ().getInstance ().getMap (entry.getKey ());
            map.removeEntryListener (entry.getValue ());
        }
    }

    class MapListener implements EntryListener<String, IIdentifiable> {

        // *************************************************************************
        // ** Hazelcast 
        // *************************************************************************

        @Override
        public void entryAdded (final @Nullable EntryEvent<String, IIdentifiable> event) {

            if (event != null) {
                final IIdentifiable data = event.getValue ();
                if (data != null) {
                    data.getMetadata ().getMap ().put ("changeType", ChangeEventType.ADD.toString ());
                    publish (data);
                } else {
                    LOG.warn ("Unable to publish 'ADD' event received from Hazelcast"
                            + " because data received was null.");
                }
            } else {
                LOG.warn ("Null event received from Hazelcast");
            }
        }

        @Override
        public void entryRemoved (final @Nullable EntryEvent<String, IIdentifiable> event) {

            if (event != null) {
                final IIdentifiable data = event.getValue ();
                if (data != null) {
                    data.getMetadata ().getMap ().put ("changeType", ChangeEventType.REMOVE.toString ());
                    publish (data);
                } else {
                    LOG.warn ("Unable to publish 'REMOVE' event received from Hazelcast"
                            + " because data from the event was null.");
                }
            } else {
                LOG.warn ("Null event received from Hazelcast");
            }
        }

        @Override
        public void entryUpdated (final @Nullable EntryEvent<String, IIdentifiable> event) {

            if (event != null) {
                final IIdentifiable data = event.getValue ();
                if (data != null) {
                    data.getMetadata ().getMap ().put ("changeType", ChangeEventType.UPDATE.toString ());
                    publish (data);
                } else {
                    LOG.warn ("Unable to publish 'UPDATE' event received from Hazelcast"
                            + " because data from the event was null.");
                }
            } else {
                LOG.warn ("Null event received from Hazelcast");
            }
        }

        @Override
        public void entryEvicted (final @Nullable EntryEvent<String, IIdentifiable> event) {

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
