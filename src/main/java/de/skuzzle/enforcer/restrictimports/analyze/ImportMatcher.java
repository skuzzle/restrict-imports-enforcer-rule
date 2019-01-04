package de.skuzzle.enforcer.restrictimports.analyze;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects banned import matches from a single source file.
 *
 * @author Simon Taddiken
 */
class ImportMatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportMatcher.class);

    private final LineSupplier supplier;

    ImportMatcher(LineSupplier supplier) {
        this.supplier = supplier;
    }

    /**
     * Collects all imports that are banned within the given source file.
     *
     * @param sourceFile The path to a java source file to check for banned imports..
     * @param groups The groups of banned imports to check the file against. From all
     *            groups, the one with the most specific base pattern match is chosen.
     * @param lineParser A parser that understands how to identify and parse lines in this
     *            source file.
     * @return a {@link MatchedFile} holds information about the found matches. Returns an
     *         empty optional if no matches were found.
     */
    public Optional<MatchedFile> matchFile(Path sourceFile, BannedImportGroups groups, SourceLineParser lineParser) {
        LOGGER.trace("Analyzing {} for banned imports", sourceFile);

        final List<MatchedImport> matches = new ArrayList<>();
        try (final Stream<String> lines = this.supplier.lines(sourceFile)) {

            final Iterable<String> lineIt = lines.map(String::trim)::iterator;

            int row = 1;
            final String fileName = getFileName(sourceFile);

            // select group default in case this file has no package statement
            BannedImportGroup group = groups.selectGroupFor(fileName).orElse(null);

            for (final Iterator<String> it = lineIt.iterator(); it.hasNext(); ++row) {
                final String line = it.next();
                if (line.isEmpty()) {
                    continue;
                }

                final Optional<String> packageDeclaration = lineParser.parsePackage(line);
                if (packageDeclaration.isPresent()) {
                    // package ...; statement

                    // INVARIANT: our own package name occurs in the first non-empty line
                    // of the java source file (after trimming leading comments)
                    final String packageName = packageDeclaration.get();
                    final String fqcn = guessFQCN(packageName, fileName);

                    final Optional<BannedImportGroup> groupMatch = groups.selectGroupFor(fqcn);
                    if (!groupMatch.isPresent()) {
                        return Optional.empty();
                    }
                    group = groupMatch.get();
                    LOGGER.trace("    Selected group {} from fqcn {}", group, fqcn);
                    continue;
                }

                final Optional<String> importDeclaration = lineParser.parseImport(line);
                if (!importDeclaration.isPresent()) {
                    // as we are skipping empty (and comment) lines, by the time we
                    // encounter a non-import line we can stop processing this file
                    if (matches.isEmpty()) {
                        return Optional.empty();
                    }
                    return Optional.of(new MatchedFile(sourceFile, matches, group));
                }

                final String importName = importDeclaration.get();
                final int lineNumber = row;
                group.ifImportIsBanned(importName)
                        .map(bannedImport -> new MatchedImport(lineNumber, importName, bannedImport))
                        .ifPresent(matches::add);
            }

            if (matches.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new MatchedFile(sourceFile, matches, group));
        } catch (final IOException e) {
            throw new RuntimeIOException(String.format(
                    "Encountered IOException while analyzing %s for banned imports",
                    sourceFile), e);
        }
    }

    private String guessFQCN(String packageName, String sourceFileName) {
        return packageName.isEmpty()
                ? sourceFileName
                : packageName + "." + sourceFileName;
    }

    private String getFileName(Path file) {
        final String s = file.getFileName().toString();
        final int i = s.lastIndexOf(".");
        return s.substring(0, i);
    }
}
