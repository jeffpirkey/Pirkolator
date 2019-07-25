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
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

import com.rws.pirkolator.core.grid.LocalGrid;
import com.rws.pirkolator.model.Message;
import com.rws.utility.test.AbstractPirkolatorTest;

// TODO jpirkey - add test for pub/sub with same analytic
// TODO jpirkey - add test for pub/sub with same filter on pub/sub

@DirtiesContext (classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration (locations = { "classpath:com/rws/pirkolator/core/engine/testSubscriber-context.xml",
        "classpath:com/rws/pirkolator/core/engine/testPublisher-context.xml" })
public class LocalPubSubTest extends AbstractPirkolatorTest {

    @Resource
    @Nullable
    Hub hub;

    @Resource
    @Nullable
    LocalGrid localGrid;

    @Resource
    @Nullable
    LocalPubSub localPubSub;

    @Resource
    @Nullable
    TestTypeSubscriber testSubscriber;

    @Resource
    @Nullable
    TestTypePublisher testPublisher;

    @Test
    public void testRegisterSubscription () {

        sleep (1000);

        // Check if Local Pub/Sub contains UUID for subscriber
        Assert.assertTrue ("Subscriber not added to the LocalPub/Sub",
                notNull (localPubSub).containsSubscriber (notNull (testSubscriber).getId ()));

        // Check if HUB contains UUID for subscriber
        Assert.assertTrue ("Subscriber not added to the HUB",
                notNull (hub).containsSubscriber (notNull (testSubscriber).getId ()));

        // Check if Local Pub/Sub contains subscription
        Assert.assertTrue ("Subscription not added to the Local Pub/Sub",
                notNull (localPubSub).containsSubscription (notNull (testSubscriber).getSubscription ().getId ()));

        // Check if HUB contains subscription group
        Assert.assertTrue ("Subscription Group not added to the HUB",
                notNull (hub).containsSubscriptionGroup (notNull (testSubscriber).getSubscription ().getGroupName ()));
    }

    @Ignore ("Not yet implemented")
    @Test
    public void testRemoveSubscriber () {

        fail ("Not yet implemented");
    }

    @Test
    public void testRegisterPublication () {

        sleep (1000);

        // Check if Local Pub/Sub contains UUID for publisher
        Assert.assertTrue ("Publisher not added to the Local Pub/Sub",
                notNull (localPubSub).containsPublisher (notNull (testPublisher).getId ()));

        // Check if HUB contains UUID for publisher
        Assert.assertTrue ("Publisher not added to the HUB",
                notNull (hub).containsPublisher (notNull (testPublisher).getId ()));

        // Check if hub contains UUID for publication
        Assert.assertTrue ("Publication not added to the Local Pub/Sub",
                notNull (localPubSub).containsPublication (notNull (testPublisher).getPublication ().getId ()));

        // Check if hub contains UUID for publication
        Assert.assertTrue ("Publication not added to the HUB",
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

        Assert.assertTrue ("Expected Test Subscriber to have received Message, but it did not",
                notNull (testSubscriber).awaitMessage (1000));
        final Message msg = notNull (testSubscriber).getMessage ();

        Assert.assertNotNull (msg);
        Assert.assertEquals ("Expected only 1 object in message", 1, msg.get ().size ());
    }

    @Test
    public void testPublishByReference () {

        final TestData txData = new TestData ();

        notNull (testPublisher).publish (txData, false);

        Assert.assertTrue ("Expected Test Subscriber to have received Message, but it did not",
                notNull (testSubscriber).awaitMessage (1000));
        final Message msg = notNull (testSubscriber).getMessage ();

        Assert.assertNotNull ("Expected Response was null", msg);
        Assert.assertEquals ("Expected only 1 object in message", 1, msg.get ().size ());
        Assert.assertEquals ("Not passed by reference", false, msg.isCopy ());
        
        if (LOG.isDebugEnabled ()) {
            final StringBuilder logMsg = new StringBuilder ();
            final List<Serializable> list = msg.get ();
            logMsg.append ("Class type = ");
            logMsg.append (list.get (0).getClass ());
            logMsg.append ("; message = ");
            logMsg.append (list.get (0));
            LOG.debug (logMsg.toString ());
        }

        final TestData rxData = msg.iterate (TestData.class).next ();

        Assert.assertNotNull ("Test Data is null", rxData);
        
        Assert.assertEquals ("UUIDs not the same", txData.getId (), rxData.getId ());
    }

    @Test
    public void testPublishByCopy () {

        final TestData txData = new TestData ();

        notNull (testPublisher).publish (txData, true);

        Assert.assertTrue ("Expected Test Subscriber to have received Message, but it did not",
                notNull (testSubscriber).awaitMessage (1000));
        final Message msg = notNull (testSubscriber).getMessage ();

        Assert.assertNotNull ("Expected Response was null", msg);
        Assert.assertEquals ("Expected only 1 object in message", 1, msg.get ().size ());
        Assert.assertTrue ("Not a copy", msg.isCopy ());
        
        final TestData rxData = msg.iterate (TestData.class).next ();

        Assert.assertNotNull ("Test Data is null", rxData);
        
        Assert.assertEquals ("UUIDs not the same", txData.getId (), rxData.getId ());
    }
}
