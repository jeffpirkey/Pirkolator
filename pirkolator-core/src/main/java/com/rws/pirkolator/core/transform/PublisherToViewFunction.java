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

import com.rws.pirkolator.core.engine.IPublisher;
import com.rws.pirkolator.model.SystemInfo;
import com.rws.pirkolator.view.model.PublisherView;
import com.rws.utility.common.UUIDs;

public class PublisherToViewFunction extends AbstractTransformFunction<IPublisher, PublisherView> {

    @Resource
    private final SystemInfo systemInfo;

    // TODO jpirkey This should be grabbed from the SPring context/TransformLibrary
    private final PublicationToViewFunction publicationToView = new PublicationToViewFunction ();

    public PublisherToViewFunction (final SystemInfo systemInfo) {

        super (IPublisher.class, PublisherView.class);

        this.systemInfo = systemInfo;
    }

    @Override
    public PublisherView apply (final @Nullable IPublisher input) {

        checkNotNull (input);

        final PublisherView view =
                new PublisherView (UUIDs.uuidAsStringOrDefault (input.getId ()), input.getName (),
                        UUIDs.uuidAsStringOrDefault (systemInfo.getId ()), systemInfo.getName (),
                        publicationToView.apply (input.getPublication ()));
        return view;
    }
}
