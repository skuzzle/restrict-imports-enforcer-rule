package de.skuzzle.enforcer.restrictimports.analyze;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public class PackagePatternSpecificityTest {

    private static class SpecificityTest {
        private String lessSpecific;
        private final String moreSpecific;

        public SpecificityTest(String moreSpecific) {
            this.moreSpecific = moreSpecific;
        }

        public SpecificityTest toBeMoreSpecificThan(String lessSpecific) {
            this.lessSpecific = lessSpecific;
            return this;
        }
    }

    private static SpecificityTest expect(String moreSpecific) {
        return new SpecificityTest(moreSpecific);

    }

    private final Collection<SpecificityTest> patterns = Arrays.asList(
            expect("*").toBeMoreSpecificThan("**"),
            expect("de.**").toBeMoreSpecificThan("**"),
            expect("de.xyz.*").toBeMoreSpecificThan("de.xyz.**"),
            expect("de.xyz.Foo").toBeMoreSpecificThan("de.xyz.Foo*"),
            expect("de.xyz.Foo").toBeMoreSpecificThan("de.xyz.*Foo"),
            expect("de.xyz.Foo").toBeMoreSpecificThan("de.xyz.*Foo*"),
            expect("de.xyz.*Foo").toBeMoreSpecificThan("de.*xyz.*Foo"),
            expect("de.x.y.z.*Foo").toBeMoreSpecificThan("de.*xyz.*Foo"),

            expect("de.*.xyz").toBeMoreSpecificThan("de.**.xyz"),
            expect("de").toBeMoreSpecificThan("*"),
            expect("de").toBeMoreSpecificThan("**"),
            expect("de.xyz").toBeMoreSpecificThan("de.*"),

            expect("de.xyz").toBeMoreSpecificThan("de"),

            expect("de.*.xyz.*").toBeMoreSpecificThan("de.**.xyz.*"),
            expect("de.*.xyz.*").toBeMoreSpecificThan("de.**.xyz.**"),
            expect("de.*.xyz.de").toBeMoreSpecificThan("de.**.xyz.de"),
            expect("de.*.xyz.**.*").toBeMoreSpecificThan("de.**.xyz.*.foo"),
            expect("com.foo.bar.**.abc.de").toBeMoreSpecificThan("com.foo.**.bar.abc.de"),
            expect("*.xyz").toBeMoreSpecificThan("**.xyz"));

    @TestFactory
    Stream<DynamicNode> testCompareToSelf() {
        return patterns.stream()
                .map(pattern -> DynamicTest.dynamicTest(String.format(
                        "%s should be more specific than itself", pattern.moreSpecific),
                        () -> {
                            final PackagePattern moreSpecific = PackagePattern
                                    .parse(pattern.moreSpecific);

                            assertThat(moreSpecific.compareTo(moreSpecific))
                                    .isEqualTo(0);
                        }));
    }

    @TestFactory
    Stream<DynamicNode> testCompareSpecificity() {
        return patterns.stream()
                .map(pattern -> DynamicTest.dynamicTest(String.format(
                        "%s should be more specific than %s", pattern.moreSpecific,
                        pattern.lessSpecific), () -> {
                            final PackagePattern lessSpecific = PackagePattern
                                    .parse(pattern.lessSpecific);
                            final PackagePattern moreSpecific = PackagePattern
                                    .parse(pattern.moreSpecific);

                            assertThat(moreSpecific.compareTo(lessSpecific))
                                    .isGreaterThan(0);
                        }));
    }

    @TestFactory
    Stream<DynamicNode> testCompareSpecificityReverse() {
        return patterns.stream()
                .map(pattern -> DynamicTest.dynamicTest(String.format(
                        "%s should not be more specific than %s", pattern.lessSpecific,
                        pattern.moreSpecific), () -> {
                            final PackagePattern lessSpecific = PackagePattern
                                    .parse(pattern.lessSpecific);
                            final PackagePattern moreSpecific = PackagePattern
                                    .parse(pattern.moreSpecific);

                            assertThat(lessSpecific.compareTo(moreSpecific))
                                    .isLessThan(0);
                        }));
    }
}
