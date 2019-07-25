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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rws.pirkolator.model.Message;

public class MessageCarrierTest {

    static Logger LOG = notNull (LoggerFactory.getLogger (MessageCarrierTest.class));

    SystemResourceManager resourceManager = new SystemResourceManager ();

    /**
     * Basic test of a message carrier to ensure that a single message carrier
     * and a single subscriber can get 1000 messages in order.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testMessageCarrier_one () throws InterruptedException {

        // FIXME jpirkey this may  not receive anything
        final TestSubscriber sub = new TestSubscriber ();
        sub.setExpectedMessageCount (1000);

        final MessageCarrier mc = new MessageCarrier (sub);

        final ExecutorService service = resourceManager.getCachedThreadExecutor ();
        service.execute (mc);

        for (int i = 0; i < 1000; i++) {
            final Message msgA = new Message ();
            final String tmp = notNull (String.valueOf (i));
            msgA.addHeader ("count", tmp);
            mc.getSubscriberReceivingQueue ().add (msgA);
        }

        Assert.assertTrue ("Expected subscriber to have received all messages in 10 seconds, but it has not.",
                sub.assertCompleted (10000));

        Assert.assertTrue ("Expected messages to be in order, but were not.", sub.isInOrder ());

        service.shutdownNow ();
        service.awaitTermination (30, TimeUnit.SECONDS);

        Assert.assertTrue ("Expected shutdown to be true, but it was false.", mc.isShutdown ());
    }

    /** 
     * Basic test to ensure that many message carriers and subscribers can
     * each receiver 1000 messages in order.
     * 
     */
    @Test
    public void testMessageCarrier_multiple () {

        final List<TestSubscriber> subscriberList = new ArrayList<> (16);
        final List<MessageCarrier> mcList = new ArrayList<> (16);
        for (int i = 0; i < 16; i++) {
            // FIXME jpirkey this may  not receive anything
            final TestSubscriber sub = new TestSubscriber ();
            sub.setExpectedMessageCount (1000);
            subscriberList.add (sub);

            final MessageCarrier mc = new MessageCarrier (sub);
            mcList.add (mc);
            final ExecutorService executioner =
                    new ThreadPoolExecutor (1, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable> (2048));
            executioner.execute (mc);
        }

        // Send 1000 messages through each message carrier
        for (int j = 0; j < 1000; j++) {
            for (int i = 0; i < 16; i++) {

                final Message msg = new Message ();
                final String tmp = notNull (String.valueOf (j));
                msg.addHeader ("count", tmp);
                mcList.get (i).getSubscriberReceivingQueue ().add (msg);
            }
        }

        for (final TestSubscriber sub : subscriberList) {
            Assert.assertTrue ("Expected subscriber to have received all messages in 10 seconds, but it has not.",
                    sub.assertCompleted (10000));
            Assert.assertTrue ("Expected messages to be in order, but were not.", sub.isInOrder ());
        }
    }
}
