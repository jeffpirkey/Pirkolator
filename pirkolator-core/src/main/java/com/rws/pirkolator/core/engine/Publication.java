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
package com.rws.pirkolator.core.engine;

import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.rws.pirkolator.model.AbstractLabeledIdentifiable;
import com.rws.pirkolator.model.filter.IFilter;
import com.rws.utility.common.UUIDs;

/**
 * This class provides an implementation that defines the {@link IFilter} definitions
 * used by a publisher to describe the data it produces.
 * 
 * @author pirk
 * @since 1.0.0
 */
public class Publication extends AbstractLabeledIdentifiable {

    private static final long serialVersionUID = 1L;

    public final static String DEFAULT_DESCRIPTION = "No description";

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    /** Definition **/
    @Nullable
    private String description;

    /** Filters **/
    private final Set<IFilter> filterSet = Sets.newConcurrentHashSet ();

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public Publication () {

        this (UUIDs.generateUUID ());
    }

    public Publication (final String name) {

        this (UUIDs.generateUUID (), name);
    }

    public Publication (final String name, final IFilter initialFilter) {

        this (UUIDs.generateUUID (), name);
        filterSet.add (initialFilter);
    }

    public Publication (final UUID id) {

        super (UUIDs.toString (id), "Publication - " + id.toString ());
    }

    public Publication (final UUID id, final String name) {

        super (UUIDs.toString (id), name);
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public Set<IFilter> getFilterSet () {

        return ImmutableSet.copyOf (filterSet);
    }

    public void setFilterSet (final Set<IFilter> filterSet) {

        this.filterSet.clear ();
        this.filterSet.addAll (filterSet);
    }

    public String getDescription () {

        final String tmp = description;
        if (tmp == null) {
            return DEFAULT_DESCRIPTION;
        }

        return tmp;
    }
    
    public void setDescription (final String description) {

        this.description = description;
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    public void addFilter (final IFilter filter) {

        synchronized (filterSet) {
            filterSet.add (filter);
        }
    }

    public boolean isDefined () {

        return (!filterSet.isEmpty ());
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), description, filterSet);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof Publication) {
            if (!super.equals (object))
                return false;
            final Publication that = (Publication) object;
            return Objects.equal (description, that.description) && Objects.equal (filterSet, that.filterSet);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("description", description)
                .add ("filterSet", filterSet).toString ();
    }

}
