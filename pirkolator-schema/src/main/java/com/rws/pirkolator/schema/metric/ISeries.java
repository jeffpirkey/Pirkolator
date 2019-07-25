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
package com.rws.pirkolator.schema.metric;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This interface describes a list or series of {@link IMeasurement} instances.
 * 
 * @author jpirkey
 *
 * @param <P>
 * @param <V>
 */
public interface ISeries<P, V> extends List<IMeasurement<P, V>> {

    /**
     * @return {@link UUID} of the id of the series, does not have to be unique
     */
    UUID getId ();

    /**
     * @return {@link UUID} of the process that is generating the measurements
     * for the series
     */
    String getSourceId ();

    /**
     * @return {@link Map} of additional properties associated with this series
     */
    Map<String, String> getPropertyMap ();
}
