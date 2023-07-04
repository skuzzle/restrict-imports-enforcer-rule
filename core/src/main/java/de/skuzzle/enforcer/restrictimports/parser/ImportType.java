package de.skuzzle.enforcer.restrictimports.parser;

/**
 * The type of a detected import.
 *
 * @author Simon Taddiken
 * @since 2.1.0
 */
public enum ImportType {
    /** Default, simple import statement. */
    IMPORT,
    /** static import statement. */
    STATIC_IMPORT,
    /** Inline, full qualified type use. */
    QUALIFIED_TYPE_USE
}
