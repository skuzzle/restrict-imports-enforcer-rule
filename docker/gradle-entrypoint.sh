#!/bin/sh
# Entrypoint of the custom Gradle image: prepares the Gradle user home, then hands over
# to the entrypoint of the base image, which sets up the JVM truststore.
#
# When started as root, the command itself runs as GRADLE_RUN_AS - the user the Gradle
# user home was just handed to. Set GRADLE_RUN_AS=root to stay root.
set -eu

prepare-gradle-user-home

base_entrypoint=/__cacert_entrypoint.sh
[ -x "$base_entrypoint" ] || base_entrypoint=

run_as=${GRADLE_RUN_AS:-}
if [ "$(id -u)" -eq 0 ] && [ -n "$run_as" ] && [ "$run_as" != root ]; then
    entry=$(getent passwd "$run_as")
    uid=$(echo "$entry" | cut -d: -f3)
    gid=$(echo "$entry" | cut -d: -f4)
    # The JVM reads user.home from the passwd entry rather than from HOME, so keep the
    # two in agreement - anything resolving paths from HOME would disagree otherwise.
    HOME=$(echo "$entry" | cut -d: -f6)
    export HOME
    exec setpriv --reuid="$uid" --regid="$gid" --init-groups --inh-caps=-all -- \
        ${base_entrypoint} "$@"
fi

exec ${base_entrypoint} "$@"
