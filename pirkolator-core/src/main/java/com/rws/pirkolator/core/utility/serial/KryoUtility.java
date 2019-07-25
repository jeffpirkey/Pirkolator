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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.rws.utility.common.Preconditions.notNull;

import java.util.UUID;

import javax.annotation.Nullable;

import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Kryo.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.rws.pirkolator.model.Message;

/**
 * This class provides Kryo support for marshaling data in {@link Message} instances.
 * 
 * @author jpirkey
 *
 */
public class KryoUtility {

    static Output OUTPUT;
    static Input INPUT;
    public static Kryo KRYO = new Kryo ();

    static {
        KRYO.setRegistrationRequired (false);
        KRYO.setInstantiatorStrategy (new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        KRYO.register (Message.class, new FieldSerializer<Message> (KRYO, Message.class));
        KRYO.register (UUID.class, new UUIDSerializer ());

        OUTPUT = new Output (16 * 1024, 128 * 1024);
        INPUT = new Input (128 * 1024);
    }

    public static synchronized Message copyMessage (final Message message) {

        return notNull (KRYO.copy (message));
    }

    public static synchronized @Nullable
    byte[] writeMessage (final Message message) {

        KRYO.writeObject (OUTPUT, message);
        return OUTPUT.getBuffer ();
    }

    public static synchronized @Nullable
    Message readBytesIntoMessage (final byte[] bytes) {

        INPUT.setBuffer (bytes);
        //INPUT.read (bytes);
        return KRYO.readObject (INPUT, Message.class);
    }

    public static <T> void registerSerializer (final Class<T> type, final Serializer<T> serializer) {

        KRYO.register (type, serializer);
    }

    public static synchronized @Nullable
    byte[] writeBytes (final Message message) {

        KRYO.writeObject (OUTPUT, message);
        return OUTPUT.getBuffer ();
    }

    private static class UUIDSerializer extends Serializer<UUID> {

        public UUIDSerializer () {

        }

        @Override
        public @Nullable
        UUID read (final @Nullable Kryo arg0, final @Nullable Input arg1, final @Nullable Class<UUID> arg2) {

            checkNotNull (arg1);
            return UUID.fromString (arg1.readString ());
        }

        @Override
        public void write (final @Nullable Kryo arg0, final @Nullable Output arg1, final @Nullable UUID arg2) {

            checkNotNull (arg1);
            checkNotNull (arg2);
            arg1.writeString (arg2.toString ());
        }

        @Override
        public @Nullable
        UUID copy (final @Nullable Kryo kryo, final @Nullable UUID original) {

            checkNotNull (original);
            return UUID.fromString (original.toString ());
        }

    }
}
