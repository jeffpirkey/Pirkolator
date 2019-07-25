package com.rws.utility.common;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

public class Preconditions {

    public static <T> T notNull (@Nullable final T obj) {

        return checkNotNull (obj);
    }

    public static <T> T notNull (@Nullable final T obj, final @Nullable Object message) {

        return checkNotNull (obj, message);
    }

    public static <T> T notNull (final @Nullable T reference, @Nullable final String errorMessageTemplate,
            @Nullable final Object... errorMessageArgs) {

        return checkNotNull (reference, errorMessageTemplate, errorMessageArgs);
    }
}
