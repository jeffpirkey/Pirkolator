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
package com.rws.pirkolator.core.engine;

import static com.rws.utility.common.Preconditions.notNull;

import com.rws.pirkolator.model.AbstractLabeledIdentifiable;
import com.rws.utility.common.UUIDs;

public class StringData extends AbstractLabeledIdentifiable {

    private static final long serialVersionUID = 1L;
    
    String text;

    public StringData () {

        super (UUIDs.defaultUUIDAsString (), "Spring Data");
        
        text = "X";
    }

    public StringData (final int size) {

        super (UUIDs.defaultUUIDAsString (), "Spring Data");

        final StringBuilder tmp = new StringBuilder ();
        for (int i = 0; i < size; i++) {
            tmp.append ("X");
        }
        text = notNull(tmp.toString ());
    }
}
