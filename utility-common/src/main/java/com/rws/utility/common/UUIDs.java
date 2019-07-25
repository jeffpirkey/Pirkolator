package com.rws.utility.common;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.UUID;

import javax.annotation.Nullable;

public class UUIDs {

    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-0000000000000";

    public static boolean uuidAsStringNotNullOrEmpty (final UUID id) {

        final String tmpId = id.toString ();
        return tmpId != null && !tmpId.isEmpty ();
    }

    public static String uuidAsStringOrDefault (final UUID id) {

        final String tmp = id.toString ();
        if (tmp != null) {
            return tmp;
        }

        return UNKNOWN_ID;
    }

    public static UUID defaultUUID () {

        return fromString (UNKNOWN_ID);
    }

    public static String defaultUUIDAsString () {

        return UNKNOWN_ID;
    }

    public static UUID generateUUID () {

        final UUID tmp = UUID.randomUUID ();
        return checkNotNull (tmp);
    }

    public static String generateUUIDAsString () {

        final String tmp = UUID.randomUUID ().toString();
        return checkNotNull (tmp);
    }
    
    public static UUID fromString (final String uuid) {

        final UUID tmp = UUID.fromString (uuid);
        return checkNotNull (tmp);
    }

    public static String toString (final UUID id) {

        final String tmp = id.toString ();
        return checkNotNull (tmp);
    }

    public static boolean isUnknownId (final @Nullable Object id) {

        return fromString (UNKNOWN_ID).equals (id);
    }

    public static boolean isUnknownIdAsString (final @Nullable Object id) {

        return UNKNOWN_ID.equals (id);
    }
}
