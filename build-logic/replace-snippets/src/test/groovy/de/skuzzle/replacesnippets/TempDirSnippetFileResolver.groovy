package de.skuzzle.replacesnippets

import org.jetbrains.annotations.NotNull
import spock.util.io.FileSystemFixture

class TempDirSnippetFileResolver implements SnippetFileResolver {

    private final FileSystemFixture root

    TempDirSnippetFileResolver(FileSystemFixture root) {
        this.root = root
    }

    FileSystemFixture getWorkspace() {
        return root
    }

    @Override
    File resolveSnippetFile(@NotNull String path) {
        return root.resolve(path).toFile()
    }
}
