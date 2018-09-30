package de.skuzzle.enforcer.restrictimports.analyze;

/**
 * Thrown in case a {@link BannedImportGroup} was not properly configured by the user.
 * 
 * @author Simon Taddiken
 */
public class BannedImportDefinitionException extends RuntimeException {

    private static final long serialVersionUID = 8385860286515923706L;

    BannedImportDefinitionException(String message) {
        super(message);
    }
}
