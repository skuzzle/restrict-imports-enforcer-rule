#!/bin/sh
# Writes updates to the Gradle cache back to the host cache mounted into the container.
# Expects GRADLE_CACHE (the mounted host cache) and GRADLE_USER_HOME to be set.
# Runs in the Linux build container and relies on GNU cp: -u keeps files that a concurrent
# build already wrote to the shared host cache.
set -eu

for dir in jdks caches wrapper; do
    [ -d "$GRADLE_USER_HOME/$dir" ] || continue
    mkdir -p "$GRADLE_CACHE/$dir"
    cp -au "$GRADLE_USER_HOME/$dir/." "$GRADLE_CACHE/$dir/" ||
        echo "WARNING: could not write $dir back to the host cache"
done
