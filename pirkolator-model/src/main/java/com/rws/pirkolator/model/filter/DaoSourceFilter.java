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

import com.rws.pirkolator.model.DaoSource;

public class DaoSourceFilter implements IFilter {

    private static final long serialVersionUID = 1L;

    private final DaoSource daoSource;

    public DaoSourceFilter (final DaoSource source) {

        super ();

        daoSource = source;
    }

    @Override
    public MatchType getMatchType () {

        return MatchType.Exact;
    }

    public final DaoSource getDaoSource () {

        return daoSource;
    }

    @Override
    public boolean match (final IFilter filter) {

        if (filter instanceof DaoSourceFilter) {
            final DaoSource targetSource = ((DaoSourceFilter) filter).getDaoSource ();
            return daoSource.getId ().equals (targetSource.getId ());
        }

        return false;
    }
}
