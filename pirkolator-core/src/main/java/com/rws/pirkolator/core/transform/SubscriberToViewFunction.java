/*******************************************************************************
 * Copyright 2014 Reality Warp Software
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
package com.rws.pirkolator.core.transform;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;
import javax.annotation.Resource;

import com.rws.pirkolator.core.engine.ISubscriber;
import com.rws.pirkolator.model.SystemInfo;
import com.rws.pirkolator.view.model.SubscriberView;
import com.rws.pirkolator.view.model.SubscriptionView;
import com.rws.utility.common.UUIDs;

public class SubscriberToViewFunction extends AbstractTransformFunction<ISubscriber, SubscriberView> {

    @Resource
    private final SystemInfo systemInfo;

    private final SubscriptionToViewFunction subscriptionToView = new SubscriptionToViewFunction ();

    public SubscriberToViewFunction (final SystemInfo systemInfo) {

        super (ISubscriber.class, SubscriberView.class);

        this.systemInfo = systemInfo;
    }

    @Override
    public SubscriberView apply (final @Nullable ISubscriber input) {

        checkNotNull (input);

        final String id = UUIDs.uuidAsStringOrDefault (input.getId ());
        final String label = input.getName ();
        final String systemId = UUIDs.uuidAsStringOrDefault (systemInfo.getId ());
        final String systemIdentifier = systemInfo.getName ();
        final SubscriptionView subscriptionView = subscriptionToView.apply (input.getSubscription ());

        final SubscriberView view = new SubscriberView (id, label, systemId, systemIdentifier, subscriptionView);
        return view;
    }
}
