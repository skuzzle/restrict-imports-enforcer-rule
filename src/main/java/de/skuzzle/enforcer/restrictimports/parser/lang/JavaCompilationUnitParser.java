package de.skuzzle.enforcer.restrictimports.parser.lang;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
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
import de.skuzzle.enforcer.restrictimports.parser.ImportType;
import de.skuzzle.enforcer.restrictimports.parser.ParsedFile;

/**
 * Parses a full java source file using JavaParser.org dependency in order to detect
 * inline full qualified type usages.
 *
 * @author Simon Taddiken
 */
final class JavaCompilationUnitParser {

    public ParsedFile parseCompilationUnit(Path sourceFilePath, Charset charset) throws IOException {
        StaticJavaParser.getParserConfiguration().setCharacterEncoding(charset);
        StaticJavaParser.getParserConfiguration().setLexicalPreservationEnabled(false);
        StaticJavaParser.getParserConfiguration().setLanguageLevel(LanguageLevel.RAW);

        final CompilationUnit compilationUnit = StaticJavaParser.parse(sourceFilePath);

        final List<ImportStatement> imports = compilationUnit.getImports().stream()
                .map(id -> new ImportStatement(id.getNameAsString(), id.getBegin().map(p -> p.line).orElse(0),
                        id.isStatic() ? ImportType.STATIC_IMPORT : ImportType.IMPORT))
                .collect(Collectors.toList());

        // find inline full qualified type usages and report them as import
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
        return ParsedFile.successful(sourceFilePath, declaredPackage, fqcn, imports);
    }

    private int positionOf(Node node) {
        return node.getBegin().map(p -> p.line).orElse(0);
    }

    private Optional<ImportStatement> nodeToImportStatement(Node node) {
        if (node instanceof NodeWithType<?, ?>) {
            final NodeWithType<?, ?> expr = (NodeWithType<?, ?>) node;
            final Type type = expr.getType();
            if (type instanceof ClassOrInterfaceType && ((ClassOrInterfaceType) type).getScope().isPresent()) {
                final ClassOrInterfaceType classType = (ClassOrInterfaceType) expr.getType();
                return Optional.of(new ImportStatement(classType.getNameWithScope(), positionOf(node),
                        ImportType.QUALIFIED_TYPE_USE));
            }
        } else if (node instanceof MethodCallExpr) {
            // Special case for full qualified static lambda calls
            final MethodCallExpr expr = (MethodCallExpr) node;
            final FieldAccessExpr accessExpr = expr.findFirst(FieldAccessExpr.class).orElse(null);
            if (accessExpr == null || accessExpr.getScope() == null) {
                return Optional.empty();
            }

            return Optional.of(new ImportStatement(accessExpr.getScope() + "." + accessExpr.getName(),
                    positionOf(node), ImportType.QUALIFIED_TYPE_USE));
        }

        return Optional.empty();
    }

    private String getFileNameWithoutExtension(Path file) {
        final String s = file.getFileName().toString();
        final int i = s.lastIndexOf(".");
        return s.substring(0, i);
    }
}
