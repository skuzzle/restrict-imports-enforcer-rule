package de.skuzzle.enforcer.restrictimports.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.skuzzle.enforcer.restrictimports.model.PackagePattern;
import nl.jqno.equalsverifier.EqualsVerifier;

public class PackagePatternImplTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull() throws Exception {
        PackagePattern.parse(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull2() throws Exception {
        PackagePattern.parseAll(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMisplacedDoubleWildcardInfix() throws Exception {
        PackagePattern.parse("foo.xyz**abc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMisplacedSingleWildcardInfix() throws Exception {
        PackagePattern.parse("foo.xyz*abc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMisplacedDoubleWildcardPrefix() throws Exception {
        PackagePattern.parse("foo.**abc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMisplacedDoubleWildcardSuffix() throws Exception {
        PackagePattern.parse("foo.abc**");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMisplacedSingleWildcardPrefix() throws Exception {
        PackagePattern.parse("foo.*abc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMisplacedSingleWildcardSuffix() throws Exception {
        PackagePattern.parse("foo.abc*");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyPartInfix() throws Exception {
        PackagePattern.parse("foo..bar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyPartPrefix() throws Exception {
        PackagePattern.parse(".bar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyPartSuffix() throws Exception {
        PackagePattern.parse("bar.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalWhitespace() throws Exception {
        PackagePattern.parse("com foo");
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("de.skuzzle.**", PackagePattern.parse("de.skuzzle.**").toString());
    }

    @Test
    public void testToStringStatic() throws Exception {
        assertEquals("static de.skuzzle.**",
                PackagePattern.parse("static de.skuzzle.**").toString());
    }

    @Test
    public void testEquals() throws Exception {
        assertEquals(PackagePattern.parse("de.skuzzle.**"),
                PackagePattern.parse("de.skuzzle.**"));
        assertEquals(PackagePattern.parse("de.skuzzle.**").hashCode(),
                PackagePattern.parse("de.skuzzle.**").hashCode());
    }

    @Test
    public void testVerifyEquals() throws Exception {
        EqualsVerifier.forClass(PackagePatternImpl.class).verify();
    }

    @Test
    public void testNotEquals() throws Exception {
        assertNotEquals(PackagePattern.parse("de.skuzzle.**"),
                PackagePattern.parse("de.skuzzle.*"));
    }

    @Test
    public void testMatchDefaultPackage() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("**");
        assertTrue(pattern.matches(""));
    }

    @Test
    public void testMatchesDefaultPackage2() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("*");
        assertTrue(pattern.matches(""));
    }

    @Test
    public void testMatchExactly() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("de.skuzzle.SomeClass");
        assertTrue(pattern.matches("de.skuzzle.SomeClass"));
        assertFalse(pattern.matches("de.skuzzle.SomeClass2"));
    }

    @Test
    public void testMatchWildCardSuffix() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("de.skuzzle.*");
        assertTrue(pattern.matches("de.skuzzle.TestClass"));
        assertTrue(pattern.matches("de.skuzzle.TestClass2"));
    }

    @Test
    public void testWildCardMatchesSingle() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("de.skuzzle.*");
        assertFalse(pattern.matches("de.skuzzle.sub.TestClass"));
    }

    @Test
    public void testMatchInfix() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("de.skuzzle.*.test");
        assertTrue(pattern.matches("de.skuzzle.foo.test"));
        assertFalse(pattern.matches("de.skuzzle.test"));
    }

    @Test
    public void testMatchMultipleInfix() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("de.skuzzle.*.xx.*.test");
        assertTrue(pattern.matches("de.skuzzle.foo.xx.bar.test"));
        assertFalse(pattern.matches("de.skuzzle.foo.xx.bar"));
    }

    @Test
    public void testWildcardMatchMultiple() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("de.skuzzle.**");
        assertTrue(pattern.matches("de.skuzzle.sub.TestClass"));
        assertTrue(pattern.matches("de.skuzzle.TestClass2"));
    }

    @Test
    public void testLogger() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("java.util.**");
        assertTrue(pattern.matches("java.util.logging.Logger"));
    }

    @Test
    public void testWildcardInStringToTest() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("java.util.ArrayList");
        assertFalse(pattern.matches("java.util.*"));
    }

    @Test
    public void testDoubleWildcardInBetween() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("com.**.bar.ClassName");
        assertTrue(pattern.matches("com.foo.bar.ClassName"));
        assertFalse(pattern.matches("com.xyz.foo.bar"));
        assertFalse(pattern.matches("com.xyz.foo.ClassName"));
        assertFalse(pattern.matches("com.bar.ClassName"));
    }

    @Test
    public void testDoubleWildcardInBetweenSkipMultiple() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("com.**.bar.ClassName");
        assertTrue(pattern.matches("com.xyz.foo.bar.ClassName"));
        assertFalse(pattern.matches("com.xyz.foo.bar"));
    }

    @Test
    public void testConsecutiveDoubleWildcard() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("com.**.**.ClassName");
        assertTrue(pattern.matches("com.xyz.foo.bar.ClassName"));
        assertFalse(pattern.matches("com.xyz.foo.bar"));
    }

    @Test
    public void testDoubleWildcard() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("com.**.xx.**.ClassName");
        assertTrue(pattern.matches("com.xyz.foo.yy.xx.bar.ClassName"));
        assertFalse(pattern.matches("com.xyz.foo.bar"));
    }

    @Test
    public void testDoubleWildCartBeginning() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("**.ClassName");
        assertTrue(pattern.matches("com.xyz.foo.bar.ClassName"));
    }

    @Test
    public void test() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("com.foo.**");
        assertFalse(pattern.matches("java.util.ArrayList"));
    }

    @Test
    public void testPatternMatchesPattern() throws Exception {
        assertTrue(PackagePattern.parse("com.foo.**")
                .matches(PackagePattern.parse("com.foo.*")));
        assertTrue(PackagePattern.parse("com.foo.*")
                .matches(PackagePattern.parse("com.foo.Class")));
        assertFalse(PackagePattern.parse("com.foo.Class")
                .matches(PackagePattern.parse("com.foo.*")));
    }

    @Test
    public void testStaticImport() throws Exception {
        assertTrue(PackagePattern.parse("static com.foo.bar.*")
                .matches("static com.foo.bar.Test"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseEmptyString() throws Exception {
        PackagePattern.parse("");
    }
}
