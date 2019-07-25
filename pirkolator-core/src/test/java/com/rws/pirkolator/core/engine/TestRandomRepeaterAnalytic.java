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

import java.io.Serializable;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import com.rws.pirkolator.core.analytic.AbstractMetricAnalytic;
import com.rws.pirkolator.model.filter.TypeFilter;
import com.rws.utility.common.UUIDs;

public class TestRandomRepeaterAnalytic extends AbstractMetricAnalytic {

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    final int interval;
    final AtomicInteger count = new AtomicInteger ();
    final Timer timer = new Timer ("random-repeater");

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public TestRandomRepeaterAnalytic (final int interval) {

        super (UUIDs.generateUUID (), "Random Repeater Analytic");

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

        pub.setLabel ("Test Random Repeater Publication");
        pub.setDescription ("TypeFilter for Serializable publication");
        pub.addFilter (new TypeFilter (Serializable.class));
    }

    @Override
    public void started () {

        new PublishTask ().run ();
    }

    // *************************************************************************
    // ** Member classes 
    // *************************************************************************

    class PublishTask extends TimerTask {

        Random random = new Random ();

        @Override
        public void run () {

            TestRandomRepeaterAnalytic.this.publish ("Test " + count.getAndIncrement ());

            final int delay = random.nextInt (interval);
            timer.schedule (new PublishTask (), delay);
        }
    }
}
