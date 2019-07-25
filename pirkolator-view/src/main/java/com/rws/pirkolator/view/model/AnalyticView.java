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

/**
 * 
 * @author jpirkey
 * @since 1.0.0
 */
@XmlRootElement
public class AnalyticView extends AbstractLabeledIdentifiable {

    private static final long serialVersionUID = 1L;

    private String systemId;
    private String systemLabel;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public AnalyticView (final String id, final String label, final String systemId, final String systemLabel) {

        super (id, label);

        this.systemId = systemId;
        this.systemLabel = systemLabel;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public String getSystemLabel () {

        return systemLabel;
    }

    public void setSystemLabel (final String value) {

        systemLabel = value;
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

        return Objects.hashCode (super.hashCode (), systemId, systemLabel);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof AnalyticView) {
            if (!super.equals (object))
                return false;
            final AnalyticView that = (AnalyticView) object;
            return Objects.equal (systemId, that.systemId)
                    && Objects.equal (systemLabel, that.systemLabel);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("systemId", systemId)
                .add ("systemLabel", systemLabel).toString ();
    }

}
