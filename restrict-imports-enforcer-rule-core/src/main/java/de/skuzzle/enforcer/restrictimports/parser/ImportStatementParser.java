package de.skuzzle.enforcer.restrictimports.parser;

import java.nio.charset.Charset;
import java.nio.file.Path;

import de.skuzzle.enforcer.restrictimports.parser.lang.LanguageSupport;

/**
 * For parsing a source file into a {@link ParsedFile}.
 */
public interface ImportStatementParser {
    /**
     * Constructs a default instance of the parser which uses the provided charset.
     *
     * @param charset The charset to use.
     * @param parseFullCompilationUnit If supported, use full compilation unit parsing.
     *            Otherwise fall back to the original line based parsing.
     * @return The parser instance.
     */
    static ImportStatementParser forCharset(Charset charset, boolean parseFullCompilationUnit) {
        return new ImportStatementParserImpl(charset, parseFullCompilationUnit);
    }

    /**
     * Parses the given source file using the given {@link LanguageSupport} implementation
     * to recognize import statements.
     *
     * @param sourceFilePath The path of the file to parse.
     * @return The parsed file.
     */
    ParsedFile parse(Path sourceFilePath);
}
