package com.rws.pirkolator.core.store.exception;


public class UnavailableStoreException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UnavailableStoreException () {

        super ();
    }

    public UnavailableStoreException (final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {

        super (message, cause, enableSuppression, writableStackTrace);
    }

    public UnavailableStoreException (final String message, final Throwable cause) {

        super (message, cause);
    }

    public UnavailableStoreException (final String message) {

        super (message);
    }

    public UnavailableStoreException (final Throwable cause) {

        super (cause);
    }
}
