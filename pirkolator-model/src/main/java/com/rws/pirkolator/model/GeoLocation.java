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

import java.io.Serializable;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

/**
 * This class defines a basic implementation of data that contains an
 * altitude, latitude and longitude needed for geo-location.
 * 
 * @author jpirkey
 *
 */
public class GeoLocation implements Serializable {

    private static final long serialVersionUID = 1L;
    
    // *************************************************************************
    // ** Member variables
    // *************************************************************************
    
    @Nullable
    private Double latitude;
    @Nullable
    private Double longitude;
    @Nullable
    private Integer altitude;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public GeoLocation () {

        super ();
    }

    public GeoLocation (final Double latitude, final Double longitude) {

        super ();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public GeoLocation (final Double latitude, final Double longitude, final Integer altitude) {

        super ();
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    /**
     * 
     * @return {@link Double} value of the latitude or null if the value has
     * not been assigned.
     */
    public @Nullable
    Double getLatitude () {

        return latitude;
    }

    public void setLatitude (final Double latitude) {

        this.latitude = latitude;
    }

    /**
     * 
     * @return {@link Double} value of the longitude or null if the value has
     * not been assigned.
     */
    public @Nullable
    Double getLongitude () {

        return longitude;
    }

    public void setLongitude (final Double longitude) {

        this.longitude = longitude;
    }

    /**
     * 
     * @return {@link Integer} value of the altitude of null if the value has
     * not been assigned.
     */
    public @Nullable
    Integer getAltitude () {

        return altitude;
    }

    public void setAltitude (final Integer altitude) {

        this.altitude = altitude;
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************
    
    @Override
    public int hashCode(){
    	return Objects.hashCode(super.hashCode(), latitude, longitude, altitude);
    }
    
    @Override
    public boolean equals(final @Nullable Object object){
    	if (object instanceof GeoLocation) {
    		if (!super.equals(object)) 
    			return false;
    		final GeoLocation that = (GeoLocation) object;
    		return Objects.equal(latitude, that.latitude)
    			&& Objects.equal(longitude, that.longitude)
    			&& Objects.equal(altitude, that.altitude);
    	}
    	return false;
    }

    @Override
    public String toString() {
    	return Objects.toStringHelper(this)
    		.add("super", super.toString())
    		.add("latitude", latitude)
    		.add("longitude", longitude)
    		.add("altitude", altitude)
    		.toString();
    }
}
