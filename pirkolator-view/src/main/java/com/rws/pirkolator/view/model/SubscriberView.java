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

public class SubscriberView extends AbstractLabeledIdentifiable {

    private static final long serialVersionUID = 1L;

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    private String systemId;
    private String systemIdentifier;
    private SubscriptionView subscriptionView;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public SubscriberView (final UUID id, final String label, final UUID systemId, final String systemIdentifier,
            final SubscriptionView view) {

        this (UUIDs.toString (id), label, UUIDs.toString (systemId), systemIdentifier, view);
    }

    public SubscriberView (final String id, final String label, final String systemId, final String systemIdentifier,
            final SubscriptionView view) {

        super (id, label);

        this.systemId = systemId;
        this.systemIdentifier = systemIdentifier;
        subscriptionView = view;
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

    public SubscriptionView getSubscriptionView () {

        return subscriptionView;
    }

    public void setSubscriptionView (final SubscriptionView subscriptionView) {

        this.subscriptionView = subscriptionView;
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), systemId, systemIdentifier, subscriptionView);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof SubscriberView) {
            if (!super.equals (object))
                return false;
            final SubscriberView that = (SubscriberView) object;
            return Objects.equal (systemId, that.systemId)
                    && Objects.equal (systemIdentifier, that.systemIdentifier)
                    && Objects.equal (subscriptionView, that.subscriptionView);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("systemId", systemId)
                .add ("systemIdentifier", systemIdentifier).add ("subscriptionView", subscriptionView).toString ();
    }
}
