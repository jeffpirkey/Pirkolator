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

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rws.pirkolator.model.Message;
import com.rws.pirkolator.model.filter.TypeFilter;
import com.rws.utility.common.Globals;
import com.rws.utility.common.UUIDs;

public class TestSubscriber extends AbstractSubscriber {

    static Logger LOG = notNull (LoggerFactory.getLogger (TestSubscriber.class));
    static final AtomicInteger sSubscriberCounter = new AtomicInteger ();

    StringBuilder runLog = new StringBuilder ();

    int expectedCount = 1;
    CountDownLatch latch = new CountDownLatch (1);
    @Nullable
    AtomicLong startTime;
    @Nullable
    AtomicLong finishTime;
    boolean inOrder = true;
    int previousNumber = -1;

    public TestSubscriber () {

        super (UUIDs.generateUUID (), "Test Subscriber " + sSubscriberCounter.getAndIncrement ());
    }

    public void setExpectedMessageCount (final int count) {

        expectedCount = count;
        latch = new CountDownLatch (count);
    }

    @Override
    public synchronized void receive (final Message message) {

        if (startTime == null) {
            startTime = new AtomicLong (System.currentTimeMillis ());
        }

        final Date rxDate = new Date (System.currentTimeMillis ());

        final int msgNumber = Integer.valueOf (message.getHeader ("count"));
        if (msgNumber != previousNumber + 1) {
            LOG.info ("Out of order on " + msgNumber + " from previous " + previousNumber);
            inOrder = false;
        }
        previousNumber = msgNumber;
        runLog.append ("Received message ").append (msgNumber).append (" at ").append (rxDate)
                .append (Globals.NEW_LINE);

        latch.countDown ();
        if (latch.getCount () == 0) {
            finishTime = new AtomicLong (System.currentTimeMillis ());
        }
    }

    public void logReceive () {

        runLog.append ("Run time = ").append (notNull (finishTime).get () - notNull (startTime).get ());
        runLog.append (Globals.NEW_LINE);
        LOG.debug (runLog.toString ());
    }

    public boolean assertCompleted (final long ms) {

        try {
            return latch.await (ms, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ex) {
            LOG.warn (ex.getLocalizedMessage (), ex);
        }
        return false;
    }

    public boolean isInOrder () {

        return inOrder;
    }

    @Override
    public void doConstruct (final Subscription sub) {

        sub.addFilter (new TypeFilter (Serializable.class));
    }
}
