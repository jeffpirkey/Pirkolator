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

import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nullable;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.rws.pirkolator.core.grid.IGrid;
import com.rws.pirkolator.core.grid.LocalGrid;
import com.rws.pirkolator.schema.IIdentifiable;

public abstract class AbstractLocalMapStore<T extends IIdentifiable> extends AbstractStore<T> implements IMapStore<T> {

    final Logger LOG = notNull (LoggerFactory.getLogger (getClass ()));

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    @Resource
    @Nullable
    private LocalGrid grid;

    private final Map<String, Method> methodMap = new ConcurrentHashMap<> ();
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock (true);

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public AbstractLocalMapStore (final Class<T> type) {

        super (type);

        init ();
    }

    private void init () {

        for (final Method method : getType ().getDeclaredMethods ()) {
            if (method.getName ().startsWith ("get")) {
                methodMap.put (Introspector.decapitalize (method.getName ().substring (3)), method);
            }
        }
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    @Override
    public IGrid getGrid () {

        return notNull (grid, "The Local Grid being accessed has not been defined."
                + " Check the Spring configuration.");
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    @Override
    public int size () {

        return getMap ().size ();
    }

    @Override
    public Optional<T> findById (final String id) {

        try {
            getReadWriteLock ().readLock ().lock ();
            return Optional.fromNullable (getMap ().get (id));
        } finally {
            getReadWriteLock ().readLock ().unlock ();
        }
    }

    @Override
    public List<T> findAll () {

        try {
            getReadWriteLock ().readLock ().lock ();
            return new ArrayList<> (getMap ().values ());
        } finally {
            getReadWriteLock ().readLock ().unlock ();
        }
    }

    public List<T> findByParam (final String key, final Object param) {

        try {
            getReadWriteLock ().readLock ().lock ();

            final List<T> list = new ArrayList<> ();

            final Method method = methodMap.get (key);
            if (method != null) {
                for (final T obj : getMap ().values ()) {
                    try {
                        final Object val = method.invoke (obj);
                        if (param.equals (val)) {
                            list.add (obj);
                        }
                    } catch (final IllegalAccessException ex) {
                        LOG.error (ex.getLocalizedMessage (), ex);
                    } catch (final IllegalArgumentException ex) {
                        LOG.error (ex.getLocalizedMessage (), ex);
                    } catch (final InvocationTargetException ex) {
                        LOG.error (ex.getLocalizedMessage (), ex);
                    }
                }
            }

            return list;
        } finally {
            getReadWriteLock ().readLock ().unlock ();
        }
    }

    @Override
    public List<T> findByParameterMap (final Map<String, Object> map) {

        try {
            getReadWriteLock ().readLock ().lock ();

            final List<T> list = new ArrayList<> ();

            for (final T obj : getMap ().values ()) {
                boolean match = true;
                for (final Entry<String, Object> entry : map.entrySet ()) {
                    final Method method = methodMap.get (entry.getKey ());
                    
                    if (method != null) {
                        try {
                            final Object val = method.invoke (obj);
                            if (!entry.getValue ().equals (val)) {
                                match = false;
                                break;
                            }
                        } catch (final IllegalAccessException ex) {
                            LOG.error (ex.getLocalizedMessage (), ex);
                        } catch (final IllegalArgumentException ex) {
                            LOG.error (ex.getLocalizedMessage (), ex);
                        } catch (final InvocationTargetException ex) {
                            LOG.error (ex.getLocalizedMessage (), ex);
                        }
                    }
                }
                
                if (match) {
                    list.add(obj);
                }
            }

            return list;
        } finally {
            getReadWriteLock ().readLock ().unlock ();
        }
    }

    public List<T> findByParamMap (final Map<String, Object> paramMap) {

        try {
            getReadWriteLock ().readLock ().lock ();
            final List<T> list = new ArrayList<> ();

            for (final T obj : getMap ().values ()) {
                boolean valid = true;
                for (final Entry<String, Object> entry : paramMap.entrySet ()) {

                    final Method method = methodMap.get (entry.getKey ());
                    if (method != null) {

                        try {
                            final Object val = method.invoke (obj);
                            if (entry.getValue ().equals (val)) {
                                valid = false;
                                break;
                            }
                        } catch (final IllegalAccessException ex) {
                            LOG.error (ex.getLocalizedMessage (), ex);
                        } catch (final IllegalArgumentException ex) {
                            LOG.error (ex.getLocalizedMessage (), ex);
                        } catch (final InvocationTargetException ex) {
                            LOG.error (ex.getLocalizedMessage (), ex);
                        }
                    }
                }

                if (valid) {
                    list.add (obj);
                }
            }

            return list;
        } finally {
            getReadWriteLock ().readLock ().unlock ();
        }
    }

    @Override
    public Optional<T> put (final T item) {

        try {
            getReadWriteLock ().writeLock ().lock ();
            return Optional.fromNullable (getMap ().put (item.getId (), item));
        } finally {
            getReadWriteLock ().writeLock ().unlock ();
        }
    }

    @Override
    public Optional<T> remove (final T item) {

        return removeById (item.getId ());
    }

    @Override
    public Optional<T> removeById (final String id) {

        try {
            getReadWriteLock ().writeLock ().lock ();
            return Optional.fromNullable (getMap ().remove (id));
        } finally {
            getReadWriteLock ().writeLock ().unlock ();
        }
    }

    @Override
    public void removeAll () {

        try {
            getReadWriteLock ().writeLock ().lock ();
            getMap ().clear ();
        } finally {
            getReadWriteLock ().writeLock ().unlock ();
        }
    }

    @Override
    public Lock getExclusiveLock () {

        return getGrid ().getExclusiveMapLock (getMapName ());
    }

    public ReentrantReadWriteLock getReadWriteLock () {

        return readWriteLock;
    }

    @Override
    public abstract String getMapName ();

    @Override
    public Map<String, T> getMap () {

        final Map<String, T> map = getGrid ().getMap (getMapName ());
        return map;
    }

    @Override
    public void reset () {

        removeAll ();
    }
}
