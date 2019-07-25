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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.rws.pirkolator.schema.IIdentifiable;
import com.rws.pirkolator.schema.IMetadata;

/**
 * This class provides a basic implementation of an {@link IIdentifiable} object
 * with related meta-data.
 * 
 * @author jpirkey
 * @since 0.1.0
 */
public class Metadata implements IMetadata {

    // TODO jpirkey : add permissions for changing meta-data

    private static final long serialVersionUID = 1L;

    /** meta-data **/
    private final Map<String, String> map = new ConcurrentHashMap<> ();

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public Metadata () {

        super ();
    }

    public Metadata (final IMetadata metadata) {

        super ();

        map.putAll (metadata.getMap ());
    }

    @JsonCreator
    public Metadata (@JsonProperty ("map") final Map<String, String> metadataMap) {

        super ();
        map.putAll (metadataMap);
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    @Override
    public Map<String, String> getMap () {

        return map;
    }

    public void setMap (final Map<String, String> map) {

        map.clear ();
        map.putAll (map);
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), map);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof Metadata) {
            if (!super.equals (object))
                return false;
            final Metadata that = (Metadata) object;
            return Objects.equal (map, that.map);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("map", map).toString ();
    }
}
