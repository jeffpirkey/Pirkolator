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
package com.rws.pirkolator.view.model;

import java.util.Map;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.rws.pirkolator.model.AbstractLabeledIdentifiable;

@XmlRootElement
public class FilterView extends AbstractLabeledIdentifiable {

    private static final long serialVersionUID = 1L;

    private String type;
    private String description;

    private final Map<String, String> propertyMap = Maps.newConcurrentMap ();

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public FilterView (final String id, final String label, final String type, final String description) {

        super (id, label);

        this.type = type;
        this.description = description;
    }

    public FilterView (final String id, final String label, final String type, final String description,
            final Map<String, String> map) {

        super (id, label);

        this.type = type;
        this.description = description;
        propertyMap.putAll (map);
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public String getType () {

        return type;
    }

    public void setType (final String type) {

        this.type = type;
    }

    public String getDescription () {

        return description;
    }

    public void setDescription (final String description) {

        this.description = description;
    }

    public Map<String, String> getPropertyMap () {

        return propertyMap;
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), type, description, propertyMap);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof FilterView) {
            if (!super.equals (object))
                return false;
            final FilterView that = (FilterView) object;
            return Objects.equal (type, that.type) && Objects.equal (description, that.description)
                    && Objects.equal (propertyMap, that.propertyMap);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("type", type)
                .add ("description", description).add ("propertyMap", propertyMap).toString ();
    }

}