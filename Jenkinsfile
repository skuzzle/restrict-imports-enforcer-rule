pipeline {
    // The Gradle user home lives in a named volume shared by this agent's executors, with
    // a directory per executor. An executor runs one build at a time, so every build gets
    // a cache it has entirely to itself and that is still there for the next build on that
    // executor - no copying in and out, and no two builds writing the same Gradle home.
    //
    // Maven needs no mount of its own. Everything it touches now lives under the project's
    // build directory: the distributions in build/maven-dist, the outer build's local
    // repository in build/m2 and one per cross-version leg in build/m2-<task>.
    agent {
        dockerfile {
            filename 'docker/Dockerfile'
            // The image entrypoint is disabled on purpose. The plugin starts the container
            // before this pipeline's environment block applies, so GRADLE_USER_HOME is not
            // set yet and the entrypoint would seed a home no build ever uses - a 164MB
            // copy per build. The plugin also checks that the command it passed is running
            // moments after the container starts and reports an entrypoint still busy at
            // that point as an error. The build prepares the real home in its first step.
            args "--entrypoint='' -v restrict-imports-gradle-user-homes:/gradle-homes:rw -v /home/jenkins/.gnupg:/.gnupg:ro"
        }
    }
    environment {
        // Enables Build Scan publishing and pushing to the remote build cache, both of which
        // the build gates on this variable being set. Jenkins does not set it by itself.
        CI = 'true'
        COVERALLS_REPO_TOKEN = credentials('coveralls_repo_token_restrict_imports_rule')
        DEVELOCITY_ACCESS_KEY = credentials('develocity_access_key')
        GRADLE_USER_HOME = "/gradle-homes/${env.EXECUTOR_NUMBER}"
        MAVEN_CONFIG = ''
        ORG_GRADLE_PROJECT_sonatype = credentials('SONATYPE_NEXUS')
        ORG_GRADLE_PROJECT_signingPassword = credentials('gpg_password')
        ORG_GRADLE_PROJECT_base64EncodedAsciiArmoredSigningKey = credentials('gpg_private_key')
    }
    stages {
        stage('Prepare Gradle user home') {
            steps {
                // An EXECUTOR_NUMBER that did not resolve would put every executor on this
                // agent in the same Gradle user home, which is a data race rather than a
                // slow build. Fail on it instead.
                sh 'test "$GRADLE_USER_HOME" = "/gradle-homes/$EXECUTOR_NUMBER"'
                // Creates the directory on first use and seeds it with the Gradle
                // distribution baked into the image, which is what the entrypoint the
                // agent disables would otherwise do.
                sh 'prepare-gradle-user-home'
            }
        }
        stage('Quickcheck') {
            steps {
                withGradle {
                    sh './gradlew quickCheck --configuration-cache'
                }
            }
        }
        stage('build-logic tests') {
            steps {
                withGradle {
                    sh './gradlew build-logic:check'
                }
            }
        }
        stage('Unit-tests') {
            steps {
                withGradle {
                    sh './gradlew test coveralls'
                }
            }
        }
        stage('Func-tests') {
            steps {
                withGradle {
                    sh './gradlew functionalTest'
                }
            }
        }
        stage('readme') {
            steps {
                withGradle {
                    sh './gradlew generateReadmeAndReleaseNotes'
                }
            }
        }
    }
    post {
        always {
            archiveArtifacts(artifacts: '*.md')
            junit(testResults: '**/build/test-results/test/**.xml,**/build/*/reports/**.xml', allowEmptyResults: true)
        }
    }
}
