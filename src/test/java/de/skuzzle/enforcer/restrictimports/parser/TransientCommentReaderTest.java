package de.skuzzle.enforcer.restrictimports.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

import com.google.common.io.CharStreams;

public class TransientCommentReaderTest {

    private String readString(String in) {
        try (final Reader r = new TransientCommentReader(new StringReader(in), true)) {
            return CharStreams.toString(r);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testInlineCommentWithCrLfEnding() throws Exception {
        final String result = readString("//comment\r\ntest");
        assertThat(result).isEqualTo("\r\ntest");
    }

    @Test
    void testBlockCommentWithCrLfEnding() throws Exception {
        final String result = readString("/*comment*/\r\ntest");
        assertThat(result).isEqualTo("\r\ntest");
    }

    @Test
    public void testReadCommentOnly() {
        final String result = readString("/**/");
        assertThat(result).isEqualTo("");
    }

    @Test
    public void testReadCommentWithAsteriksContent() {
        final String result = readString("/** */");
        assertThat(result).isEqualTo("");
    }

    @Test
    public void testReadCommentAfterComment() {
        final String result = readString("/**//**/");
        assertThat(result).isEqualTo("");
    }

    @Test
    public void testReadCommentAfterCommentSeparatedByAsteriks() {
        final String result = readString("/**/*/**/");
        assertThat(result).isEqualTo("*");
    }

    @Test
    public void testReadIncompleteBeginning() {
        final String result = readString("/xy*");
        assertThat(result).isEqualTo("/xy*");
    }

    @Test
    public void testReadInfixComment() {
        final String result = readString("prefix/*comment*/suffix");
        assertThat(result).isEqualTo("prefixsuffix");
    }

    @Test
    public void testReadEos() {
        final String result = readString("/*");
        assertThat(result).isEqualTo("");
    }

    @Test
    public void testReadIncompletePrefix() {
        final String result = readString("/");
        assertThat(result).isEqualTo("/");
    }

    @Test
    public void testReadEmptyInlineComment() {
        final String result = readString("//");
        assertThat(result).isEqualTo("");
    }

    @Test
    public void testReadInlineComment() {
        final String result = readString("// c");
        assertThat(result).isEqualTo("");
    }

    @Test
    public void testReadEnclosedInlineComment() {
        final String result = readString("f\n// c\ns");
        assertThat(result).isEqualTo("f\n\ns");
    }

    @Test
    public void testSkipCommentInTickLiteral() {
        final String result = readString("'string'");
        assertThat(result).isEqualTo("'string'");
    }

    @Test
    void testAddSkippedLinesUnix() throws Exception {
        final String result = readString(
                "Just /* a block\n comment\nspanning\n3lines*/ and more");

        assertThat(result).isEqualTo("Just \n\n\n and more");
    }

    @Test
    void testAddSkippedLinesMac() throws Exception {
        final String result = readString(
                "Just /* a block\r comment\rspanning\r3lines*/ and more");

        assertThat(result).isEqualTo("Just \n\n\n and more");
    }

    @Test
    void testAddSkippedLinesWindows() throws Exception {
        final String result = readString(
                "Just /* a block\r\n comment\r\nspanning\r\n3lines*/ and more");

        assertThat(result).isEqualTo("Just \n\n\n and more");
    }

    @Test
    void testAddSkippedLinesMixed() throws Exception {
        final String result = readString(
                "Just /* a block\n comment\rspanning\r\n3lines*/ and more");

        assertThat(result).isEqualTo("Just \n\n\n and more");
    }

    @Test
    void testAsterixInBlockComment() throws Exception {
        final String result = readString("/***/abc");
        assertThat(result).isEqualTo("abc");
    }

    @Test
    void testWeirdComment() throws Exception {
        final String result = readString(
                "/** Weird block comment ///**//**/import de.skuzzle.sample.Test5;//de.skuzzle.sample.TestIgnored");
        assertThat(result).isEqualTo("import de.skuzzle.sample.Test5;");
    }
}
