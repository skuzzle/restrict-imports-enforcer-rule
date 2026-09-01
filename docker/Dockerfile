# syntax=docker/dockerfile:1
#
# The build image: the official Gradle image plus this repository's wrapper distribution
# and a Gradle user home that can be moved somewhere else at run time.
#
# Build from the repository root, so that the wrapper files are part of the context:
#     docker build -f docker/Dockerfile -t restrict-imports-build .
ARG BASE_IMAGE=gradle:jdk21
FROM ${BASE_IMAGE}

# Where the Gradle user home baked into this image lives. The entrypoint seeds the
# effective GRADLE_USER_HOME from here. It is deliberately not /home/gradle/.gradle:
# the base image declares that path as a volume, so a mount would hide the seed.
ENV GRADLE_USER_HOME_SEED=/opt/gradle-user-home

# Where builds read and write their Gradle user home. Point this anywhere at run time.
ENV GRADLE_USER_HOME=/home/gradle/.gradle

# The user a build runs as when the container is started as root, and who the Gradle
# user home is handed to. Set to root to keep the base image's behaviour.
ENV GRADLE_RUN_AS=gradle

# auto chowns GRADLE_USER_HOME only when it belongs to somebody else, always chowns on
# every start, never leaves ownership alone.
ENV GRADLE_FIX_OWNERSHIP=auto

# Bake in the distribution that this repository's wrapper asks for. Letting the wrapper
# itself download it is what puts it under the hash-derived path the wrapper looks in.
COPY gradlew /tmp/wrapper/
COPY gradle/wrapper /tmp/wrapper/gradle/wrapper
RUN GRADLE_USER_HOME="$GRADLE_USER_HOME_SEED" /tmp/wrapper/gradlew --version \
    && rm -rf /tmp/wrapper "$GRADLE_USER_HOME_SEED/daemon" "$GRADLE_USER_HOME_SEED/.tmp" \
    && chmod -R a+rX "$GRADLE_USER_HOME_SEED"

# A parent for Gradle user homes, meant to have a shared volume mounted over it so that
# several containers can keep a home each. Docker initialises a fresh named volume from
# the image's directory, mode included, and 1777 is what lets an unprivileged container
# create its own home in a volume that would otherwise belong to root - while still
# keeping it from touching anybody else's, the same bargain /tmp makes.
RUN install -d -m 1777 /gradle-homes

COPY --chmod=0755 docker/prepare-gradle-user-home docker/gradle-entrypoint.sh /usr/local/bin/

ENTRYPOINT ["/usr/local/bin/gradle-entrypoint.sh"]
CMD ["gradle"]
