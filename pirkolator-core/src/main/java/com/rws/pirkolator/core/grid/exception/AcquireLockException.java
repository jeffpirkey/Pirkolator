package com.rws.pirkolator.core.grid.exception;

public class AcquireLockException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public AcquireLockException () {

        super ();
    }

    public AcquireLockException (final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {

        super (message, cause, enableSuppression, writableStackTrace);
    }

    public AcquireLockException (final String message, final Throwable cause) {

        super (message, cause);
    }

    public AcquireLockException (final String message) {

        super (message);
    }

    public AcquireLockException (final Throwable cause) {

        super (cause);
    }

}
