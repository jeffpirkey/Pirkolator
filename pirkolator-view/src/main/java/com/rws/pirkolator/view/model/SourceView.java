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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.rws.pirkolator.model.AbstractLabeledIdentifiable;

public class SourceView extends AbstractLabeledIdentifiable {

    private static final long serialVersionUID = 1L;

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    private final Map<String, String> propertyMap = new ConcurrentHashMap<> ();

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public SourceView (final String id, final String label) {

        super (id, label);
    }

    public SourceView (final String id, final String label, final Map<String, String> propertyMap) {

        super (id, label);

        propertyMap.putAll (propertyMap);
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public final Map<String, String> getPropertyMap () {

        return propertyMap;
    }

    public final void setPropertyMap (final Map<String, String> properties) {

        propertyMap.clear ();
        propertyMap.putAll (properties);
    }

    // *************************************************************************
    // ** Member methods 
    // *************************************************************************

    /**
     * 
     * @param key
     * @return {@link String} value of associated key, or null if it was not found.
     */
    public @Nullable
    String getProperty (final String key) {

        return propertyMap.get (key);
    }

    public void addProperty (final String key, final String value) {

        propertyMap.put (key, value);
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), propertyMap);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof SourceView) {
            if (!super.equals (object))
                return false;
            final SourceView that = (SourceView) object;
            return Objects.equal (propertyMap, that.propertyMap);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("propertyMap", propertyMap)
                .toString ();
    }
}
