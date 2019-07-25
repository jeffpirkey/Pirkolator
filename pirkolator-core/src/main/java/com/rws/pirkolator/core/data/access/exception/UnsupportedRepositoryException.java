package com.rws.pirkolator.core.data.access.exception;

public class UnsupportedRepositoryException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public UnsupportedRepositoryException () {

        super ();
    }

    public UnsupportedRepositoryException (final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {

        super (message, cause, enableSuppression, writableStackTrace);
    }

    public UnsupportedRepositoryException (final String message, final Throwable cause) {

        super (message, cause);
    }

    public UnsupportedRepositoryException (final String message) {

        super (message);
    }

    public UnsupportedRepositoryException (final Throwable cause) {

        super (cause);
    }

}
