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
package com.rws.pirkolator.core.data.model;

import org.junit.Test;

import com.rws.pirkolator.core.utility.serial.KryoUtility;
import com.rws.pirkolator.core.utility.serial.SerializationUtility;
import com.rws.pirkolator.model.Message;

/**
 * Performance tests of serialization, externalizable, and kryo techniques.
 * 
 * @author jpirkey
 *
 */
public class SerializationPerf {
    
    @Test
    public void perfComparison () {

        long start = System.currentTimeMillis ();
        final Message msg = new Message ();
        msg.add ("Test String");
        for (int i = 0; i < 100000; i++) {
            SerializationUtility.copyObject (msg, Message.class);
        }

        System.out.println ("Serializable Response (ms) = " + (System.currentTimeMillis () - start));

        start = System.currentTimeMillis ();
        for (int i = 0; i < 100000; i++) {
            KryoUtility.copyMessage (msg);
        }

        System.out.println ("Kryo Response (ms) = " + (System.currentTimeMillis () - start));
    }
}
