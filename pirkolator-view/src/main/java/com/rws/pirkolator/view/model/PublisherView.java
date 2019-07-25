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

import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.rws.pirkolator.model.AbstractLabeledIdentifiable;
import com.rws.utility.common.UUIDs;

public class PublisherView extends AbstractLabeledIdentifiable {

    private static final long serialVersionUID = 1L;

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    private String systemId;
    private String systemIdentifier;
    private PublicationView publicationView;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public PublisherView (final UUID id, final String label, final UUID systemId, final String systemIdentifier,
            final PublicationView view) {

        this (UUIDs.toString (id), label, UUIDs.toString (systemId), systemIdentifier, view);
    }

    public PublisherView (final String id, final String label, final String systemId, final String systemIdentifier,
            final PublicationView view) {

        super (id, label);

        this.systemId = systemId;
        this.systemIdentifier = systemIdentifier;
        publicationView = view;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public String getSystemId () {

        return systemId;
    }

    public void setSystemId (final String systemId) {

        this.systemId = systemId;
    }

    public String getSystemIdentifier () {

        return systemIdentifier;
    }

    public void setSystemIdentifier (final String systemIdentifier) {

        this.systemIdentifier = systemIdentifier;
    }

    public PublicationView getPublicationView () {

        return publicationView;
    }

    public void setPublicationView (final PublicationView publicationView) {

        this.publicationView = publicationView;
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), systemId, systemIdentifier, publicationView);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof PublisherView) {
            if (!super.equals (object))
                return false;
            final PublisherView that = (PublisherView) object;
            return Objects.equal (systemId, that.systemId)
                    && Objects.equal (systemIdentifier, that.systemIdentifier)
                    && Objects.equal (publicationView, that.publicationView);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("systemId", systemId)
                .add ("systemIdentifier", systemIdentifier).add ("publicationView", publicationView).toString ();
    }
}
