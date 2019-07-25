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
package com.rws.utility.common.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author jpirkey
 */
public class CollectionUtils {

    /**
    * Retrieve a value from a map.
    *
    * @param <K>   the key type
    * @param <V>   the value type
    * @param map   - The map that contains the value
    * @param value - the value retrieved from the map
    * @return the k
    */
    public static <K, V> K findKeyFromMapByValue (final Map<K, V> map, final V value) {

        for (final Entry<K, V> entry : map.entrySet ()) {
            if (entry.getValue ().equals (value)) {
                return entry.getKey ();
            }
        }

        return null;
    }

    /**
     * Create an arraylist that only contains the specified type extracted from the original
     * collection.
     *
     * @param <T>   the generic type
     * @param pList the pList
     * @param pType the pType
     * @return the list
     */
    public static <T> Collection<T> typedCollection (final Collection<?> pList, final Class<? extends T> pType) {

        final Collection<T> newList = new ArrayList<> ();
        for (final Object element : pList) {
            if (pType.isAssignableFrom (element.getClass ())) {
                newList.add (pType.cast (element));
            }
        }

        return newList;
    }

    /**
     * Create an arraylist that only contains the specified type extracted from the original
     * collection.
     *
     * @param <T>   the generic type
     * @param pList the pList
     * @param pType the pType
     * @return the list
     */
    public static <T> Iterable<T> typedIterable (final Iterable<?> pList, final Class<? extends T> pType) {

        final Collection<T> newList = new ArrayList<> ();
        for (final Object element : pList) {
            if (pType.isAssignableFrom (element.getClass ())) {
                newList.add (pType.cast (element));
            }
        }

        return newList;
    }
    
    /**
     * Create an array list that only contains the specified type extracted from the original
     * collection.
     *
     * @param <T>   the generic type
     * @param pList the pList
     * @param pType the pType
     * @return the list
     */
    public static <T> List<T> typedArrayList (final Collection<?> pList, final Class<? extends T> pType) {

        final List<T> newList = new ArrayList<> ();
        for (final Object element : pList) {
            if (pType.isAssignableFrom (element.getClass ())) {
                newList.add (pType.cast (element));
            }
        }

        return newList;
    }

    /**
     * Create a hash set that only contains elements of the specified type from the original
     * collection.
     *
     * @param <T>   the generic type
     * @param pList the pList
     * @param pType the pType
     * @return the sets the
     */
    public static <T> Set<T> typedHashSet (final Collection<?> pList, final Class<? extends T> pType) {

        final Set<T> newSet = new HashSet<> ();
        for (final Object element : pList) {
            if (pType.isAssignableFrom (element.getClass ())) {
                newSet.add (pType.cast (element));
            }
        }

        return newSet;
    }

}
