package com.rws.pirkolator.core.engine.exception;

public class PublicationNotFoundException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public PublicationNotFoundException () {

        super ();
    }

    public PublicationNotFoundException (final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {

        super (message, cause, enableSuppression, writableStackTrace);
    }

    public PublicationNotFoundException (final String message, final Throwable cause) {

        super (message, cause);
    }

    public PublicationNotFoundException (final String message) {

        super (message);
    }

    public PublicationNotFoundException (final Throwable cause) {

        super (cause);
    }

}
