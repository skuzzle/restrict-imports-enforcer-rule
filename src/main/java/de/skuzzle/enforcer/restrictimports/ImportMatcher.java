package de.skuzzle.enforcer.restrictimports;

import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;


interface ImportMatcher {

    Stream<Match> matchFile(Path file, Collection<PackagePattern> bannedImports,
            Collection<PackagePattern> allowed);
}
