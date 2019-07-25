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

import com.rws.pirkolator.core.analytic.IAnalytic;
import com.rws.pirkolator.model.SystemInfo;
import com.rws.pirkolator.view.model.AnalyticView;
import com.rws.utility.common.UUIDs;

public class AnalyticToViewFunction extends AbstractTransformFunction<IAnalytic, AnalyticView> {

    private final SystemInfo systemInfo;

    public AnalyticToViewFunction (final SystemInfo systemInfo) {

        super (IAnalytic.class, AnalyticView.class);

        this.systemInfo = systemInfo;
    }

    @Override
    public AnalyticView apply (final @Nullable IAnalytic input) {

        checkNotNull (input);
        
        final AnalyticView view =
                new AnalyticView (UUIDs.uuidAsStringOrDefault (input.getId ()), input.getName (),
                        UUIDs.uuidAsStringOrDefault (systemInfo.getId ()), systemInfo.getName ());

        return view;
    }
}
