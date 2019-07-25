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

import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.rws.pirkolator.model.AbstractIdentifiable;
import com.rws.utility.common.UUIDs;

public class StatusView extends AbstractIdentifiable {

    private static final long serialVersionUID = 1L;

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    /** define **/
    String componentId;
    String systemId;
    String systemIdentifier;
    String state;
    long timestamp;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public StatusView (final String id, final String componentId, final String systemId, final String systemIdentifier, final String state,
            final long timestamp) {

        super (id);

        this.componentId = componentId;
        this.systemId = systemId;
        this.state = state;
        this.systemIdentifier = systemIdentifier;
        this.timestamp = timestamp;
    }

    public StatusView (final UUID id, final UUID componentId, final UUID systemId, final String systemIdentifier, final String state,
            final long timestamp) {

        super (UUIDs.toString (id));

        this.componentId = UUIDs.toString (componentId);
        this.systemId = UUIDs.toString (systemId);
        this.systemIdentifier = systemIdentifier;
        this.state = state;
        this.timestamp = timestamp;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public final String getComponentId () {

        return componentId;
    }

    public final void setComponentId (final String id) {

        componentId = id;
    }

    public String getSystemId () {

        return systemId;
    }

    public void setSystemId (final String systemId) {

        this.systemId = systemId;
    }

    public String getSystemIdentifier () {

        return systemIdentifier;
    }

    public void setSystemIdentifier (final String value) {

        systemIdentifier = value;
    }

    public String getState () {

        return state;
    }

    public void setState (final String state) {

        this.state = state;
    }

    public long getTimestamp () {

        return timestamp;
    }

    public void setTimestamp (final long timestamp) {

        this.timestamp = timestamp;
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), componentId, systemId, systemIdentifier, state, timestamp);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof StatusView) {
            if (!super.equals (object))
                return false;
            final StatusView that = (StatusView) object;
            return Objects.equal (componentId, that.componentId) && Objects.equal (systemId, that.systemId)
                    && Objects.equal (systemIdentifier, that.systemIdentifier)
                    && Objects.equal (state, that.state) && Objects.equal (timestamp, that.timestamp);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("componentId", componentId)
                .add ("systemId", systemId).add ("systemIdentifier", systemIdentifier).add ("state", state)
                .add ("timestamp", timestamp).toString ();
    }
}
