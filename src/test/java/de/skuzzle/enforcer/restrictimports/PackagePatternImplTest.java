package de.skuzzle.enforcer.restrictimports;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class PackagePatternImplTest {

    @Before
    public void setUp() throws Exception {}

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
    public void testWildCatMatchMultiple() throws Exception {
        final PackagePattern pattern = PackagePattern.parse("de.skuzzle.**");
        assertTrue(pattern.matches("de.skuzzle.sub.TestClass"));
        assertTrue(pattern.matches("de.skuzzle.TestClass2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWildCardNoSuffix() throws Exception {
        PackagePattern.parse("de.skuzzle.**.test");
    }
}
