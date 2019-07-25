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
package com.rws.pirkolator.core.utility.serial;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.rws.utility.common.Preconditions.notNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rws.utility.common.Externalizables;
import com.rws.utility.common.Globals;

public class MarshallingPerformance {

    private final static Logger LOG = notNull (LoggerFactory.getLogger (MarshallingPerformance.class));

    @Test
    public void perfSerializeExternalize () throws Exception {

        final TestSerial serial = new TestSerial ();
        serial.setName ("Test serial");
        final List<String> tmp = new ArrayList<> ();
        tmp.add ("Item 1");
        tmp.add ("Item 2");
        serial.setList (tmp);

        final long serialStartTime = System.currentTimeMillis ();
        for (int i = 0; i < 1000000; i++) {
            SerializationUtility.copyObject (serial, TestSerial.class);
        }
        final long serialFinishTime = System.currentTimeMillis ();

        final TestExternal external = new TestExternal ();
        external.setName ("Test extern");
        external.setList (tmp);

        final long externalStartTime = System.currentTimeMillis ();
        for (int i = 0; i < 1000000; i++) {
            SerializationUtility.copyObject (external, TestExternal.class);
        }
        final long externalFinishTime = System.currentTimeMillis ();

        final TestExternal2 external2 = new TestExternal2 ();
        external.setName ("Test extern via List cast");
        external.setList (tmp);

        final long externalStartTime2 = System.currentTimeMillis ();
        for (int i = 0; i < 1000000; i++) {
            SerializationUtility.copyObject (external2, TestExternal2.class);
        }
        final long externalFinishTime2 = System.currentTimeMillis ();

        LOG.info ("Serial time = {}ms{}External time = {}ms{}External via cast = {}ms", serialFinishTime
                - serialStartTime, Globals.NEW_LINE, externalFinishTime - externalStartTime, Globals.NEW_LINE,
                externalFinishTime2 - externalStartTime2);
    }

    class TestSerial implements Serializable {

        private static final long serialVersionUID = 1L;

        private String name;
        private final List<String> list = new ArrayList<> ();

        public TestSerial () {

            super ();
            name = "";
        }

        public String getName () {

            return name;
        }

        public void setName (final String name) {

            this.name = name;
        }

        public List<String> getList () {

            return list;
        }

        public void setList (final List<String> list) {

            this.list.clear ();
            this.list.addAll (list);
        }
    }

    class TestExternal implements Externalizable {

        private String name;
        private final List<String> list = new ArrayList<> ();

        public TestExternal () {

            super ();
            name = "";
        }

        public String getName () {

            return name;
        }

        public void setName (final String name) {

            this.name = name;
        }

        public List<String> getList () {

            return list;
        }

        public void setList (final List<String> list) {

            this.list.clear ();
            this.list.addAll (list);
        }

        @Override
        public void writeExternal (final @Nullable ObjectOutput out) throws IOException {

            checkNotNull (out);

            out.writeUTF (name);

            out.writeInt (list.size ());
            for (final String obj : list) {
                out.writeUTF (obj);
            }
        }

        @Override
        public void readExternal (final @Nullable ObjectInput in) throws IOException, ClassNotFoundException {

            checkNotNull (in);

            name = Externalizables.readUTF (in);
            final int size = in.readInt ();
            list.clear ();
            for (int i = 0; i < size; i++) {
                list.add (in.readUTF ());
            }
        }
    }

    class TestExternal2 implements Externalizable {

        private String name;
        private List<String> list = new ArrayList<> ();

        public TestExternal2 () {

            super ();
            name = "";
        }

        public String getName () {

            return name;
        }

        public void setName (final String name) {

            this.name = name;
        }

        public List<String> getList () {

            return list;
        }

        public void setList (final List<String> list) {

            this.list.clear ();
            this.list.addAll (list);
        }

        @Override
        public void writeExternal (final @Nullable ObjectOutput out) throws IOException {

            checkNotNull (out);

            out.writeUTF (name);
            out.writeObject (list);
        }

        @SuppressWarnings ("unchecked")
        @Override
        public void readExternal (final @Nullable ObjectInput in) throws IOException, ClassNotFoundException {

            checkNotNull (in);
            
            name = Externalizables.readUTF (in);
            list = (List<String>) notNull (in.readObject ());
        }
    }
}
