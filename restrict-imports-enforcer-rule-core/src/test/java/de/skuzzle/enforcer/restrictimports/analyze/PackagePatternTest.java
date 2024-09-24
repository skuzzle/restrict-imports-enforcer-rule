package de.skuzzle.enforcer.restrictimports.analyze;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class PackagePatternTest {

    @Test
    void testMatchWildcardPrefix() {
        // See #76
        // https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/76
        assertThat(PackagePattern.parse("**.com.google.common.annotations.VisibleForTesting")
                .matches("com.couchbase.client.core.deps.com.google.common.annotations.VisibleForTesting")).isTrue();
    }

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
    void testNull() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parse(null));
    }

    @Test
    void testNull2() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parseAll(null));
    }

    @Test
    void testMisplacedDoubleWildcardInfix() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parse("foo.xyz**abc"));
    }

    @Test
    void testMisplacedSingleWildcardInfix() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parse("foo.xyz*abc"));
    }

    @Test
    void testMisplacedDoubleWildcardPrefix() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parse("foo.**abc"));
    }

    @Test
    void testMisplacedDoubleWildcardSuffix() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parse("foo.abc**"));
    }

    @Test
    void testEmptyPartInfix() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parse("foo..bar"));
    }

    @Test
    void testEmptyPartPrefix() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parse(".bar"));
    }

    @Test
    void testEmptyPartSuffix() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parse("bar."));
    }

    @Test
    void testIllegalWhitespace() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parse("com foo"));
    }

    @Test
    void testToString() {
        assertThat(PackagePattern.parse("de.skuzzle.**").toString())
                .isEqualTo("de.skuzzle.**");
    }

    @Test
    void testToStringStatic() {
        assertThat(PackagePattern.parse("static de.skuzzle.**").toString())
                .isEqualTo("static de.skuzzle.**");
    }

    @Test
    void testVerifyEquals() {
        EqualsVerifier.forClass(PackagePattern.class).verify();
    }

    @Test
    void testNotEquals() {
        assertThat(PackagePattern.parse("de.skuzzle.**")).isNotEqualTo(
                PackagePattern.parse("de.skuzzle.*"));
    }

    @Test
    void testMatchDefaultPackage() {
        final PackagePattern pattern = PackagePattern.parse("**");
        assertThat(pattern.matches("")).isTrue();
    }

    @Test
    void testMatchesDefaultPackage2() {
        final PackagePattern pattern = PackagePattern.parse("*");
        assertThat(pattern.matches("")).isTrue();
    }

    @Test
    void testMatchesDefaultPackage3() {
        final PackagePattern pattern = PackagePattern.parse("*Foo*");
        assertThat(pattern.matches("NotFooType")).isTrue();
    }

    @Test
    void testMatchExactly() {
        final PackagePattern pattern = PackagePattern.parse("de.skuzzle.SomeClass");
        assertThat(pattern.matches("de.skuzzle.SomeClass")).isTrue();
        assertThat(pattern.matches("de.skuzzle.SomeClass2")).isFalse();
    }

    @Test
    void testMatchWildCardSuffix() {
        final PackagePattern pattern = PackagePattern.parse("de.skuzzle.*");
        assertThat(pattern.matches("de.skuzzle.TestClass")).isTrue();
        assertThat(pattern.matches("de.skuzzle.TestClass2")).isTrue();
    }

    @Test
    void testWildCardMatchesSingle() {
        final PackagePattern pattern = PackagePattern.parse("de.skuzzle.*");
        assertThat(pattern.matches("de.skuzzle.sub.TestClass")).isFalse();
    }

    @Test
    void testMatchInfix() {
        final PackagePattern pattern = PackagePattern.parse("de.skuzzle.*.test");
        assertThat(pattern.matches("de.skuzzle.foo.test")).isTrue();
        assertThat(pattern.matches("de.skuzzle.test")).isFalse();
    }

    @Test
    void testMatchMultipleInfix() {
        final PackagePattern pattern = PackagePattern.parse("de.skuzzle.*.xx.*.test");
        assertThat(pattern.matches("de.skuzzle.foo.xx.bar.test")).isTrue();
        assertThat(pattern.matches("de.skuzzle.foo.xx.bar")).isFalse();
    }

    @Test
    void testWildcardMatchMultiple() {
        final PackagePattern pattern = PackagePattern.parse("de.skuzzle.**");
        assertThat(pattern.matches("de.skuzzle.sub.TestClass")).isTrue();
        assertThat(pattern.matches("de.skuzzle.TestClass2")).isTrue();
    }

    @Test
    void testLogger() {
        final PackagePattern pattern = PackagePattern.parse("java.util.**");
        assertThat(pattern.matches("java.util.logging.Logger")).isTrue();
    }

    @Test
    void testWildcardInStringToTest() {
        final PackagePattern pattern = PackagePattern.parse("java.util.ArrayList");
        assertThat(pattern.matches("java.util.*")).isFalse();
    }

    @Test
    void testDoubleWildcardInBetween() {
        final PackagePattern pattern = PackagePattern.parse("com.**.bar.ClassName");
        assertThat(pattern.matches("com.foo.bar.ClassName")).isTrue();
        assertThat(pattern.matches("com.xyz.foo.bar")).isFalse();
        assertThat(pattern.matches("com.xyz.foo.ClassName")).isFalse();
        assertThat(pattern.matches("com.bar.ClassName")).isFalse();
    }

    @Test
    void testDoubleWildcardInBetweenSkipMultiple() {
        final PackagePattern pattern = PackagePattern.parse("com.**.bar.ClassName");
        assertThat(pattern.matches("com.xyz.foo.bar.ClassName")).isTrue();
        assertThat(pattern.matches("com.xyz.foo.bar")).isFalse();
    }

    @Test
    void testConsecutiveDoubleWildcard() {
        final PackagePattern pattern = PackagePattern.parse("com.**.**.ClassName");
        assertThat(pattern.matches("com.xyz.foo.bar.ClassName")).isTrue();
        assertThat(pattern.matches("com.xyz.foo.bar")).isFalse();
    }

    @Test
    void testDoubleWildcard() {
        final PackagePattern pattern = PackagePattern.parse("com.**.xx.**.ClassName");
        assertThat(pattern.matches("com.xyz.foo.yy.xx.bar.ClassName")).isTrue();
        assertThat(pattern.matches("com.xyz.foo.bar")).isFalse();
    }

    @Test
    void testDoubleWildCartBeginning() {
        final PackagePattern pattern = PackagePattern.parse("**.ClassName");
        assertThat(pattern.matches("com.xyz.foo.bar.ClassName")).isTrue();
    }

    @Test
    void test() {
        final PackagePattern pattern = PackagePattern.parse("com.foo.**");
        assertThat(pattern.matches("java.util.ArrayList")).isFalse();
    }

    @Test
    void testPatternMatchesPattern() {
        assertThat(PackagePattern.parse("com.foo.**")
                .matches(PackagePattern.parse("com.foo.*"))).isTrue();
        assertThat(PackagePattern.parse("com.foo.*")
                .matches(PackagePattern.parse("com.foo.Class"))).isTrue();
        assertThat(PackagePattern.parse("com.foo.Class")
                .matches(PackagePattern.parse("com.foo.*"))).isFalse();
    }

    @Test
    void testStaticImport() {
        assertThat(PackagePattern.parse("static com.foo.bar.*")
                .matches("static com.foo.bar.Test")).isTrue();
    }

    @Test
    void testImplicitStaticImport() {
        assertThat(PackagePattern.parse("com.foo.bar.*")
                .matches("static com.foo.bar.Test")).isTrue();
    }

    @Test
    void testStaticImportWithWildWhitespaces() {
        assertThat(PackagePattern.parse("\n   \tstatic   \t  \n \r    com.foo.bar.*\t   ")
                .matches("       static \r  \t com.foo.bar.Test   \n       ")).isTrue();
    }

    @Test
    void testRealPackageNameStartswithStatic() {
        final PackagePattern pattern = PackagePattern.parse("staticc.foo.Bar");
        assertThat(pattern.toString()).isEqualTo("staticc.foo.Bar");
    }

    @Test
    void testParseEmptyString() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> PackagePattern.parse(""));
    }

    @Test
    void testMatchSuffixWithinLastPart() {
        final PackagePattern pattern = PackagePattern.parse("com.console.model.*Entity");
        assertThat(pattern.matches("com.console.model.UserEntity")).isTrue();
    }

    @Test
    void testMatchSuffixWithinMiddlePart() {
        final PackagePattern pattern = PackagePattern.parse("com.console.*model.UserEntity");
        assertThat(pattern.matches("com.console.whatevermodel.UserEntity")).isTrue();
    }

    @Test
    void testMatchPrefixWithinLastPart() {
        final PackagePattern pattern = PackagePattern.parse("com.console.model.Entity*");
        assertThat(pattern.matches("com.console.model.EntityWhatever")).isTrue();
    }

    @Test
    void testMatchPrefixWithinMiddlePart() {
        final PackagePattern pattern = PackagePattern.parse("com.console.model*.UserEntity");
        assertThat(pattern.matches("com.console.modelwhatever.UserEntity")).isTrue();
    }

    @Test
    void testMatchContainingStringWithinLastPart() {
        final PackagePattern pattern = PackagePattern.parse("com.console.model.*Foo*");
        assertThat(pattern.matches("com.console.model.XxFooBar")).isTrue();
    }

    @Test
    void testMatchContainingStringWithinMiddlePart() {
        final PackagePattern pattern = PackagePattern.parse("com.console.*model*.Foo");
        assertThat(pattern.matches("com.console.foomodelfoo.Foo")).isTrue();
    }

    @Test
    void testMatchContainingStringExactMatch() {
        final PackagePattern pattern = PackagePattern.parse("com.console.*model*.Foo");
        assertThat(pattern.matches("com.console.model.Foo")).isTrue();
    }

    @Test
    void testDumbNameExample() {
        final PackagePattern pattern = PackagePattern.parse("**.*DumbName");
        assertThat(pattern.matches("com.console.model.ThisIsADumbName")).isTrue();
        assertThat(pattern.matches("com.foo.bar.ThisIsADumbName")).isTrue();
    }
}
