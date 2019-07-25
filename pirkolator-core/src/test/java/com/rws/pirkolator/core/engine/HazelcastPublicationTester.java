/*******************************************************************************
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

import java.io.Serializable;
import java.util.UUID;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;
import com.rws.pirkolator.core.grid.HazelcastGrid;
import com.rws.pirkolator.model.filter.TypeFilter;
import com.rws.utility.common.UUIDs;

public class HazelcastPublicationTester implements EntryListener<UUID, GlobalPublication> {

    private static Logger LOG = notNull (LoggerFactory.getLogger (HazelcastPublicationTester.class));

    @Nullable
    private GlobalPublication registeredPublication;

    private final UUID publisherId = UUIDs.generateUUID ();
    private final String publisherName = "Publication Tester";
    private final UUID hubId;
    private final Publication originalPublication = new Publication ("Test Publication");
    private final HazelcastGrid grid;

    public HazelcastPublicationTester (final UUID hubId, final HazelcastGrid grid) {

        this.grid = grid;
        this.hubId = hubId;
        final IMap<UUID, GlobalPublication> map = grid.getMap (HazelcastPubSub.PUBLICATION_MAP);
        map.addEntryListener (this, true);
    }

    public UUID getPublisherId () {

        return publisherId;
    }

    public Publication getOriginalPublication () {

        return notNull (originalPublication);
    }

    public GlobalPublication getRegisteredPublication () {

        return notNull (registeredPublication);
    }

    public Publication getPublication () {

        return originalPublication;
    }

    public void addTestPublication () {

        final TypeFilter filter = new TypeFilter ();
        filter.addType (Serializable.class);

        originalPublication.addFilter (filter);

        final GlobalPublication globalPub =
                new GlobalPublication (hubId, publisherId, publisherName, originalPublication);
        final IMap<UUID, GlobalPublication> map = grid.getMap (HazelcastPubSub.PUBLICATION_MAP);
        map.put (globalPub.getPublisherId (), globalPub);

    }

    @Override
    public void entryAdded (final @Nullable EntryEvent<UUID, GlobalPublication> event) {

        checkNotNull (event);

        if (event.getKey ().equals (publisherId)) {
            registeredPublication = event.getValue ();
        }
        LOG.info ("Hazelcast Publication added");
    }

    @Override
    public void entryRemoved (final @Nullable EntryEvent<UUID, GlobalPublication> event) {

        // TODO Auto-generated method stub

    }

    @Override
    public void entryUpdated (final @Nullable EntryEvent<UUID, GlobalPublication> event) {

        checkNotNull (event);

        if (event.getKey ().equals (publisherId)) {
            registeredPublication = event.getValue ();
        }
        LOG.info ("Hazelcast Publication updated");
    }

    @Override
    public void entryEvicted (final @Nullable EntryEvent<UUID, GlobalPublication> event) {

        // TODO Auto-generated method stub

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
