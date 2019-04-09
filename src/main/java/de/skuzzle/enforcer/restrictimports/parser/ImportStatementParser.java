package de.skuzzle.enforcer.restrictimports.parser;

import de.skuzzle.enforcer.restrictimports.parser.lang.LanguageSupport;

import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * For parsing a source file into a {@link ParsedFile}.
 */
public interface ImportStatementParser {
    /**
     * Constructs a default instance of the parser which uses the provided charset.
     *
     * @param charset The charset to use.
     * @return The parser instance.
     */
    static ImportStatementParserImpl defaultInstance(Charset charset) {
        return new ImportStatementParserImpl(new SkipCommentsLineSupplier(charset));
    }

    /**
     * Parses the given source file using the given {@link LanguageSupport}
     * implementation to recognize import statements.
     *
     * @param sourceFilePath  The path of the file to parse.
     * @param languageSupport For parsing the import statements.
     * @return The parsed file.
     * @throws de.skuzzle.enforcer.restrictimports.io.RuntimeIOExceptionn In case reading the file fails.
     */
    ParsedFile parse(Path sourceFilePath, LanguageSupport languageSupport);
}
