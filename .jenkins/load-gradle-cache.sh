#!/bin/sh
# Copies the Gradle cache from the host into the container, so that the build can write to it.
# Expects GRADLE_CACHE (the mounted host cache) and GRADLE_USER_HOME to be set.
# Runs in the Linux build container and relies on GNU cp.
set -eu

for dir in jdks caches wrapper; do
    [ -d "$GRADLE_CACHE/$dir" ] || continue
    mkdir -p "$GRADLE_USER_HOME/$dir"
    # A cache that can not be copied is not worth failing the build over
    cp -a "$GRADLE_CACHE/$dir/." "$GRADLE_USER_HOME/$dir/" ||
        echo "WARNING: could not load $dir from the host cache, continuing without it"
done
