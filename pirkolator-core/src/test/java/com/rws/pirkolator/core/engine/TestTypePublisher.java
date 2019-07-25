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
package com.rws.pirkolator.core.engine;

import java.io.Serializable;

import com.rws.pirkolator.model.filter.TypeFilter;
import com.rws.utility.common.UUIDs;

public class TestTypePublisher extends AbstractPublisher {

    Class<?> type;

    public TestTypePublisher () {

        this (Serializable.class);
    }

    public TestTypePublisher (final Class<?> type) {

        super (UUIDs.generateUUID (), "Test Publisher");

        this.type = type;
    }

    @Override
    public void doConstruct (final Publication pub) {

        pub.addFilter (new TypeFilter (type));
    }
}
