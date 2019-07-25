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
import com.rws.pirkolator.model.AbstractIdentifiable;

/**
 * A measurement representation with the id of the view being the period.
 * 
 * @author jpirkey
 *
 */
public class MeasurementView extends AbstractIdentifiable {

    private static final long serialVersionUID = 1L;

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    private final String period;
    private final String value;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public MeasurementView (final String period, final String value) {

        super (period);

        this.period = period;
        this.value = value;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public String getPeriod () {

        return period;
    }

    public String getValue () {

        return value;
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), period, value);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof MeasurementView) {
            if (!super.equals (object))
                return false;
            final MeasurementView that = (MeasurementView) object;
            return Objects.equal (period, that.period) && Objects.equal (value, that.value);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("period", period)
                .add ("value", value).toString ();
    }
}
