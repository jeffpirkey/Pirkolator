package com.rws.utility.common;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.ObjectInput;


public class Externalizables {

    public static <T> T readObjectAs (final ObjectInput in, final Class<T> type) throws ClassNotFoundException, IOException {
        final T tmp = type.cast (in.readObject ());
        return checkNotNull (tmp);
    }
    
    public static String readUTF (final ObjectInput in) throws IOException {
        
        final String tmp = in.readUTF ();
        return checkNotNull (tmp);
    }
}
