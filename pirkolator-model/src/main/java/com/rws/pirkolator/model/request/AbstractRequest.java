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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.data.annotation.Id;

import com.google.common.base.Objects;
import com.rws.pirkolator.schema.IUserSession;
import com.rws.pirkolator.schema.request.IRequest;

@XmlRootElement
public abstract class AbstractRequest implements IRequest {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    @Id
    private final String id;
    private final String requesterId;
    private final IUserSession userSession;
    private final String requestType;
    private String label;

    private final List<String> parameterList = new ArrayList<> ();

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public AbstractRequest (final IRequest request) {

        super ();

        id = request.getId ();
        requesterId = request.getRequesterId ();
        userSession = request.getUserSession ();
        requestType = request.getRequestType ();
        label = "No label";
    }

    public AbstractRequest (final String id, final IUserSession userSession, final String requesterId,
            final String requestType) {

        super ();

        this.id = id;
        this.userSession = userSession;
        this.requesterId = requesterId;
        this.requestType = requestType;
        label = "No label";
    }

    public AbstractRequest (final String id, final IUserSession userSession, final String requesterId,
            final String requestType, final String label) {

        super ();

        this.id = id;
        this.userSession = userSession;
        this.requesterId = requesterId;
        this.requestType = requestType;
        this.label = label;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    @Override
    public String getId () {

        return id;
    }

    @Override
    public String getLabel () {

        return label;
    }

    public void setLabel (final String label) {

        this.label = label;
    }

    @Override
    public IUserSession getUserSession () {

        return userSession;
    }

    @Override
    public String getRequesterId () {

        return requesterId;
    }

    @Override
    public String getRequestType () {

        return requestType;
    }

    @Override
    public List<String> getParameterList () {

        return parameterList;
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    public void addParameter (final String param) {

        checkNotNull (param);
        parameterList.add (param);
    }

    public void addAllParameter (final Collection<String> params) {

        checkNotNull (params);
        parameterList.addAll (params);
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), id, requesterId, userSession, requestType, label, parameterList);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof AbstractRequest) {
            if (!super.equals (object))
                return false;
            final AbstractRequest that = (AbstractRequest) object;
            return Objects.equal (id, that.id) && Objects.equal (requesterId, that.requesterId)
                    && Objects.equal (userSession, that.userSession) && Objects.equal (requestType, that.requestType)
                    && Objects.equal (label, that.label) && Objects.equal (parameterList, that.parameterList);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("id", id)
                .add ("requesterId", requesterId).add ("userSession", userSession).add ("requestType", requestType)
                .add ("label", label).add ("parameterList", parameterList).toString ();
    }
}
