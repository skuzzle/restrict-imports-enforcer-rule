package de.skuzzle.enforcer.restrictimports.analyze;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class BannedImportGroups {

    private final List<BannedImportGroup> groups;

    public BannedImportGroups(List<BannedImportGroup> groups) {
        this.groups = groups;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<BannedImportGroup> selectGroupFor(String fqcn) {
        return groups.stream()
                .map(group -> matches(group, fqcn))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted()
                .map(GroupMatch::getGroup)
                .findFirst();
    }

    private Optional<GroupMatch> matches(BannedImportGroup group, String fqcn) {
        final Optional<PackagePattern> basePackageMatch = group.getBasePackages().stream()
                .filter(pattern -> pattern.matches(fqcn))
                .findFirst();

        if (!basePackageMatch.isPresent() || group.exclusionMatches(fqcn)) {
            return Optional.empty();
        }

        return basePackageMatch
                .map(basePackage -> new GroupMatch(basePackage, group));
    }

    private static class GroupMatch implements Comparable<GroupMatch> {
        private final PackagePattern basePackage;
        private final BannedImportGroup group;

        public GroupMatch(PackagePattern basePackage, BannedImportGroup group) {
            this.basePackage = basePackage;
            this.group = group;
        }

        public BannedImportGroup getGroup() {
            return this.group;
        }

        @Override
        public int compareTo(GroupMatch o) {
            return o.basePackage.compareTo(basePackage);
        }
    }

    public static final class Builder {
        private final List<BannedImportGroup> groups = new ArrayList<>();

        public Builder withGroup(BannedImportGroup.Builder groupBuilder) {
            groups.add(groupBuilder.build());
            return this;
        }

        public BannedImportGroups build() {
            return new BannedImportGroups(groups);
        }
    }
}
