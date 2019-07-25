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

import java.io.Serializable;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.rws.pirkolator.schema.metric.IMeasurement;

/**
 * The Measurement class defines a discrete value in a series of data.
 * 
 * @author jpirkey
 * @since 0.1.0
 */
public class Measurement<P, V> implements IMeasurement<P, V>, Serializable {

    private static final long serialVersionUID = 1L;

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    private final P period;
    private final V value;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public Measurement (final P period, final V value) {

        super ();

        this.period = period;
        this.value = value;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    @Override
    public P getPeriod () {

        return period;
    }

    @Override
    public V getValue () {

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

        if (object instanceof Measurement) {
            if (!super.equals (object))
                return false;
            final Measurement<?, ?> that = (Measurement<?, ?>) object;
            return Objects.equal (this.period, that.period) && Objects.equal (this.value, that.value);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("period", period)
                .add ("value", value).toString ();
    }

}
