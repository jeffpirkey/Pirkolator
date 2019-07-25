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
package com.rws.pirkolator.core.data.model;

import static com.rws.utility.common.Preconditions.notNull;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import com.rws.pirkolator.core.request.RequestProcess;
import com.rws.pirkolator.core.transform.RequestEventToViewFunction;
import com.rws.pirkolator.core.utility.serial.KryoUtility;
import com.rws.pirkolator.core.utility.serial.SerializationUtility;
import com.rws.pirkolator.model.Message;
import com.rws.pirkolator.model.UserSession;
import com.rws.pirkolator.model.request.Request;
import com.rws.pirkolator.model.request.RequestEvent;
import com.rws.pirkolator.model.request.RequestState;
import com.rws.pirkolator.model.request.RequestStats;
import com.rws.pirkolator.view.model.DalView;
import com.rws.pirkolator.view.model.RequestView;
import com.rws.pirkolator.view.model.SystemView;
import com.rws.utility.common.UUIDs;

/**
 * <ul>
 * <li>Verify views are serializable
 * <li>Verify Response is serializable
 * </ul>
 * 
 * @author jpirkey
 *
 */
public class SerializationTest {

    @Test
    public void testDalView () {

        final String id = UUIDs.defaultUUIDAsString ();
        final String systemId = UUIDs.defaultUUIDAsString ();
        final String daoId = UUIDs.defaultUUIDAsString ();
        final String systemIdentifier = "system identifier";
        final String name = "test-dal";
        final boolean correlationEnabled = true;
        final DalView dal = new DalView (id, name, correlationEnabled, daoId, systemId, systemIdentifier);
        final DalView testResult = SerializationUtility.copyObject (dal, DalView.class);
        Assert.assertNotNull (testResult);
    }

    @Test
    public void testSystemView () {

        final UUID id = UUIDs.defaultUUID ();
        final SystemView testObj = new SystemView (id, "Pirkolator A");
        final SystemView testResult = SerializationUtility.copyObject (testObj, SystemView.class);
        Assert.assertNotNull (testResult);
    }

    @Test
    public void testRequestViewFunction () {

        final String id = UUIDs.defaultUUIDAsString ();
        final String requesterId = UUIDs.defaultUUIDAsString ();
        final UserSession session = new UserSession ("user-id", "session-id");

        final RequestProcess rp = new RequestProcess ();
        final Request request = new Request (id, session, requesterId, "load");
        request.addParameter ("test");
        rp.setRequest (request);

        final RequestStats stats = new RequestStats ();
        stats.setCurrentProgress (1);
        stats.setId (id);
        stats.setTimeCompleted (3);
        stats.setTimeSubmitted (2);
        stats.setTotalProgress (4);
        rp.setStats (stats);

        rp.setState (RequestState.INPROGRESS);

        final RequestEventToViewFunction func = new RequestEventToViewFunction ();
        final RequestView view = func.apply (new RequestEvent (rp.getRequest (), rp.getStats (), rp.getState ()));

        final RequestView resultView = SerializationUtility.copyObject (view, RequestView.class);
        Assert.assertNotNull (resultView);
    }

    @Test
    public void testRequest () {

        final String id = UUIDs.defaultUUIDAsString ();
        final String requesterId = UUIDs.defaultUUIDAsString ();
        final UserSession session = new UserSession ("user-id", "session-id");
        final Request request = new Request (id, session, requesterId, "load");
        request.addParameter ("test");

        final Request resultRequest = SerializationUtility.copyObject (request, Request.class);
        Assert.assertNotNull (resultRequest);

    }

    @Test
    public void testRequestProcess () {

        final String id = UUIDs.defaultUUIDAsString ();
        final String requesterId = UUIDs.defaultUUIDAsString ();
        final UserSession session = new UserSession ("user-id", "session-id");
        final Request request = new Request (id, session, requesterId, "load");
        request.addParameter ("test");

        final UUID fulfillerId = UUIDs.generateUUID ();
        final RequestProcess rp = new RequestProcess ();
        rp.getStats ().setTimeCompleted (2);
        rp.getStats ().setCurrentProgress (3);
        rp.addFulfiller (fulfillerId);
        rp.setRequest (request);
        rp.setState (RequestState.INPROGRESS);
        rp.getStats ().setTimeSubmitted (4);
        rp.getStats ().setTotalProgress (5);

        final RequestProcess resultRP = SerializationUtility.copyObject (rp, RequestProcess.class);
        Assert.assertNotNull (resultRP);
    }

    @Test
    public void testKryoMessage () {

        final UUID testId = UUIDs.generateUUID ();
        final String testString = "Test Info";

        final Message testMessage = new Message ();
        testMessage.add (testId);
        testMessage.add (testString);

        final Message resultMsg =
                KryoUtility.readBytesIntoMessage (notNull (KryoUtility.writeBytes (testMessage)));
        Assert.assertNotNull ("Expected Response not to be null", resultMsg);
        Assert.assertEquals ("Expected Response Ids to be the same", testMessage.getId (), resultMsg.getId ());

        Assert.assertEquals ("Expected Strings to be the same", testString, resultMsg.get (String.class).iterator ()
                .next ());
        Assert.assertEquals ("Expected UUIDs to be the same", testId, resultMsg.get (UUID.class).iterator ().next ());

        final Message resultMsg2 = KryoUtility.copyMessage (testMessage);
        Assert.assertNotNull ("Expected Response not to be null", resultMsg2);
        Assert.assertEquals ("Expected Response Ids to be the same", testMessage.getId (), resultMsg.getId ());

        Assert.assertEquals ("Expected Strings to be the same", testString, resultMsg2.get (String.class).iterator ()
                .next ());
        Assert.assertEquals ("Expected UUIDs to be the same", testId, resultMsg2.get (UUID.class).iterator ().next ());
    }
}
