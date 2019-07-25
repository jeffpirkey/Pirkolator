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
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.rws.utility.common.Externalizables;

/**
 * The NameFilter provides a {@link String} match between filters.  The filter
 * defaults to MatchType.Any.
 * 
 * @author jpirkey
 * @since 1.0.0
 */
public class NameFilter implements IFilter, Externalizable {

    private IFilter.MatchType matchType;
    private final Set<String> nameSet = Sets.newConcurrentHashSet ();

    public NameFilter () {

        super ();
        matchType = MatchType.Any;
    }

    public NameFilter (final String name) {

        this ();
        addName (name);
    }

    // *************************************************************************
    // ** NameSet methods
    // *************************************************************************

    public Set<String> getNameSet () {

        return ImmutableSet.copyOf (nameSet);
    }

    public void setNameSet (final Set<String> names) {

        nameSet.clear ();
        nameSet.addAll (names);
    }

    public void addName (final String name) {

        nameSet.add (name);
    }

    public void addNameSet (final Set<String> names) {

        nameSet.addAll (names);
    }

    // *************************************************************************
    // ** Match methods
    // *************************************************************************

    @Override
    public MatchType getMatchType () {

        return matchType;
    }

    @Override
    public boolean match (final IFilter filter) {

        if (filter instanceof NameFilter) {
            if (matchType == MatchType.All) {
                return matchAll ((NameFilter) filter);
            } else if (matchType == MatchType.Exact) {
                return matchExact ((NameFilter) filter);
            } else {
                return matchAny ((NameFilter) filter);
            }
        }

        return false;
    }

    boolean matchAll (final NameFilter filter) {

        synchronized (nameSet) {
            for (final String str : nameSet) {
                if (!filter.getNameSet ().contains (str)) {
                    return false;
                }
            }
        }

        return true;
    }

    boolean matchAny (final NameFilter filter) {

        synchronized (nameSet) {
            for (final String str : nameSet) {
                if (filter.getNameSet ().contains (str)) {
                    return true;
                }
            }
        }

        return false;
    }

    boolean matchExact (final NameFilter filter) {

        for (final String str : filter.getNameSet ()) {
            if (!getNameSet ().contains (str)) {
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
        nameSet.clear ();
        final int size = in.readInt ();
        for (int i = 0; i < size; i++) {
            nameSet.add ((String) in.readObject ());
        }
    }

    @Override
    public void writeExternal (final @Nullable ObjectOutput out) throws IOException {

        checkNotNull (out);

        out.writeObject (matchType);
        out.writeInt (nameSet.size ());

        synchronized (nameSet) {
            for (final String str : nameSet) {
                out.writeObject (str);
            }
        }
    }

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), matchType, nameSet);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof NameFilter) {
            if (!super.equals (object))
                return false;
            final NameFilter that = (NameFilter) object;
            return Objects.equal (matchType, that.matchType) && Objects.equal (nameSet, that.nameSet);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("matchType", matchType)
                .add ("nameSet", nameSet).toString ();
    }
}
