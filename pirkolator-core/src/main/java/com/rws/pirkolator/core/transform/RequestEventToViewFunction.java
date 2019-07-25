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

import com.rws.pirkolator.model.request.Request;
import com.rws.pirkolator.model.request.RequestEvent;
import com.rws.pirkolator.model.request.RequestStats;
import com.rws.pirkolator.view.model.RequestView;

public class RequestEventToViewFunction extends AbstractTransformFunction<RequestEvent, RequestView> {

    public RequestEventToViewFunction () {

        super (RequestEvent.class, RequestView.class);
    }

    @Override
    public RequestView apply (final @Nullable RequestEvent event) {

        checkNotNull (event);

        final Request request = event.getRequest ();
        final RequestStats stats = event.getStats ();

        String state = event.getState ().toString ();
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
