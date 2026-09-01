# Build image

The official Gradle image, plus the two things a CI build of this repository needs from it:
the wrapper distribution is already in the image, and the Gradle user home can be moved
somewhere else without running into file ownership.

```shell
docker build -f docker/Dockerfile -t restrict-imports-build .
```

Build from the repository root: the Dockerfile copies `gradlew` and `gradle/wrapper` out of
the build context. Pass `--build-arg BASE_IMAGE=gradle:jdk25` to build against another JDK.

## The baked wrapper distribution

The image runs `./gradlew --version` at build time against a Gradle user home of its own,
`/opt/gradle-user-home`. Letting the wrapper do the download is what puts the distribution
under the hash-derived path the wrapper later looks in, and it picks up whatever version
`gradle/wrapper/gradle-wrapper.properties` names, so the image cannot drift from the
repository. Rebuild the image after a wrapper upgrade.

`/opt/gradle-user-home` is only a seed. Builds never write to it, and it is deliberately not
`/home/gradle/.gradle`, which the base image declares as a volume - a mount there would hide
whatever the image baked in.

## Moving the Gradle user home

Point `GRADLE_USER_HOME` at any path. Before the build starts, that directory is created,
seeded from `/opt/gradle-user-home` and handed to the user the build runs as:

```shell
docker run --rm -e GRADLE_USER_HOME=/tmp/gradle-home -v "$PWD:/work" -w /work \
    restrict-imports-build ./gradlew build
```

Seeding never overwrites: a cache mounted at that path keeps its own files and only gains
what it is missing.

| Variable                | Default               | Meaning                                                                                             |
|-------------------------|-----------------------|-----------------------------------------------------------------------------------------------------|
| `GRADLE_USER_HOME`      | `/home/gradle/.gradle`| Where builds read and write their Gradle user home.                                                  |
| `GRADLE_RUN_AS`         | `gradle`              | The user a build runs as, and who the Gradle user home is handed to. Only applies when the container starts as root; `root` keeps the base image's behaviour. |
| `GRADLE_FIX_OWNERSHIP`  | `auto`                | `auto` chowns `GRADLE_USER_HOME` only when it belongs to somebody else, `always` chowns on every start, `never` leaves ownership alone. |
| `GRADLE_USER_HOME_SEED` | `/opt/gradle-user-home` | The Gradle user home baked into the image.                                                         |

## Ownership

A directory mounted from the host, or a fresh named volume, belongs to whoever created it,
which is rarely the user the build runs as. Started as root, the image chowns
`GRADLE_USER_HOME` to `GRADLE_RUN_AS` and only then drops to that user, so the build finds a
directory it owns. `auto` compares the directory's owner first, so a warm cache is not walked
again on every start.

Started as an unprivileged user - `docker run -u`, which is how the Jenkins Docker Pipeline
plugin launches agents - nothing inside the container can change ownership. The image then
only warns when `GRADLE_USER_HOME` is not writable. Either mount a directory that already
belongs to that uid, or keep the Gradle user home inside the container.

## Runners that override the entrypoint

The Jenkins Docker Pipeline plugin starts containers with `--entrypoint cat` and runs every
step through `docker exec`, so the image entrypoint never runs. The preparation is a script
of its own for that reason - call it as the first step of the build:

```shell
prepare-gradle-user-home
```

It is idempotent, so calling it more than once costs nothing.
