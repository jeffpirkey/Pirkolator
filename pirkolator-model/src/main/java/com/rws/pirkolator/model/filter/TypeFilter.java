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
package com.rws.pirkolator.model.filter;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import com.rws.pirkolator.model.Message;
import com.rws.utility.common.Externalizables;

/**
 * This implementation provides a class type match between filters.  The filter
 * defaults to MatchType.Any.
 * 
 * @author jpirkey
 * @since 1.0.0
 */
public class TypeFilter implements IContentFilter, Externalizable {

    private static final long serialVersionUID = 1L;

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    private final Set<Class<?>> typeSet = Sets.newConcurrentHashSet ();
    private IFilter.MatchType matchType;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public TypeFilter () {

        super ();
        matchType = MatchType.Any;
    }

    public TypeFilter (final Class<?>... types) {

        this ();
        for (final Class<?> type : types) {
            typeSet.add (type);
        }
    }

    // *************************************************************************
    // ** Type methods
    // *************************************************************************

    public boolean containsType (final Class<?> type) {

        final boolean match;
        match = typeSet.contains (type);

        if (!match) {
            for (final Class<?> t : typeSet) {

                if (type.isAssignableFrom (t)) {
                    return true;
                }
            }
        }

        return match;
    }

    public void setTypeSet (final Set<Class<?>> types) {

        typeSet.clear ();
        typeSet.addAll (types);
    }

    public void addType (final Class<?> type) {

        typeSet.add (type);
    }

    public void addTypes (final Class<?>... types) {

        Collections.addAll (typeSet, types);
    }

    public void addTypeFilters (final Set<Class<?>> types) {

        typeSet.addAll (types);
    }

    @Override
    public Set<Class<?>> getTypeSet () {

        return typeSet;
    }

    public boolean hasTypeFilters () {

        return !typeSet.isEmpty ();
    }

    @Override
    public boolean supports (final Message message) {

        for (final Class<?> type : message.getTypes ()) {

            // Exact match for performance
            if (typeSet.contains (type)) {
                return true;
            }

            for (final Class<?> t : typeSet) {

                if (t.isAssignableFrom (type)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean supportsObject (final Object obj, final String[] propertyList) {

        final Class<?> type = obj.getClass ();
        
        // Exact match for speed
        if (typeSet.contains (type)) {
            return true;
        }

        for (final Class<?> t : typeSet) {

            if (t.isAssignableFrom (type)) {
                return true;
            }
        }

        return false;
    }

    // *************************************************************************
    // ** Match methods
    // *************************************************************************

    @Override
    public MatchType getMatchType () {

        return matchType;
    }

    @Override
    public boolean match (final IFilter targetFilter) {

        if (targetFilter instanceof TypeFilter) {
            if (matchType == MatchType.All) {
                return matchAll ((TypeFilter) targetFilter);
            } else if (matchType == MatchType.Exact) {
                return matchExact ((TypeFilter) targetFilter);
            } else {
                return matchAny ((TypeFilter) targetFilter);
            }
        }

        return false;
    }

    private boolean matchAll (final TypeFilter filter) {

        for (final Class<?> type : typeSet) {
            if (type != null && !filter.containsType (type)) {
                return false;
            }
        }

        return true;
    }

    private boolean matchAny (final TypeFilter filter) {

        for (final Class<?> type : typeSet) {
            // Exact class match
            if (type != null && filter.containsType (type)) {
                return true;
            }

            for (final Class<?> targetType : filter.getTypeSet ()) {
                if (type != null && type.isAssignableFrom (targetType)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean matchExact (final TypeFilter filter) {

        for (final Class<?> type : filter.getTypeSet ()) {
            if (type != null && !containsType (type)) {
                return false;
            }
        }

        return true;
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public void readExternal (final @Nullable ObjectInput in) throws IOException, ClassNotFoundException {

        checkNotNull (in);

        matchType = Externalizables.readObjectAs (in, MatchType.class);

        typeSet.clear ();
        final int size = in.readInt ();
        for (int i = 0; i < size; i++) {
            typeSet.add ((Class<?>) in.readObject ());
        }
    }

    @Override
    public void writeExternal (final @Nullable ObjectOutput out) throws IOException {

        checkNotNull (out);

        out.writeObject (matchType);
        out.writeInt (typeSet.size ());
        for (final Class<?> type : typeSet) {
            out.writeObject (type);
        }
    }

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), typeSet, matchType);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof TypeFilter) {
            if (!super.equals (object))
                return false;
            final TypeFilter that = (TypeFilter) object;
            return Objects.equal (typeSet, that.typeSet) && Objects.equal (matchType, that.matchType);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("typeSet", typeSet)
                .add ("matchType", matchType).toString ();
    }

}
