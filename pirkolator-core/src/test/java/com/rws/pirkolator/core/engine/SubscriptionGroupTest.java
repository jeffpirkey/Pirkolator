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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.rws.utility.common.Preconditions.notNull;
import static org.junit.Assert.fail;

import javax.annotation.Nullable;
import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import com.rws.pirkolator.model.Message;
import com.rws.utility.test.AbstractPirkolatorTest;

// TODO jpirkey - More Subscription Group tests

@ContextConfiguration (locations = { "classpath:com/rws/pirkolator/core/engine/testSubscriptionGroup-context.xml" })
public class SubscriptionGroupTest extends AbstractPirkolatorTest {

    @Resource
    @Nullable
    Hub hub;

    @Resource (name = "testSubscriber1")
    @Nullable
    TestSubscriberGroup testSubscriber1;

    @Resource (name = "testSubscriber2")
    @Nullable
    TestSubscriberGroup testSubscriber2;

    @Resource
    @Nullable
    TestTypePublisher testPublisher;

    @Test
    public void testRegisterSubscription () {

        sleep (1000);

        /***** Test Subscriber 1 *****/

        // Check if hub contains subscription group
        Assert.assertTrue ("Subscription Group not added to HUB",
                notNull (hub).containsSubscriptionGroup (notNull (testSubscriber1).getSubscription ().getGroupName ()));

        /***** Test Subscriber 2 *****/

        // Check if HUB contains subscription group
        Assert.assertTrue ("Subscription Group B not added to the HUB",
                notNull (hub).containsSubscriptionGroup (notNull (testSubscriber2).getSubscription ().getGroupName ()));
    }

    @Ignore ("Not yet implemented")
    @Test
    public void testRemoveSubscriber () {

        fail ("Not yet implemented");
    }

    @Test
    public void testRegisterPublication () {

        sleep (1000);

        // Check if hub contains UUID for publication
        Assert.assertTrue ("Publication not added in a timely fashion",
                notNull (hub).containsPublication (notNull (testPublisher).getPublication ().getId ()));

        // Check that the registered publication has a group name that is equal
        // to the hub's group name
        final Publication publicationFromHub =
                notNull (hub).getPublication (notNull (testPublisher).getPublication ().getId ());
        Assert.assertNotNull (publicationFromHub);

    }

    @Ignore ("Not yet implemented")
    @Test
    public void testRemovePublisher () {

        fail ("Not yet implemented");
    }

    @Test
    public void testPublish () {

        notNull (testPublisher).publish ("Test");
        Message msg1 = null;
        Message msg2 = null;
        try {
            msg1 = notNull (testSubscriber1).awaitMessage (1000);

            msg2 = notNull (testSubscriber2).awaitMessage (1000);

        } catch (final InterruptedException ex) {
            ex.printStackTrace ();
        }

        Assert.assertTrue ("Expected only one subscriber to receive a message",
                ((msg1 == null && msg2 != null) || (msg1 != null && msg2 == null)));
    }

    @Test
    public void testPublishByReference () {

        final TestData txData = new TestData ();

        notNull (testPublisher).publish (txData, false);
        Message msg = null;
        try {
            msg = notNull (testSubscriber1).awaitMessage (1000);
        } catch (final InterruptedException ex) {
            ex.printStackTrace ();
        }

        Message msg2 = null;
        try {
            msg2 = notNull (testSubscriber2).awaitMessage (1000);
        } catch (final InterruptedException ex) {
            ex.printStackTrace ();
        }

        Assert.assertTrue ("Expected only one message to be received", (msg == null && msg2 != null)
                || (msg != null && msg2 == null));

    }

    @Test
    @Ignore
    public void testPublishByCopy () {

        final TestData txData = new TestData ();

        notNull (testPublisher).publish (txData, true);
        Message msg = null;
        try {
            msg = notNull (testSubscriber1).awaitMessage (1000);
        } catch (final InterruptedException ex) {
            ex.printStackTrace ();
        }
        checkNotNull (msg);
        Assert.assertTrue ("Not a copy", msg.isCopy ());

        Assert.assertNotNull ("Expected Response was null", msg);
        Assert.assertEquals ("Expected only 1 object in message", 1, msg.get ().size ());

        final TestData rxData = msg.iterate (TestData.class).next ();

        Assert.assertNotNull ("Test Data is null", rxData);
        Assert.assertEquals ("UUIDs not the same", txData.getId (), rxData.getId ());
    }
}
