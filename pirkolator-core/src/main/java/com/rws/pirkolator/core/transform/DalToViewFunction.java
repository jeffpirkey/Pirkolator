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

import com.rws.pirkolator.core.data.access.IDao;
import com.rws.pirkolator.model.SystemInfo;
import com.rws.pirkolator.view.model.DalView;
import com.rws.utility.common.UUIDs;

public class DalToViewFunction extends AbstractTransformFunction<IDao, DalView> {

    private final SystemInfo systemInfo;

    public DalToViewFunction (final SystemInfo systemInfo) {

        super (IDao.class, DalView.class);

        this.systemInfo = systemInfo;
    }

    @Override
    public DalView apply (final @Nullable IDao input) {

        checkNotNull (input);

        final DalView view =
                new DalView (input.getSource ().getId (), input.getSource ().getLabel (), Boolean.parseBoolean (input
                        .getMetadata ().getMap ().get ("correlation-enabled")), UUIDs.uuidAsStringOrDefault (input
                        .getId ()), UUIDs.uuidAsStringOrDefault (systemInfo.getId ()),
                        systemInfo.getName ());

        return view;
    }
}
