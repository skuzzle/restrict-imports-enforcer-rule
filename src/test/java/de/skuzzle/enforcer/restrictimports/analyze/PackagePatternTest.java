package de.skuzzle.enforcer.restrictimports.analyze;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class PackagePatternTest {

    @Test
    void testMatchLiteralAsterisk() {
        final PackagePattern pattern = PackagePattern.parse("java.util.'*'");
        assertThat(pattern.matches("java.util.*")).isTrue();
        assertThat(pattern.matches("java.util.ArrayList")).isFalse();
    }

    @Test
    void testMatchLiteralAsteriskWithWildCard() {
        final PackagePattern pattern = PackagePattern.parse("**.'*'");
        assertThat(pattern.matches("java.util.*")).isTrue();
        assertThat(pattern.matches("java.util.ArrayList")).isFalse();
    }

    @Test
    public void testNull() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parse(null));
    }

    @Test
    public void testNull2() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parseAll(null));
    }

    @Test
    public void testMisplacedDoubleWildcardInfix() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parse("foo.xyz**abc"));
    }

    @Test
    public void testMisplacedSingleWildcardInfix() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parse("foo.xyz*abc"));
    }

    @Test
    public void testMisplacedDoubleWildcardPrefix() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parse("foo.**abc"));
    }

    @Test
    public void testMisplacedDoubleWildcardSuffix() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parse("foo.abc**"));
    }

    @Test
    public void testMisplacedSingleWildcardPrefix() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parse("foo.*abc"));
    }

    @Test
    public void testMisplacedSingleWildcardSuffix() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parse("foo.abc*"));
    }

    @Test
    public void testEmptyPartInfix() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parse("foo..bar"));
    }

    @Test
    public void testEmptyPartPrefix() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parse(".bar"));
    }

    @Test
    public void testEmptyPartSuffix() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parse("bar."));
    }

    @Test
    public void testIllegalWhitespace() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parse("com foo"));
    }

    @Test
    public void testToString() throws Exception {
        assertThat(PackagePattern.parse("de.skuzzle.**").toString())
                .isEqualTo("de.skuzzle.**");
    }

    @Test
    public void testToStringStatic() throws Exception {
        assertThat(PackagePattern.parse("static de.skuzzle.**").toString())
                .isEqualTo("static de.skuzzle.**");
    }

    @Test
    public void testVerifyEquals() throws Exception {
        EqualsVerifier.forClass(PackagePattern.class).verify();
    }

    @Test
    public void testNotEquals() throws Exception {
        assertThat(PackagePattern.parse("de.skuzzle.**")).isNotEqualTo(
                PackagePattern.parse("de.skuzzle.*"));
    }

    @Test
    public void testMatchDefaultPackage() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("**");
        assertThat(pattern.matches("")).isTrue();
    }

    @Test
    public void testMatchesDefaultPackage2() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("*");
        assertThat(pattern.matches("")).isTrue();
    }

    @Test
    public void testMatchExactly() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("de.skuzzle.SomeClass");
        assertThat(pattern.matches("de.skuzzle.SomeClass")).isTrue();
        assertThat(pattern.matches("de.skuzzle.SomeClass2")).isFalse();
    }

    @Test
    public void testMatchWildCardSuffix() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("de.skuzzle.*");
        assertThat(pattern.matches("de.skuzzle.TestClass")).isTrue();
        assertThat(pattern.matches("de.skuzzle.TestClass2")).isTrue();
    }

    @Test
    public void testWildCardMatchesSingle() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("de.skuzzle.*");
        assertThat(pattern.matches("de.skuzzle.sub.TestClass")).isFalse();
    }

    @Test
    public void testMatchInfix() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("de.skuzzle.*.test");
        assertThat(pattern.matches("de.skuzzle.foo.test")).isTrue();
        assertThat(pattern.matches("de.skuzzle.test")).isFalse();
    }

    @Test
    public void testMatchMultipleInfix() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("de.skuzzle.*.xx.*.test");
        assertThat(pattern.matches("de.skuzzle.foo.xx.bar.test")).isTrue();
        assertThat(pattern.matches("de.skuzzle.foo.xx.bar")).isFalse();
    }

    @Test
    public void testWildcardMatchMultiple() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("de.skuzzle.**");
        assertThat(pattern.matches("de.skuzzle.sub.TestClass")).isTrue();
        assertThat(pattern.matches("de.skuzzle.TestClass2")).isTrue();
    }

    @Test
    public void testLogger() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("java.util.**");
        assertThat(pattern.matches("java.util.logging.Logger")).isTrue();
    }

    @Test
    public void testWildcardInStringToTest() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("java.util.ArrayList");
        assertThat(pattern.matches("java.util.*")).isFalse();
    }

    @Test
    public void testDoubleWildcardInBetween() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("com.**.bar.ClassName");
        assertThat(pattern.matches("com.foo.bar.ClassName")).isTrue();
        assertThat(pattern.matches("com.xyz.foo.bar")).isFalse();
        assertThat(pattern.matches("com.xyz.foo.ClassName")).isFalse();
        assertThat(pattern.matches("com.bar.ClassName")).isFalse();
    }

    @Test
    public void testDoubleWildcardInBetweenSkipMultiple() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("com.**.bar.ClassName");
        assertThat(pattern.matches("com.xyz.foo.bar.ClassName")).isTrue();
        assertThat(pattern.matches("com.xyz.foo.bar")).isFalse();
    }

    @Test
    public void testConsecutiveDoubleWildcard() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("com.**.**.ClassName");
        assertThat(pattern.matches("com.xyz.foo.bar.ClassName")).isTrue();
        assertThat(pattern.matches("com.xyz.foo.bar")).isFalse();
    }

    @Test
    public void testDoubleWildcard() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("com.**.xx.**.ClassName");
        assertThat(pattern.matches("com.xyz.foo.yy.xx.bar.ClassName")).isTrue();
        assertThat(pattern.matches("com.xyz.foo.bar")).isFalse();
    }

    @Test
    public void testDoubleWildCartBeginning() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("**.ClassName");
        assertThat(pattern.matches("com.xyz.foo.bar.ClassName")).isTrue();
    }

    @Test
    public void test() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("com.foo.**");
        assertThat(pattern.matches("java.util.ArrayList")).isFalse();
    }

    @Test
    public void testPatternMatchesPattern() throws Exception {
        assertThat(PackagePattern.parse("com.foo.**")
                .matches(PackagePattern.parse("com.foo.*"))).isTrue();
        assertThat(PackagePattern.parse("com.foo.*")
                .matches(PackagePattern.parse("com.foo.Class"))).isTrue();
        assertThat(PackagePattern.parse("com.foo.Class")
                .matches(PackagePattern.parse("com.foo.*"))).isFalse();
    }

    @Test
    public void testStaticImport() throws Exception {
        assertThat(PackagePattern.parse("static com.foo.bar.*")
                .matches("static com.foo.bar.Test")).isTrue();
    }

    @Test
    public void testStaticImportWithWildWhitespaces() throws Exception {
        assertThat(PackagePattern.parse("\n   \tstatic   \t  \n \r    com.foo.bar.*\t   ")
                .matches("       static \r  \t com.foo.bar.Test   \n       ")).isTrue();
    }

    @Test
    public void testRealPackageNameStartswithStatic() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("staticc.foo.Bar");
        assertThat(pattern.toString()).isEqualTo("staticc.foo.Bar");
    }

    @Test
    public void testParseEmptyString() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parse(""));
    }
}
