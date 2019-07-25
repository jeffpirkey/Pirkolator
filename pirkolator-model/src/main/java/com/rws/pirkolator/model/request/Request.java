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
package com.rws.pirkolator.model.request;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;
import com.rws.pirkolator.model.UserSession;
import com.rws.pirkolator.schema.request.IRequest;
import com.rws.utility.common.UUIDs;

@XmlRootElement
public class Request extends AbstractRequest {

    public final static String DEFAULT_REQUEST_TYPE = "default";

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public Request () {

        super (UUIDs.defaultUUIDAsString (), new UserSession (), DEFAULT_REQUEST_TYPE, UUIDs.defaultUUIDAsString ());
    }

    public Request (final String id, final UserSession userSession, final String requesterId, final String requestType,
            final String label) {

        super (id, userSession, requesterId, requestType, label);
    }

    public Request (final String id, final UserSession userSession, final String requesterId, final String requestType) {

        super (id, userSession, requesterId, requestType);
    }

    public Request (final IRequest request) {

        super (request);
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).toString ();
    }
}
