pipeline {
    // Both caches must be mounted at the home directory of the image's uid 1000 user:
    // Maven resolves its local repository from the JVM's user.home, which comes from the
    // passwd entry and can not be redirected with the HOME environment variable.
    //
    // The Gradle home is a named volume per executor rather than one shared directory.
    // Gradle's caches use file locking, but that only coordinates processes which can talk
    // to each other, which containerised builds can not - and this controller runs several
    // branch builds at once. An executor only ever runs one build at a time, so a volume
    // per executor is exclusive without copying gigabytes in and out of every build.
    //
    // A named volume rather than a bind mount so that Docker creates it on demand: the
    // image already owns /home/gradle/.gradle as uid 1000, and a new named volume inherits
    // that ownership. A bind mount would be created as root and adding an executor would
    // need a matching directory on the host first.
    agent {
        docker {
            image 'gradle:jdk21'
            args "-v /home/jenkins/caches/restrict-imports/.m2:/home/gradle/.m2:rw -v restrict-imports-gradle-home-${env.EXECUTOR_NUMBER}:/home/gradle/.gradle -v /home/jenkins/.gnupg:/.gnupg:ro"
        }
    }
    environment {
        // Enables Build Scan publishing and pushing to the remote build cache, both of which
        // the build gates on this variable being set. Jenkins does not set it by itself.
        CI = 'true'
        COVERALLS_REPO_TOKEN = credentials('coveralls_repo_token_restrict_imports_rule')
        DEVELOCITY_ACCESS_KEY = credentials('develocity_access_key')
        GRADLE_USER_HOME = '/home/gradle/.gradle'
        MAVEN_CONFIG = ''
        ORG_GRADLE_PROJECT_sonatype = credentials('SONATYPE_NEXUS')
        ORG_GRADLE_PROJECT_signingPassword = credentials('gpg_password')
        ORG_GRADLE_PROJECT_base64EncodedAsciiArmoredSigningKey = credentials('gpg_private_key')
    }
    stages {
        stage('Quickcheck') {
            steps {
                withGradle {
                    sh './gradlew quickCheck'
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
