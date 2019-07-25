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

import java.util.Set;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import com.rws.pirkolator.model.AbstractLabeledIdentifiable;

/**
 * 
 * @author jpirkey
 * @since 1.0.0
 */
@XmlRootElement
public class AbstractComponentView extends AbstractLabeledIdentifiable {

    private static final long serialVersionUID = 1L;

    private String systemId;
    private String systemIdentifier;

    private final Set<ConnectionView> connectionSet = Sets.newConcurrentHashSet ();

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public AbstractComponentView (final String id, final String label, final String systemId,
            final String systemIdentifier) {

        super (id, label);

        this.systemId = systemId;
        this.systemIdentifier = systemIdentifier;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public String getSystemIdentifier () {

        return systemIdentifier;
    }

    public void setSystemIdentifier (final String value) {

        systemIdentifier = value;
    }

    public String getSystemId () {

        return systemId;
    }

    public void setSystemId (final String systemId) {

        this.systemId = systemId;
    }

    Set<ConnectionView> getConnectionSet () {

        return connectionSet;
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), systemId, systemIdentifier, connectionSet);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof AbstractComponentView) {
            if (!super.equals (object))
                return false;
            final AbstractComponentView that = (AbstractComponentView) object;
            return Objects.equal (systemId, that.systemId)
                    && Objects.equal (systemIdentifier, that.systemIdentifier)
                    && Objects.equal (connectionSet, that.connectionSet);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("systemId", systemId)
                .add ("systemIdentifier", systemIdentifier).add ("connectionSet", connectionSet).toString ();
    }

}
