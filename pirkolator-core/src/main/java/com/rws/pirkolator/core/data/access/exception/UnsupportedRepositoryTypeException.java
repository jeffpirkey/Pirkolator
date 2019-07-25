package com.rws.pirkolator.core.data.access.exception;

public class UnsupportedRepositoryTypeException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public UnsupportedRepositoryTypeException () {

        super ();
    }

    public UnsupportedRepositoryTypeException (final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {

        super (message, cause, enableSuppression, writableStackTrace);
    }

    public UnsupportedRepositoryTypeException (final String message, final Throwable cause) {

        super (message, cause);
    }

    public UnsupportedRepositoryTypeException (final String message) {

        super (message);
    }

    public UnsupportedRepositoryTypeException (final Throwable cause) {

        super (cause);
    }

}
