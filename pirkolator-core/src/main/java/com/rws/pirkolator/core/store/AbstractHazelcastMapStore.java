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
package com.rws.pirkolator.core.store;

import static com.rws.utility.common.Preconditions.notNull;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.SqlPredicate;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionException;
import com.rws.pirkolator.core.data.access.IRepository;
import com.rws.pirkolator.core.grid.HazelcastGrid;
import com.rws.pirkolator.model.event.ChangeEventType;
import com.rws.pirkolator.schema.IIdentifiable;
import com.rws.utility.common.collections.CollectionUtils;

public abstract class AbstractHazelcastMapStore<T extends IIdentifiable> extends AbstractStore<T> implements
        IMapStore<T> {

    final Logger LOG = notNull (LoggerFactory.getLogger (getClass ()));

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    @Resource
    @Nullable
    private HazelcastGrid grid;

    @Nullable
    private String listenerName;

    private final MapListener listener = new MapListener ();
    private final Map<String, Method> methodMap = new ConcurrentHashMap<> ();

    @Nullable
    private IRepository<T, T, ?> repo;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public AbstractHazelcastMapStore (final Class<T> type) {

        super (type);

        init ();
    }

    public AbstractHazelcastMapStore (final Class<T> type, final IRepository<T, T, ?> repo) {

        super (type);

        this.repo = repo;

        init ();
    }

    private void init () {

        for (final Method method : getType ().getDeclaredMethods ()) {
            if (method.getName ().startsWith ("get")) {
                methodMap.put (method.getName ().substring (3), method);
            }
        }
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    @Override
    public HazelcastGrid getGrid () {

        return notNull (grid, "The Hazelcast Grid in the HazelcastMapViewStore has not been defined."
                + " Check the Spring configuration to make sure things are configured correctly.");
    }

    @Override
    public Lock getExclusiveLock () {

        return getGrid ().getExclusiveMapLock (getMapName ());
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    public final Collection<T> findByPredicate (final Predicate<String, T> predicate) {

        Collection<T> coll = getMap ().values (predicate);
        if (coll == null) {
            coll = Collections.emptySet ();
        }

        return notNull (coll);
    }

    @Override
    public int size () {

        return getMap ().size ();
    }

    @Override
    public Optional<T> findById (final String id) {

        return Optional.fromNullable (getMap ().get (id));
    }

    public Collection<T> findBy (final String param, final String value) {

        Collection<T> list;
        if (value.equals ("*")) {
            list = getMap ().values ();
        } else {
            list = getMap ().values (new SqlPredicate (param + "='" + value + "'"));
        }

        if (list == null) {
            throw new RuntimeException ("Object for " + param + "=" + value + " not found");
        }

        return list;
    }

    @Override
    public Iterable<? extends T> findAll () {

        final IRepository<T, T, ?> tmpRepo = repo;
        if (tmpRepo != null && useRepoForFindAll ()) {
            return tmpRepo.findAll ();
        }

        final Iterable<? extends T> tmp = getMap ().values ();
        if (tmp != null) {
            return tmp;
        }

        return CollectionUtils.emptyCollection ();
    }

    boolean useRepoForFindAll () {

        final IRepository<T, T, ?> tmpRepo = repo;
        if (tmpRepo != null) {

            return tmpRepo.count () > size ();
        }

        return false;
    }

    @Override
    public Iterable<? extends T> findByParameterMap (final Map<String, Object> map) {

        final IRepository<T, T, ?> workingRepo = repo;
        if (workingRepo == null) {

            final StringBuilder query = new StringBuilder ();
            boolean first = true;
            for (final Entry<String, Object> entry : map.entrySet ()) {
                if (first) {
                    query.append (entry.getKey ()).append ("='").append (entry.getValue ()).append ("'");
                    first = false;
                    continue;
                }

                query.append (" AND ").append (entry.getKey ()).append ("='").append (entry.getValue ()).append ("'");
            }

            final Collection<T> coll = getMap ().values (new SqlPredicate (query.toString ()));
            if (coll == null) {
                return CollectionUtils.emptyCollection ();
            }

            return coll;

        }

        // We have a backing repo, so use it for the query
        return workingRepo.findByParameterMap (map);
    }

    @Override
    public Optional<T> put (final T item) {

        return Optional.fromNullable (getMap ().put (item.getId (), item));
    }

    public void put (final T item, final TransactionContext context) {

        getMap (context).put (item.getId (), item);
    }

    @Override
    public Optional<T> remove (final T item) {

        return removeById (item.getId ());
    }

    public Optional<T> remove (final T item, final TransactionContext context) {

        return removeById (item.getId (), context);
    }

    @Override
    public Optional<T> removeById (final String id) {

        return Optional.fromNullable (getMap ().remove (id));
    }

    public Optional<T> removeById (final String id, final TransactionContext context) {

        return Optional.fromNullable (getMap (context).remove (id));
    }

    @Override
    public void removeAll () {

        getMap ().clear ();
    }

    @Override
    public abstract String getMapName ();

    @Override
    public IMap<String, T> getMap () {

        return getGrid ().getMap (getMapName ());
    }

    public TransactionalMap<String, T> getMap (final TransactionContext context) {

        final TransactionalMap<String, T> tmp = context.getMap (getMapName ());
        if (tmp == null) {
            throw new TransactionException ("Unable to create transaction map for " + getMapName ());
        }

        return tmp;
    }

    @Override
    public void reset () {

        getMap ().clear ();
    }

    public boolean useRepo () {

        final IRepository<T, T, ?> tmpRepo = repo;
        if (tmpRepo != null) {
            return tmpRepo.count () > getMap ().size ();
        }

        return false;
    }

    // *************************************************************************
    // ** Life-cycle 
    // *************************************************************************

    @PostConstruct
    private final void postConstruct () {

        final IMap<String, T> map = getGrid ().getInstance ().getMap (getMapName ());
        listenerName = map.addEntryListener (listener, true);
    }

    @PreDestroy
    private final void preDestroy () {

        try {
            final IMap<String, T> map = getGrid ().getInstance ().getMap (getMapName ());
            if (listenerName != null) {
                map.removeEntryListener (listenerName);
            }
        } catch (final HazelcastInstanceNotActiveException ex) {
            LOG.warn ("Not removing map listener from Hazelcast. Hazelcast instance has already been shutdown.");
        }
    }

    // *************************************************************************
    // ** Member classes 
    // *************************************************************************

    class MapListener implements EntryListener<String, T> {

        // *************************************************************************
        // ** Hazelcast 
        // *************************************************************************

        @Override
        public void entryAdded (final @Nullable EntryEvent<String, T> event) {

            if (event != null) {
                final T view = event.getValue ();
                if (view != null) {
                    view.getMetadata ().getMap ().put ("changeType", ChangeEventType.ADD.toString ());
                    postEvent (view);
                } else {
                    LOG.warn ("Unable to publish view 'add' update event received from Hazelcast"
                            + " because view instance received was null.");
                }
            } else {
                LOG.warn ("Null event received from Hazelcast");
            }
        }

        @Override
        public void entryRemoved (final @Nullable EntryEvent<String, T> event) {

            if (event != null) {
                final T view = event.getValue ();
                if (view != null) {
                    view.getMetadata ().getMap ().put ("changeType", ChangeEventType.REMOVE.toString ());
                    postEvent (view);
                } else {
                    LOG.warn ("Unable to publish view 'remove' update event received from Hazelcast"
                            + " because view instance from the event was null.");
                }
            } else {
                LOG.warn ("Null event received from Hazelcast");
            }
        }

        @Override
        public void entryUpdated (final @Nullable EntryEvent<String, T> event) {

            if (event != null) {
                final T view = event.getValue ();
                if (view != null) {
                    view.getMetadata ().getMap ().put ("changeType", ChangeEventType.UPDATE.toString ());
                    postEvent (view);
                } else {
                    LOG.warn ("Unable to publish view 'update' update event received from Hazelcast"
                            + " because view instance from the event was null.");
                }
            } else {
                LOG.warn ("Null event received from Hazelcast");
            }
        }

        @Override
        public void entryEvicted (final @Nullable EntryEvent<String, T> event) {

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
