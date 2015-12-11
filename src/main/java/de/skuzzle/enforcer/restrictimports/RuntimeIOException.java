package de.skuzzle.enforcer.restrictimports;

import java.io.IOException;


class RuntimeIOException extends RuntimeException {

    /** */
    private static final long serialVersionUID = 1L;

    RuntimeIOException(IOException cause) {
        super(cause);
    }
}
