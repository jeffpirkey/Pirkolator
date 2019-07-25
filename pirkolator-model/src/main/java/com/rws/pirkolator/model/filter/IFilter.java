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
package com.rws.pirkolator.model.filter;

import java.io.Serializable;


// TODO jpirkey - Look at providing match type on a per send basis instead of per filters
/**
 * <p>This interface describes the base interface used by the engines to match
 * data provided with data consumed.
 * 
 * <p>Filter Match Types:
 * 
 * <p>The source filter is this instance of the filter being called while the target
 * filter is the filter that this instance will be matching against.
 * <p><ul>
 * <li>If MatchType.Any, then any value in the target filter will return
 *      on a match against the source filter.</li>
 * <li>If MatchType.Exact, then the target filter's values must match exactly against
 *      a value in the source filter.</li>
 * <li>If MatchType.All, the source filter's values must match exactly against
 *      values in the target filter.</li>
 * </ul>
 * 
 * @author jpirkey
 * @since 0.1.0
 */
public interface IFilter extends Serializable {

    enum MatchType {
        Any, Exact, All
    }

    MatchType getMatchType ();
    
    /**
     * Returns a match based on the filter parameter.  For match type definitions,
     * see above, {@link IFilter}.
     * 
     * @param filter
     * @return
     */
    boolean match (final IFilter filter);
}
