package de.skuzzle.enforcer.restrictimports.impl;

import java.io.IOException;


public class RuntimeIOException extends RuntimeException {

    /** */
    private static final long serialVersionUID = 1L;

    RuntimeIOException(IOException cause) {
        super(cause);
    }
}
