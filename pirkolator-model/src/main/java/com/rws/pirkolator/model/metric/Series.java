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
package com.rws.pirkolator.model.metric;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.rws.pirkolator.schema.metric.IMeasurement;
import com.rws.pirkolator.schema.metric.ISeries;
import com.rws.utility.common.UUIDs;

/**
 * 
 * <h2>View Properties</h2>
 * <ul>
 *  <li><b>"seriesLabel"</b> a pretty name for the series
 *  <li><b>"periodLabel"</b> a pretty name for the period type
 *  <li><b>"periodType"</b> a type identifier that is used by the UI to format the period
 *  <li><b>"valueLabel"</b> a pretty name for the value type
 *  <li><b>"valueType"</b> a type identifier that is used by the UI to format the period
 *  <li><b>"category"</b> a comma-separated list of any categories attached to the series; this is
 *  used by the UI to narrow down the display of series.
 * </ul>
 * <h2>Type Identifiers</h2>
 * <ul>
 *  <li><b>"string"</b>
 *  <li><b>"number"</b>
 *  <li><b>"boolean"</b>
 *  <li><b>"date"</b>
 *  <li><b>"object"</b>
 * </ul>
 * 
 * @author jpirkey
 *
 * @param <P> Period type
 * @param <V> Value type
 */
public class Series<P, V> extends ArrayList<IMeasurement<P, V>> implements ISeries<P, V> {

    private static final long serialVersionUID = 1L;

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    private final UUID id;
    private final String sourceId;
    private final Map<String, String> propertyMap = new ConcurrentHashMap<> ();

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public Series (final UUID id, final String sourceId) {

        super ();

        this.id = id;
        this.sourceId = sourceId;
    }

    public Series (final String sourceId) {

        super ();

        this.id = UUIDs.generateUUID ();
        this.sourceId = sourceId;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    @Override
    public UUID getId () {

        return id;
    }

    @Override
    public String getSourceId () {

        return sourceId;
    }

    @Override
    public Map<String, String> getPropertyMap () {

        return propertyMap;
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), id, sourceId, propertyMap);
    }

    @Override
    public boolean equals (@Nullable final Object object) {

        if (object instanceof Series) {
            if (!super.equals (object))
                return false;
            final Series<?, ?> that = (Series<?, ?>) object;
            return Objects.equal (this.id, that.id) && Objects.equal (this.sourceId, that.sourceId)
                    && Objects.equal (this.propertyMap, that.propertyMap);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("id", id).add ("sourceId", sourceId)
                .add ("propertyMap", propertyMap).toString ();
    }

}
