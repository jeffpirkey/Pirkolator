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
package com.rws.pirkolator.view.model;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;
import com.rws.pirkolator.model.AbstractLabeledIdentifiable;

@XmlRootElement
public class DalView extends AbstractLabeledIdentifiable {

    private static final long serialVersionUID = 1L;

    private String daoId;
    private String systemId;
    private String systemIdentifier;

    private boolean correlationEnabled;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public DalView (final String id, final String label, final boolean correlationEnabled, final String daoId,
            final String systemId, final String systemIdentifier) {

        super (id, label);

        this.correlationEnabled = correlationEnabled;
        this.daoId = daoId;
        this.systemId = systemId;
        this.systemIdentifier = systemIdentifier;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public String getDaoId () {

        return daoId;
    }

    public void setDaoId (final String id) {

        daoId = id;
    }

    public boolean isCorrelationEnabled () {

        return correlationEnabled;
    }

    public void setCorrelationEnabled (final boolean correlationEnabled) {

        this.correlationEnabled = correlationEnabled;
    }

    public String getSystemIdentifier () {

        return systemIdentifier;
    }

    public void setSystemIdentifier (final String systemIdentifier) {

        this.systemIdentifier = systemIdentifier;
    }

    public String getSystemId () {

        return systemId;
    }

    public void setSystemId (final String systemId) {

        this.systemId = systemId;
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), daoId, systemId, systemIdentifier, correlationEnabled);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof DalView) {
            if (!super.equals (object))
                return false;
            final DalView that = (DalView) object;
            return Objects.equal (daoId, that.daoId) && Objects.equal (systemId, that.systemId)
                    && Objects.equal (systemIdentifier, that.systemIdentifier)
                    && Objects.equal (correlationEnabled, that.correlationEnabled);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("daoId", daoId)
                .add ("systemId", systemId).add ("systemIdentifier", systemIdentifier)
                .add ("correlationEnabled", correlationEnabled).toString ();
    }

}