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
package com.rws.pirkolator.core.utility.serial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ConcurrentModificationException;

import javax.annotation.Nullable;

/**
 * This class provides utility methods for Java marshaling of objects.
 * 
 * @author pirk
 * @since 1.0.0
 */
public class SerializationUtility {

    // *************************************************************************
    // ** Copy methods
    // *************************************************************************

    @Nullable
    public static <T> T copyByBinarySerialization (final T obj, final Class<T> type) throws IOException,
            ClassNotFoundException, ConcurrentModificationException {

        final ByteArrayOutputStream outBuf = new ByteArrayOutputStream ();
        final ObjectOutputStream out = new ObjectOutputStream (outBuf);

        out.writeObject (obj);
        out.flush ();
        outBuf.flush ();

        final ByteArrayInputStream buf = new ByteArrayInputStream (outBuf.toByteArray ());
        final ObjectInputStream in = new ObjectInputStream (buf);

        final T clone = type.cast (in.readObject ());
        return clone;
    }

    @Nullable
    public static <T extends Serializable> T copyObject (final T object, final Class<T> type) {

        try {
            return SerializationUtility.copyByBinarySerialization (object, type);
        } catch (final Exception e) {
            return object;
        }
    }

    // *************************************************************************
    // ** Deserialize
    // *************************************************************************

    @Nullable
    public static <T extends Serializable> T binaryDeserialize (final byte[] bytes, final Class<T> type)
            throws IOException, ClassNotFoundException, ClassCastException {

        final ByteArrayInputStream inBuf = new ByteArrayInputStream (bytes);
        final ObjectInputStream in = new ObjectInputStream (inBuf);

        try {
            final Object obj = in.readObject ();

            if (type.isAssignableFrom (obj.getClass ())) {
                return type.cast (obj);
            }
            throw new ClassCastException ("Unable to deserialize type [expected type=" + type.toString ()
                    + "; actual type=" + obj.getClass ().toString () + "]");
        } finally {
            in.close ();
        }
    }

    @Nullable
    public static <T extends Serializable> T binaryDeserializeFromFile (final File file, final Class<T> type)
            throws IOException, ClassNotFoundException {

        final FileInputStream inBuf = new FileInputStream (file);
        final ObjectInputStream in = new ObjectInputStream (inBuf);

        try {
            final Object obj = in.readObject ();

            if (type.isAssignableFrom (obj.getClass ())) {
                return type.cast (obj);
            }
            throw new ClassCastException ("Unable to deserialize type [expected type=" + type.toString ()
                    + "; actual type=" + obj.getClass ().toString () + "]");
        } finally {
            in.close ();
        }
    }

    @Nullable
    public static <T extends Serializable> T binaryDeserializeFromFile (final String file, final Class<T> type)
            throws IOException, ClassNotFoundException {

        return SerializationUtility.binaryDeserializeFromFile (new File (file), type);
    }

    // *************************************************************************
    // ** Serialize methods
    // *************************************************************************

    @Nullable
    public static byte[] binarySerialize (final Serializable obj) throws IOException {

        final ByteArrayOutputStream outBuf = new ByteArrayOutputStream ();
        final ObjectOutputStream out = new ObjectOutputStream (outBuf);

        out.writeObject (obj);
        out.flush ();

        return outBuf.toByteArray ();
    }

    public static void binarySerializeToFile (final File file, final Serializable obj) throws IOException {

        final FileOutputStream outBuf = new FileOutputStream (file);
        final ObjectOutputStream out = new ObjectOutputStream (outBuf);

        out.writeObject (obj);
        out.flush ();
        outBuf.close ();
    }

    public static void binarySerializeToFile (final String file, final Serializable obj) throws IOException {

        SerializationUtility.binarySerializeToFile (new File (file), obj);
    }

    public static <T extends Serializable> long getObjectBinarySize (final T obj) throws IOException {

        final ByteArrayOutputStream outBuf = new ByteArrayOutputStream ();
        final ObjectOutputStream out = new ObjectOutputStream (outBuf);

        out.writeObject (obj);

        out.flush ();
        outBuf.flush ();

        return outBuf.size ();
    }
}
