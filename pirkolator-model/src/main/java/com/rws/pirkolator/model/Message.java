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
package com.rws.pirkolator.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.rws.pirkolator.schema.IMessage;

/**
 * This final class provides the {@link IMessage} implementation.
 * 
 * @author pirk
 * @since 1.0.0
 */
public final class Message implements IMessage {

    private static final long serialVersionUID = 1L;

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    /**
     * Definitions *
     */
    private final UUID id;
    @Nullable
    private UUID originalId;

    /**
     * Collections *
     */
    private final Map<String, String> headerMap = new ConcurrentHashMap<> ();
    private final List<Serializable> objectList = new LinkedList<> ();
    private final Set<Class<?>> typeSet = new HashSet<> (5);

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public Message () {

        super ();
        final UUID tmp = UUID.randomUUID ();
        checkNotNull (tmp);
        id = tmp;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    @Override
    public UUID getId () {

        return id;
    }

    @Override
    public UUID getOriginalId () {

        UUID tmp = null;
        if (originalId == null) {
            tmp = originalId;
        } else {
            tmp = id;
        }

        return checkNotNull (tmp);
    }

    // *************************************************************************
    // ** Header methods
    // *************************************************************************

    public void addHeader (final String name, final String value) {

        headerMap.put (name, value);
    }

    @Override
    public @Nullable
    String getHeader (final String name) {

        return headerMap.get (name);
    }

    @Override
    public Map<String, String> getHeaderMap () {

        return new HashMap<> (headerMap);
    }

    // *************************************************************************
    // ** Type methods
    // *************************************************************************

    @Override
    public boolean containsType (final Class<?> type) {

        for (final Class<?> c : typeSet) {
            if (type.isAssignableFrom (c)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Set<Class<?>> getTypes () {

        return typeSet;
    }

    // *************************************************************************
    // ** Access methods
    // *************************************************************************

    @Override
    public boolean isEmpty () {

        return objectList.size () <= 0;
    }

    @Override
    public List<Serializable> get () {

        return ImmutableList.copyOf (objectList);
    }

    public boolean isCopy () {

        final String copy = headerMap.get ("copy");
        if (copy != null) {
            final boolean isCopy = Boolean.valueOf (copy);
            return isCopy;
        }

        return false;
    }

    public <T> List<T> get (final Class<T> type) {

        final List<T> newList = new ArrayList<> ();
        for (final Object obj : objectList) {
            if (type.isInstance (obj)) {
                newList.add (type.cast (obj));
            }
        }

        return newList;
    }

    @Override
    public Iterator<Serializable> iterate () {

        final Iterator<Serializable> iterator = objectList.iterator ();
        if (iterator == null) {
            return Iterators.emptyIterator ();
        }
        return Iterators.unmodifiableIterator (iterator);
    }

    @Override
    public <T> Iterator<T> iterate (final Class<T> type) {

        final Iterator<T> iterator = get (type).iterator ();
        if (iterator == null) {
            return Iterators.emptyIterator ();
        }
        return Iterators.unmodifiableIterator (iterator);
    }

    // *************************************************************************
    // ** Manipulation methods
    // *************************************************************************

    public void add (final Serializable object) {

        typeSet.add (object.getClass ());
        objectList.add (object);
    }

    public void add (final Serializable... objects) {

        for (final Serializable s : objects) {
            add (checkNotNull (s));
        }
    }

    public void add (final Collection<? extends Serializable> objects) {

        for (final Serializable s : objects) {
            add (checkNotNull (s));
        }
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), id, originalId, headerMap, objectList, typeSet);
    }

    @Override
    public boolean equals (@Nullable final Object object) {

        if (object instanceof Message) {
            if (!super.equals (object))
                return false;
            final Message that = (Message) object;
            return Objects.equal (id, that.id) && Objects.equal (originalId, that.originalId)
                    && Objects.equal (headerMap, that.headerMap) && Objects.equal (objectList, that.objectList)
                    && Objects.equal (typeSet, that.typeSet);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("id", id)
                .add ("originalId", originalId).add ("headerMap", headerMap).add ("objectList", objectList)
                .add ("typeSet", typeSet).toString ();
    }

}
