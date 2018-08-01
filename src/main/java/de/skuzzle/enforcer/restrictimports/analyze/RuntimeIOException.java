package de.skuzzle.enforcer.restrictimports.analyze;

import java.io.IOException;

/**
 * Runtime exception replacement for {@link IOException}. Useful for throwing in methods
 * that are to be used in a Stream pipeline.
 *
 * @author Simon Taddiken
 */
class RuntimeIOException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RuntimeIOException(IOException cause) {
        super(cause);
    }
}
