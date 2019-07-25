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

import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

/**
 * 
 * @author jpirkey
 * @since 1.0.0
 */
public class SystemInfo extends AbstractSystemIdentifiable {

    private final Metadata metadata = new Metadata ();

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public SystemInfo (final UUID id) {

        this (id, "Unnamed Pirkolator");
    }

    public SystemInfo (final UUID id, final String name) {

        super (id, name);
    }

    //*************************************************************************
    //** Member properties 
    //*************************************************************************

    public final Metadata getMetadata () {

        return metadata;
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), metadata);
    }

    @Override
    public boolean equals (@Nullable final Object object) {

        if (object instanceof SystemInfo) {
            if (!super.equals (object))
                return false;
            final SystemInfo that = (SystemInfo) object;
            return Objects.equal (metadata, that.metadata);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("metadata", metadata).toString ();
    }
}
