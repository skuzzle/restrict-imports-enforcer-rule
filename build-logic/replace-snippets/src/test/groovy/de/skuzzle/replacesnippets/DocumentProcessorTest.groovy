package de.skuzzle.replacesnippets

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

class DocumentProcessorTest extends Specification {

    @TempDir
    FileSystemFixture workspace;
    @Subject
    DocumentProcessor documentProcessor

    def setup() {
        documentProcessor = new DocumentProcessor(new TempDirSnippetFileResolver(workspace))
    }

    def "should replace additional tokens"() {
        given:
        def document = "@project.name@:@project.version@"

        when:
        def result = documentProcessor.process(document, ['project.name': 'restrict-imports', 'project.version': '2.5'])

        then:
        result == "restrict-imports:2.5"
    }

    def "should collect multiple occurrences of a single snippet from the same file"() {
        given:
        workspace.file("snippets/snippets.java") << """\
        // [snippet]
        import com.foo.bar.*;
        
        // [/snippet]
        
        // [unrelated-snippet]
        // Some text and stuff
        // [/unrelated-snippet]
        
        
        // [snippet]
        public void foo() {
        }
        // [/snippet]
        """.stripIndent(true)

        and:
        def document = "[!snippet in snippets/snippets.java]"

        when:
        def result = documentProcessor.process(document, [:])

        then:
        result == """\
        import com.foo.bar.*;
        
        public void foo() {
        }""".stripIndent(true)
    }

    def "should replace empty snippet"() {
        given:
        workspace.file("snippets/snippets.txt") << """\
        // [empty-snippet1]
        // [/empty-snippet1]
        // [empty-snippet2][/empty-snippet2]
        """.stripIndent(true)

        and:
        def document = "[!empty-snippet1 in snippets/snippets.txt][!empty-snippet2 in snippets/snippets.txt]"
        when:
        def result = documentProcessor.process(document, [:])

        then:
        result.empty
    }

    def "should be able to read multiple snippets from same file"() {
        given:
        workspace.file("snippets/snippets.txt") << """\
        // [snippet1]
        
        Multi line
        snippet
        // [/snippet1]
        
        [snippet2]
        Single line snippet
        
        [/snippet2]
        
        """.stripIndent(true)

        and:
        def document = """\
        [!snippet1 in snippets/snippets.txt]
        [!snippet2 in snippets/snippets.txt]
        """.stripIndent(true)

        when:
        def result = documentProcessor.process(document, [:])

        then:
        result == """\

        Multi line
        snippet
        Single line snippet
        
        """.stripIndent(true)
    }
}
