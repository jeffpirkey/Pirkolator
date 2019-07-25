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
package com.rws.pirkolator.core.net;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;
import com.rws.pirkolator.core.engine.AbstractPublisher;
import com.rws.pirkolator.core.engine.Publication;
import com.rws.pirkolator.model.filter.IFilter;

/**
 * This class provides the capability to take data from an end-point, such as 
 * an Apache Camel route, and publish it to the Pub/Sub engines.
 * 
 * @author jpirkey
 * @since 0.1.0
 */
public class InRoute extends AbstractPublisher {

    private final Set<IFilter> filterSet = Sets.newConcurrentHashSet ();

    public InRoute (final UUID id, final String name) {

        super (id, name);
    }

    public InRoute (final UUID id) {

        super (id);
    }

    public void setFilterSet (final Set<IFilter> set) {

        filterSet.addAll (set);
    }

    @Override
    public void doConstruct (final Publication pub) {

        for (final IFilter filter : filterSet) {
            pub.addFilter (checkNotNull (filter));
        }
    }
}
