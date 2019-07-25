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
package com.rws.pirkolator.core.engine;

import static com.rws.utility.common.Preconditions.notNull;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;
import javax.annotation.Resource;

import com.rws.pirkolator.core.analytic.AbstractMetricAnalytic;
import com.rws.pirkolator.model.filter.TypeFilter;
import com.rws.utility.common.UUIDs;

public class TestPeriodicRepeaterAnalytic extends AbstractMetricAnalytic {

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    @Resource
    @Nullable
    SystemResourceManager resourceManager;

    private final int interval;

    final AtomicInteger count = new AtomicInteger ();

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public TestPeriodicRepeaterAnalytic (final int interval) {

        super (UUIDs.generateUUID (), "Periodic Repeater Analytic");

        this.interval = interval;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public int getSentCount () {

        return count.get ();
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    @Override
    public void doConstruct (final Publication pub, final Subscription sub) {

        pub.setLabel ("Test Periodic Repeater Publication");
        pub.setDescription ("TypeFilter for Serializable publication");
        pub.addFilter (new TypeFilter (Serializable.class));
    }

    @Override
    public void started () {

        notNull (resourceManager).getScheduledThreadExecutor ("periodic-repeater", 1).scheduleAtFixedRate (
                new PublishTask (), interval, interval, TimeUnit.MILLISECONDS);

        super.started ();
    }

    // *************************************************************************
    // ** Member classes 
    // *************************************************************************

    class PublishTask implements Runnable {

        @Override
        public void run () {

            TestPeriodicRepeaterAnalytic.this.publish ("Test " + count.getAndIncrement ());
        }
    }
}
