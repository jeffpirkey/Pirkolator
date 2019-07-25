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

import com.rws.pirkolator.model.Status;
import com.rws.pirkolator.model.SystemInfo;
import com.rws.pirkolator.view.model.StatusView;
import com.rws.utility.common.UUIDs;

public class StatusToViewFunction extends AbstractTransformFunction<Status, StatusView> {

    private final SystemInfo systemInfo;

    public StatusToViewFunction (final SystemInfo systemInfo) {

        super (Status.class, StatusView.class);

        this.systemInfo = systemInfo;
    }

    @Override
    public StatusView apply (final @Nullable Status input) {

        checkNotNull (input);

        final String id = UUIDs.uuidAsStringOrDefault (input.getId ());
        final String componentId = UUIDs.uuidAsStringOrDefault (input.getComponentId ());
        String tmp = input.getState ().toString ();
        // FIXME jpirkey state nullness
        if (tmp == null) {
            tmp = "UNKNOWN";
        }

        final long timestamp = input.getTimestamp ();
        final String systemId = UUIDs.uuidAsStringOrDefault (systemInfo.getId ());
        final String systemIdentifier = systemInfo.getName ();

        final StatusView view = new StatusView (id, componentId, systemId, systemIdentifier, tmp, timestamp);
        return view;
    }
}
