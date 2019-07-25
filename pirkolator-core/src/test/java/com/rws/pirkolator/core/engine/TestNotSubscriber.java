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

import static com.rws.utility.common.Preconditions.notNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rws.pirkolator.model.Message;
import com.rws.pirkolator.model.filter.TypeFilter;
import com.rws.utility.common.UUIDs;

public class TestNotSubscriber extends AbstractSubscriber {

    private static final Logger LOG = notNull (LoggerFactory.getLogger (TestNotSubscriber.class));

    private volatile CountDownLatch latch = new CountDownLatch (1);

    public TestNotSubscriber () {

        super (UUIDs.generateUUID (), "Test Not Subscriber");
    }

    public boolean awaitMessage (final long time) {

        try {
            return latch.await (time, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ex) {
            LOG.warn ("Awaiting message interrupted");
        }

        return false;
    }

    @Override
    public void receive (final Message message) {

        throw new RuntimeException ("Received message on " + getName () + " " + message.toString ());
    }

    public void reset () {

        latch = new CountDownLatch (1);
    }

    @Override
    public void doConstruct (final Subscription sub) {

        sub.setLabel ("Test Not Subscription");
        sub.addFilter (new TypeFilter (NotData.class));
    }
}
