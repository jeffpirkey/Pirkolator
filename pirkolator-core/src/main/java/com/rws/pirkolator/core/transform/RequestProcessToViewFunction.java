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
package com.rws.pirkolator.core.transform;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import com.rws.pirkolator.core.request.RequestProcess;
import com.rws.pirkolator.model.request.Request;
import com.rws.pirkolator.model.request.RequestStats;
import com.rws.pirkolator.view.model.RequestView;

public class RequestProcessToViewFunction extends AbstractTransformFunction<RequestProcess, RequestView> {

    public RequestProcessToViewFunction () {

        super (RequestProcess.class, RequestView.class);
    }

    @Override
    public RequestView apply (final @Nullable RequestProcess entity) {

        checkNotNull (entity);

        final Request request = entity.getRequest ();
        final RequestStats stats = entity.getStats ();

        String state = entity.getState ().toString ();
        if (state == null) {
            // TODO jpirkey fix this so it matches the actual state
            state = "UNKNOWN";
        }

        final RequestView view =
                new RequestView (request.getId (), "request", request.getRequestType (), request.getRequesterId (),
                        state, stats.getTimeSubmitted (), stats.getTimeCompleted (), stats.getCurrentProgress (),
                        stats.getTotalProgress (), request.getParameterList ());

        return view;
    }
}
