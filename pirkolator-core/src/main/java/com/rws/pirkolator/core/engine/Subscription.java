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
import com.rws.pirkolator.model.filter.IContentFilter;
import com.rws.pirkolator.model.filter.IFilter;
import com.rws.utility.common.UUIDs;

/**
 * A subscription defines what kinds of Messages a component wants to receive
 * through the use of {@link IFilter} definitions. If a subscription is changed
 * the subscription needs to re-registered with the owning {@link ISubscriber}'s
 * grid.
 *
 * @author pirk
 * @since 1.0.0
 */
public class Subscription extends AbstractLabeledIdentifiable {

    private static final long serialVersionUID = 1L;

    public final static String DEFAULT_DESCRIPTION = "No description";

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    /** Definitions **/
    private String description;
    private String groupName;

    /** Filters **/
    private final Set<IFilter> filterSet = Sets.newConcurrentHashSet ();
    private final Set<IContentFilter> contentFilterSet = Sets.newConcurrentHashSet ();

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public Subscription () {

        this (UUIDs.generateUUID ());
    }

    public Subscription (final String name, final String groupName) {

        this (UUIDs.generateUUID (), name, groupName);
    }

    public Subscription (final UUID id) {

        this (id, "Subscription - " + id.toString (), UUIDs.defaultUUIDAsString ());
    }

    public Subscription (final UUID id, final String name, final String groupName) {

        super (UUIDs.toString (id), name);

        description = DEFAULT_DESCRIPTION;
        this.groupName = groupName;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public Set<IFilter> getFilterSet () {

        return ImmutableSet.copyOf (filterSet);
    }

    public void setFilterSet (final Set<IFilter> newSet) {

        filterSet.clear ();
        filterSet.addAll (newSet);
    }

    public Set<IContentFilter> getContentFilterSet () {

        return ImmutableSet.copyOf (contentFilterSet);
    }

    public void setContentFilterSet (final Set<IContentFilter> newSet) {

        contentFilterSet.clear ();
        contentFilterSet.addAll (newSet);
    }

    public String getDescription () {

        return description;
    }

    public void setDescription (final String description) {

        this.description = description;
    }

    /**
     * @return {@link String} name of the group
     */
    public String getGroupName () {

        return groupName;
    }

    public void setGroupName (final String groupName) {

        this.groupName = groupName;
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    public void addFilter (final IFilter filter) {

        filterSet.add (filter);

        if (filter instanceof IContentFilter) {
            addContentFilter ((IContentFilter) filter);
        }
    }

    public boolean isDefined () {

        return (!filterSet.isEmpty ());
    }

    private void addContentFilter (final IContentFilter filter) {

        contentFilterSet.add (filter);
    }

    public boolean isContentDefined () {

        return (!contentFilterSet.isEmpty ());
    }

    /**
     * Subscription match which will be true if any match
     *
     * @param publication
     * @return
     */
    public boolean match (final Publication publication) {

        for (final IFilter pubFilter : publication.getFilterSet ()) {
            if (pubFilter == null) {
                return false;
            }

            for (final IFilter subFilter : filterSet) {
                if (subFilter.match (pubFilter)) {
                    return true;
                }
            }
        }

        return false;
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), description, groupName, filterSet, contentFilterSet);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof Subscription) {
            if (!super.equals (object))
                return false;
            final Subscription that = (Subscription) object;
            return Objects.equal (description, that.description) && Objects.equal (groupName, that.groupName)
                    && Objects.equal (filterSet, that.filterSet)
                    && Objects.equal (contentFilterSet, that.contentFilterSet);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("description", description)
                .add ("groupName", groupName).add ("filterSet", filterSet).add ("contentFilterSet", contentFilterSet)
                .toString ();
    }

}
