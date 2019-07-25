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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.rws.pirkolator.model.AbstractSystemIdentifiable;
import com.rws.utility.common.UUIDs;

/**
 * This class provides an in-memory, local JVM implementation for an {@link IGrid}.
 * 
 * @author jpirkey
 *
 */
public class LocalGrid extends AbstractSystemIdentifiable implements IGrid {

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    /** Lists **/
    Map<String, List<? extends Serializable>> listMap = new ConcurrentHashMap<> ();
    Lock listLock = new ReentrantLock (true);

    /** Queues **/
    Map<String, BlockingQueue<Serializable>> queueMap = new ConcurrentHashMap<> ();
    Lock queueLock = new ReentrantLock (true);

    /** Maps **/
    Map<String, ConcurrentMap<? extends Serializable, ? extends Serializable>> mapMap = new ConcurrentHashMap<> ();
    Lock mapLock = new ReentrantLock (true);

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public LocalGrid () {

        super (UUIDs.generateUUID (), "Local Grid");
    }

    // *************************************************************************
    // ** List methods
    // *************************************************************************

    @SuppressWarnings ("unchecked")
    @Override
    public <T extends Serializable> List<T> getList (final String name) {

        final List<T> list;
        if (!listMap.containsKey (name)) {
            list = new ArrayList<> ();
            listMap.put (name, list);
        } else {
            list = (List<T>) listMap.get (name);
        }

        return checkNotNull (list);
    }

    @Override
    public void destroyList (final String name) {

        listMap.remove (name);
    }

    @Override
    public Lock getExclusiveListLock (final String key) {

        // TODO jpirkey Add locking to lists - need to implement IList interface
        return listLock;
    }

    // *************************************************************************
    // ** Queue methods
    // *************************************************************************

    @SuppressWarnings ("unchecked")
    @Override
    public <T extends Serializable> BlockingQueue<T> getQueue (final String name) {

        if (!queueMap.containsKey (name)) {
            final ArrayBlockingQueue<Serializable> queue = new ArrayBlockingQueue<> (1024, true);
            queueMap.put (name, queue);
        }

        final BlockingQueue<T> queue = (BlockingQueue<T>) queueMap.get (name);
        return checkNotNull (queue);
    }

    @Override
    public void destroyQueue (final String name) {

        queueMap.remove (name);

    }

    @Override
    public Lock getExclusiveQueueLock (final String key) {

        return listLock;
    }

    // *************************************************************************
    // ** Map methods
    // *************************************************************************

    @SuppressWarnings ("unchecked")
    @Override
    public <K extends Serializable, V extends Serializable> ConcurrentMap<K, V> getMap (final String name) {

        ConcurrentMap<K, V> map;
        if (!mapMap.containsKey (name)) {
            map = new ConcurrentHashMap<> ();
            mapMap.put (name, map);
        } else {
            map = (ConcurrentMap<K, V>) mapMap.get (name);
        }

        return checkNotNull (map);
    }

    @Override
    public void destroyMap (final String mapName) {

        mapMap.remove (mapName);

    }

    @Override
    public void lockMapEntry (final String mapName, final Serializable key) {

        // TODO jpirkey Add locking to map entries - need to implement IMap interface

    }

    @Override
    public void unlockMapEntry (final String mapName, final Serializable key) {

        // TODO jpirkey Add locking to map entries - need to implement IMap interface

    }

    @Override
    public Lock getExclusiveMapLock (final String key) {

        // TODO jpirkey Add locking to map - need to implement IMap interface
        return mapLock;
    }
}
