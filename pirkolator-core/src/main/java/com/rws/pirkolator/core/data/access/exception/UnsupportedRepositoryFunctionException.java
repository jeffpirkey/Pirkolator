package com.rws.pirkolator.core.data.access.exception;

public class UnsupportedRepositoryFunctionException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public UnsupportedRepositoryFunctionException () {

        super ();
    }

    public UnsupportedRepositoryFunctionException (final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {

        super (message, cause, enableSuppression, writableStackTrace);
    }

    public UnsupportedRepositoryFunctionException (final String message, final Throwable cause) {

        super (message, cause);
    }

    public UnsupportedRepositoryFunctionException (final String message) {

        super (message);
    }

    public UnsupportedRepositoryFunctionException (final Throwable cause) {

        super (cause);
    }

}
