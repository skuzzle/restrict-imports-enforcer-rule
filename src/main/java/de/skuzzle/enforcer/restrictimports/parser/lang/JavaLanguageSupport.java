package de.skuzzle.enforcer.restrictimports.parser.lang;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import de.skuzzle.enforcer.restrictimports.parser.ImportStatement;
import de.skuzzle.enforcer.restrictimports.parser.ParsedFile;
import de.skuzzle.enforcer.restrictimports.util.Whitespaces;

public class JavaLanguageSupport implements LanguageSupport {

    private static final String STATIC_PREFIX = "static";
    private static final String IMPORT_STATEMENT = "import ";
    private static final String PACKAGE_STATEMENT = "package ";

    @Override
    public Set<String> getSupportedFileExtensions() {
        return Collections.singleton("java");
    }

    @Override
    public boolean parseFullCompilationUnitSupported() {
        return true;
    }

    @Override
    public ParsedFile parseCompilationUnit(Path sourceFilePath, Charset charset) throws IOException {
        StaticJavaParser.getParserConfiguration().setCharacterEncoding(charset);
        StaticJavaParser.getParserConfiguration().setLexicalPreservationEnabled(false);
        StaticJavaParser.getParserConfiguration().setLanguageLevel(LanguageLevel.RAW);

        final CompilationUnit compilationUnit = StaticJavaParser.parse(sourceFilePath);

        final List<ImportStatement> imports = compilationUnit.getImports().stream()
                .map(id -> new ImportStatement(id.getNameAsString(), id.getBegin().map(p -> p.line).orElse(0),
                        id.isStatic()))
                .collect(Collectors.toList());

        compilationUnit.stream()
                .map(this::nodeToImportStatement)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(imports::add);

        final String fileName = getFileNameWithoutExtension(sourceFilePath);
        final String declaredPackage = compilationUnit.getPackageDeclaration().map(pd -> pd.getNameAsString())
                .orElse("");
        final String primaryTypeName = compilationUnit.getPrimaryTypeName().orElse(fileName);
        final String fqcn = declaredPackage.isEmpty() ? primaryTypeName : declaredPackage + "." + primaryTypeName;
        return new ParsedFile(sourceFilePath, declaredPackage, fqcn, imports);
    }

    private Optional<ImportStatement> nodeToImportStatement(Node node) {
        if (node instanceof NodeWithType<?, ?>) {
            final NodeWithType<?, ?> expr = (NodeWithType<?, ?>) node;
            if (isQualifiedTypeUse(expr.getType())) {
                return Optional.of(new ImportStatement(node.toString(), node.getBegin().map(p -> p.line).orElse(0),
                        false));
            }
        } else if (node instanceof MethodCallExpr) {
            final MethodCallExpr expr = (MethodCallExpr) node;
            final FieldAccessExpr accessExpr = expr.findFirst(FieldAccessExpr.class).orElse(null);
            if (accessExpr == null || accessExpr.getScope() == null) {
                return Optional.empty();
            }

            return Optional.of(new ImportStatement(accessExpr.getScope() + "." + accessExpr.getName(),
                    node.getBegin().map(p -> p.line).orElse(0),
                    false));
        }

        return Optional.empty();
    }

    private boolean isQualifiedTypeUse(Type node) {
        return node instanceof ClassOrInterfaceType && ((ClassOrInterfaceType) node).getScope().isPresent();
    }

    private String getFileNameWithoutExtension(Path file) {
        final String s = file.getFileName().toString();
        final int i = s.lastIndexOf(".");
        return s.substring(0, i);
    }

    @Override
    public Optional<String> parsePackage(String line) {
        if (!isPackage(line)) {
            return Optional.empty();
        }

        return Optional.of(extractPackageName(line));
    }

    @Override
    public List<ImportStatement> parseImport(String line, int lineNumber) {
        if (!isImport(line)) {
            return Collections.emptyList();
        }

        // There can be multiple import statements within the same line, so
        // we simply split them at their ';'
        final String[] parts = line.split(";");
        return Arrays.stream(parts)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.substring(IMPORT_STATEMENT.length()))
                .map(String::trim)
                .map(importName -> toImportStatement(importName, lineNumber))
                .collect(Collectors.toList());
    }

    private ImportStatement toImportStatement(String importName, int lineNumber) {
        if (importName.startsWith(STATIC_PREFIX)) {
            final String realImportName = Whitespaces.trimAll(importName.substring(STATIC_PREFIX.length()));
            return new ImportStatement(realImportName, lineNumber, true);
        }
        return new ImportStatement(importName, lineNumber, false);
    }

    private boolean is(String compare, String line) {
        return line.startsWith(compare) && line.endsWith(";");
    }

    private boolean isPackage(String line) {
        return is(PACKAGE_STATEMENT, line);
    }

    private boolean isImport(String line) {
        return is(IMPORT_STATEMENT, line);
    }

    private static String extractPackageName(String line) {
        final int spaceIdx = line.indexOf(" ");
        final int semiIdx = line.indexOf(";");
        final String sub = line.substring(spaceIdx, semiIdx);
        return sub.trim();
    }

}
