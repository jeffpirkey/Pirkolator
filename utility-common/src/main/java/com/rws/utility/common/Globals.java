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
package com.rws.utility.common;

import static com.rws.utility.common.Preconditions.notNull;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * @author pirk
 */
public class Globals {

    public final static transient String DASH_LINE = System.getProperty ("line.separator")
            + "--------------------------------------------------------------------------------"
            + System.getProperty ("line.separator");

    public final static transient String STATUS_SEPARATOR =
            ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>";

    /**
     * System independent new line character.
     */
    public final static transient String NEW_LINE = notNull (System.getProperty ("line.separator"));

    public final static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat ("EEE, d MMM yyyy HH:mm:ss.SSS");

    public final static DecimalFormat DECIMAL_FORMATTER_2 = new DecimalFormat ("#.##");
}
