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
package com.rws.pirkolator.model;

import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.rws.utility.common.UUIDs;

public class Status {

    private final UUID id = UUIDs.generateUUID ();
    private final UUID componentId;
    private State state;
    private long timestamp;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public Status (final UUID componentId) {

        super ();
        this.componentId = componentId;
        state = State.UNKNOWN;
        timestamp = System.currentTimeMillis ();
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public UUID getId () {

        return id;
    }

    public State getState () {

        return state;
    }

    public UUID getComponentId () {

        return componentId;
    }

    public long getTimestamp () {

        return timestamp;
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    public void initialized () {

        state = State.INITIALIZED;
        timestamp = System.currentTimeMillis ();
    }

    public void starting () {

        state = State.STARTING;
        timestamp = System.currentTimeMillis ();
    }

    public void started () {

        state = State.STARTED;
        timestamp = System.currentTimeMillis ();
    }

    public void stopping () {

        state = State.STOPPING;
        timestamp = System.currentTimeMillis ();
    }

    public void stopped () {

        state = State.STOPPED;
        timestamp = System.currentTimeMillis ();
    }

    public void exception () {

        state = State.EXCEPTION;
        timestamp = System.currentTimeMillis ();
    }

    public void unknown () {

        state = State.UNKNOWN;
        timestamp = System.currentTimeMillis ();
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (id, componentId, state, timestamp);
    }

    @Override
    public boolean equals (@Nullable final Object object) {

        if (object instanceof Status) {
            final Status that = (Status) object;
            return Objects.equal (id, that.id) && Objects.equal (componentId, that.componentId)
                    && Objects.equal (state, that.state) && Objects.equal (timestamp, that.timestamp);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("id", id).add ("componentId", componentId).add ("state", state)
                .add ("timestamp", timestamp).toString ();
    }

    public enum State {
        UNKNOWN, INITIALIZED, STARTING, STARTED, STOPPING, STOPPED, EXCEPTION
    }
}
