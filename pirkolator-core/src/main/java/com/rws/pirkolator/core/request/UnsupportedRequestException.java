package com.rws.pirkolator.core.request;

public class UnsupportedRequestException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UnsupportedRequestException (final String string) {

        super(string);
    }
}
