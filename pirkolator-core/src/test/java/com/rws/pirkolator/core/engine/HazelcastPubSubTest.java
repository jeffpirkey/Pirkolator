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

import java.util.UUID;

import javax.annotation.Nullable;
import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import com.rws.pirkolator.core.grid.HazelcastGrid;
import com.rws.utility.common.UUIDs;
import com.rws.utility.test.AbstractPirkolatorTest;

/**
 * 
 * @author jpirkey
 *
 */
@DirtiesContext (classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class HazelcastPubSubTest extends AbstractPirkolatorTest {

    @Resource
    @Nullable
    Hub hub;

    @Resource
    @Nullable
    HazelcastGrid hazelcastGrid;

    @Resource
    @Nullable
    HazelcastPubSub hazelcastPubSub;

    @Test
    public void testGetPubSubGrids () {

        Assert.assertEquals (notNull (hub).getPubSubGrids ().size (), 2);
    }

    @Test
    public void testRegisterPublication () {

        final UUID testHubId = UUIDs.generateUUID ();
        final HazelcastPublicationTester tester = new HazelcastPublicationTester (testHubId, notNull (hazelcastGrid));
        tester.addTestPublication ();

        sleep (1000);

        // Check if Pub/Sub contains UUID for Publisher
        Assert.assertTrue ("Publisher not added to the Hazelcast Pub/Sub",
                notNull (hazelcastPubSub).containsPublisher (tester.getPublisherId ()));

        // Check if Pub/Sub contains UUID for Publication
        Assert.assertTrue ("Publication not added to the Hazelcast Pub/Sub", notNull (hazelcastPubSub)
                .containsPublication (tester.getPublication ().getId ()));

        // Check if HUB contains UUID for Publisher
        Assert.assertTrue ("Publication not added to the HUB",
                notNull (hub).containsPublication (tester.getOriginalPublication ().getId ()));

        // Check if hub contains UUID for subscription
        Assert.assertTrue ("Publication not added to the HUB",
                notNull (hub).containsPublication (tester.getOriginalPublication ().getId ()));

        // Check that the registered publication is not null
        final Publication publicationFromHub = notNull (hub).getPublication (tester.getOriginalPublication ().getId ());
        Assert.assertNotNull (publicationFromHub);

        final Publication publicationFromRegistration = tester.getRegisteredPublication ().getPublication ();
        Assert.assertNotNull (publicationFromRegistration);
    }

    @Ignore ("Not yet implemented")
    @Test
    public void testRemovePublisher () {

        fail ("Not yet implemented");
    }

}
