package com.rws.pirkolator.core.data.access.exception;

public class UnsupportedDaoSourceException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public UnsupportedDaoSourceException () {

        super ();
    }

    public UnsupportedDaoSourceException (final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {

        super (message, cause, enableSuppression, writableStackTrace);
    }

    public UnsupportedDaoSourceException (final String message, final Throwable cause) {

        super (message, cause);
    }

    public UnsupportedDaoSourceException (final String message) {

        super (message);
    }

    public UnsupportedDaoSourceException (final Throwable cause) {

        super (cause);
    }

}
