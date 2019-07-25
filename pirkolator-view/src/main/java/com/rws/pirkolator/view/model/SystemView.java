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
package com.rws.pirkolator.view.model;

import java.util.Map;
import java.util.UUID;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;
import com.rws.utility.common.UUIDs;

@XmlRootElement
public class SystemView extends SourceView {

    private static final long serialVersionUID = 1L;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public SystemView (final String id, final String label) {

        super (id, label);
    }

    public SystemView (final UUID id, final String label) {

        this (UUIDs.toString (id), label);
    }

    public SystemView (final String id, final String label, final Map<String, String> propertyMap) {

        super (id, label, propertyMap);
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).toString ();
    }
}
