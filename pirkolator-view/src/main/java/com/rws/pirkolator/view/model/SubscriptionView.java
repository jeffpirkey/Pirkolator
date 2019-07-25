/*******************************************************************************
 * Copyright 2014 Reality Warp Software
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
package com.rws.pirkolator.view.model;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.rws.pirkolator.model.AbstractLabeledIdentifiable;

public class SubscriptionView extends AbstractLabeledIdentifiable {

    private static final long serialVersionUID = 1L;

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    private String groupName;
    private String description;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public SubscriptionView (final String id, final String label, final String groupName, final String description) {

        super (id, label);

        this.groupName = groupName;
        this.description = description;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public String getGroupName () {

        return groupName;
    }

    public void setGroupName (final String groupName) {

        this.groupName = groupName;
    }

    public String getDescription () {

        return description;
    }

    public void setDescription (final String description) {

        this.description = description;
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), groupName, description);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof SubscriptionView) {
            if (!super.equals (object))
                return false;
            final SubscriptionView that = (SubscriptionView) object;
            return Objects.equal (groupName, that.groupName) && Objects.equal (description, that.description);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("groupName", groupName)
                .add ("description", description).toString ();
    }

}
