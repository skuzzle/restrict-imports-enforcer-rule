package de.skuzzle.enforcer.restrictimports;
import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import de.skuzzle.enforcer.restrictimports.impl.PackagePatternImpl;


/**
 * Pattern class to match java style package and class names using wild card
 * operators.
 *
 * @author Simon Taddiken
 */
public interface PackagePattern {

    public static List<PackagePattern> parseAll(Collection<String> strings) {
        return strings.stream()
                .map(PackagePattern::parse)
                .collect(Collectors.toList());
    }

    public static PackagePattern parse(String s) {
        checkArgument(s != null);
        return new PackagePatternImpl(s);
    }

    boolean matches(String packageName);
}
