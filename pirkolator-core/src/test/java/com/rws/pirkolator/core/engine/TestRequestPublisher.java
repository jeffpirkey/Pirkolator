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

import com.rws.pirkolator.model.filter.RequestFilter;
import com.rws.utility.common.UUIDs;

public class TestRequestPublisher extends AbstractPublisher {

    public TestRequestPublisher () {

        super (UUIDs.generateUUID (), "Test Request Publisher");
    }

    @Override
    public void doConstruct (final Publication pub) {

        pub.setLabel ("Test Request Publication");
        pub.addFilter (new RequestFilter("test"));
    }
}
