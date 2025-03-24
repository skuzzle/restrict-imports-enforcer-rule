package de.skuzzle.enforcer.restrictimports.analyze;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import de.skuzzle.enforcer.restrictimports.util.Preconditions;

/**
 * Groups multiple {@link BannedImportGroup} objects.
 *
 * @author Simon Taddiken
 */
public final class BannedImportGroups {

    private final List<BannedImportGroup> groups;

    private BannedImportGroups(List<BannedImportGroup> groups) {
        Preconditions.checkArgument(!groups.isEmpty(), "Groups may not be empty");
        this.groups = groups;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Selects the {@link BannedImportGroup} with the most specific base package matching
     * the given full qualified class name. If the most specific match also specifies an
     * exclusion pattern for the given fqcn the result will be empty.
     *
     * @param fqcn The full qualified class name to find the group for.
     * @return The group with the most specific base package match.
     */
    public Optional<BannedImportGroup> selectGroupFor(String fqcn) {
        return groups.stream()
                .map(group -> matches(group, fqcn))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted()
                .map(GroupMatch::getGroup)
                .findFirst()
                .filter(group -> !group.exclusionMatches(fqcn));
    }

    private Optional<GroupMatch> matches(BannedImportGroup group, String fqcn) {
        return group.getBasePackages().stream()
                .filter(pattern -> pattern.matches(fqcn))
                .findFirst()
                .map(basePackage -> new GroupMatch(basePackage, group));
    }

    boolean hasNotFixableDefinition() {
        return groups.get(0).hasNotFixables();
    }

    @Override
    public int hashCode() {
        return Objects.hash(groups);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof BannedImportGroups
                && Objects.equals(groups, ((BannedImportGroups) obj).groups);
    }

    @Override
    public String toString() {
        return groups.stream().map(BannedImportGroup::toString).collect(Collectors.joining(System.lineSeparator()));
    }

    private static final class GroupMatch implements Comparable<GroupMatch> {
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

        public Builder withGroups(Collection<BannedImportGroup> groups) {
            this.groups.addAll(groups);
            return this;
        }

        public Builder withGroup(BannedImportGroup group) {
            this.groups.add(group);
            return this;
        }

        public Builder withGroup(BannedImportGroup.Builder groupBuilder) {
            groups.add(groupBuilder.build());
            return this;
        }

        private void checkGroupsConsistency() {
            groupsShouldNotBeEmpty();
            groupsShouldNotHaveEqualBasePackages();
        }

        private void groupsShouldNotBeEmpty() {
            if (groups.isEmpty()) {
                throw new BannedImportDefinitionException("No BannedImportGroups have been specified");
            }
        }

        private void groupsShouldNotHaveEqualBasePackages() {
            // there should be no 2 groups with equal base packages
            for (BannedImportGroup groupOuter : groups) {
                for (BannedImportGroup groupInner : groups) {
                    if (groupInner == groupOuter) {
                        continue;
                    }
                    if (new HashSet<>(groupOuter.getBasePackages())
                            .equals(new HashSet<>(groupInner.getBasePackages()))) {
                        throw new BannedImportDefinitionException(
                                "There are two groups with equal base package definitions: "
                                        + groupInner.getBasePackages().stream().map(PackagePattern::toString)
                                                .collect(Collectors.joining(", ")));
                    }
                }
            }

        }

        public BannedImportGroups build() {
            checkGroupsConsistency();
            return new BannedImportGroups(groups);
        }
    }
}
