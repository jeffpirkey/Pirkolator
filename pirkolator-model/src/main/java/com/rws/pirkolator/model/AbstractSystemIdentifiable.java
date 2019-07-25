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

import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.rws.pirkolator.schema.ISystemIdentifiable;
import com.rws.utility.common.UUIDs;

/**
 * This abstraction provides basic implementations for the {@link ISystemIdentifiable}
 * interface.
 * 
 * @author jpirkey
 * @since 0.1.0
 */
public abstract class AbstractSystemIdentifiable implements ISystemIdentifiable {

    public static final UUID UNKNOWN_ID = UUIDs.fromString ("00000000-0000-0000-0000-0000000000000");

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    /** define **/
    private UUID id;
    private String name;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    protected AbstractSystemIdentifiable (final UUID id, final String name) {

        super ();
        this.id = id;
        this.name = name;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    @Override
    public UUID getId () {

        return id;
    }

    public void setId (final UUID id) {

        checkNotNull (id);
        this.id = id;
    }

    @Override
    public String getName () {

        return name;
    }

    public void setName (final String name) {

        checkNotNull (name);
        this.name = name;
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), id, name);
    }

    @Override
    public boolean equals (@Nullable final Object object) {
        
        if (object instanceof AbstractSystemIdentifiable) {
            if (!super.equals (object))
                return false;
            final AbstractSystemIdentifiable that = (AbstractSystemIdentifiable) object;
            return Objects.equal (id, that.id) && Objects.equal (name, that.name);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("id", id).add ("name", name)
                .toString ();
    }

}
