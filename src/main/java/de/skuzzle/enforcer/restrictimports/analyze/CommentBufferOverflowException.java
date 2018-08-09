package de.skuzzle.enforcer.restrictimports.analyze;

/**
 * Thrown if the {@link TransientCommentReader}'s comment line buffer is too small.
 *
 * @author Simon Taddiken
 */
public class CommentBufferOverflowException extends RuntimeException {

    private static final long serialVersionUID = -3642604269476610810L;

    CommentBufferOverflowException(String message) {
        super(message);
    }
}
