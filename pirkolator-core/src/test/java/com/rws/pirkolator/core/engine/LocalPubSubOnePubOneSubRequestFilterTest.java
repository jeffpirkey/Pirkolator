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

import javax.annotation.Nullable;
import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

import com.rws.pirkolator.model.Message;
import com.rws.pirkolator.model.UserSession;
import com.rws.pirkolator.model.request.Request;
import com.rws.utility.common.UUIDs;
import com.rws.utility.test.AbstractSpringTest;

@DirtiesContext (classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration (locations = { "classpath:config/system-context.xml",
        "classpath:com/rws/pirkolator/core/engine/localPubSubOnePubOneSubRequestFilterTest-context.xml" })
public class LocalPubSubOnePubOneSubRequestFilterTest extends AbstractSpringTest {

    @Resource (name = "testRequestSubscriber")
    @Nullable
    TestRequestSubscriber testRequestSubscriber;

    @Resource (name = "testNotRequestSubscriber")
    @Nullable
    TestNotSubscriber testNotSubscriber;

    @Resource (name = "testRequestPublisher")
    @Nullable
    TestRequestPublisher testPublisher;

    @Test (timeout = 30000)
    public void testPublishByDefault () {

        final String requestId = UUIDs.generateUUIDAsString ();
        final String requesterId = UUIDs.generateUUIDAsString ();
        final String userId = "user-id";
        final String sessionId = "session-id";
        final UserSession session = new UserSession (userId, sessionId);
        final Request testData = new Request (requestId, session, requesterId, "test");

        // Should not get message because test publisher not registered to send it?
        notNull (testPublisher).publish (testData);
        assertMessageReceived (notNull (testRequestSubscriber), testData);
        assertMessageNotReceived (notNull (testNotSubscriber));
    }

    @Test (timeout = 30000)
    public void testPublishByCopy () {

        final String requestId = UUIDs.generateUUIDAsString ();
        final String requesterId = UUIDs.generateUUIDAsString ();
        final String userId = "user-id";
        final String sessionId = "session-id";
        final UserSession session = new UserSession (userId, sessionId);
        final Request testData = new Request (requestId, session, requesterId, "test");

        // Should not get message because test publisher not registered to send it?
        notNull (testPublisher).publish (testData, true);
        assertMessageNotReceived (notNull (testNotSubscriber));
    }

    @Test (timeout = 30000)
    public void testPublishByReference () {

        final String requestId = UUIDs.generateUUIDAsString ();
        final String requesterId = UUIDs.generateUUIDAsString ();
        final String userId = "user-id";
        final String sessionId = "session-id";
        final UserSession session = new UserSession (userId, sessionId);
        final Request testData = new Request (requestId, session, requesterId, "test");

        // Should not get message because test publisher not registered to send it?
        notNull (testPublisher).publish (testData, false);
        assertMessageNotReceived (notNull (testNotSubscriber));
    }

    void assertMessageReceived (final TestRequestSubscriber sub, final Request txData) {

        Assert.assertTrue ("Expected subscriber to have received Message", sub.awaitMessage (1000));
        final Message msg = sub.getMessage ();

        Assert.assertNotNull ("Expected Message was null", msg);
        Assert.assertEquals ("Expected only 1 object in message", 1, msg.get ().size ());

        final Request rxData = msg.iterate (Request.class).next ();

        Assert.assertNotNull ("Test data is null", rxData);
        Assert.assertEquals ("UUIDs the same", txData.getId (), rxData.getId ());
    }

    void assertMessageNotReceived (final TestNotSubscriber sub) {

        Assert.assertFalse ("Expected subscriber to have not received Message", sub.awaitMessage (1000));
    }
}
