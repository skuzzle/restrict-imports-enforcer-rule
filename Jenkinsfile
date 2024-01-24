pipeline {
    agent {
        docker {
            image 'ghcr.io/cloud-taddiken-online/build-java:21-jdk'
            args '-v /home/jenkins/caches/restrict-imports/.m2:/home/jenkins/.m2:rw -v /home/jenkins/caches/restrict-imports/.gradle:/tmp/gradle-user-home:rw -v /home/jenkins/.gnupg:/.gnupg:ro'
        }
    }
    environment {
        COVERALLS_REPO_TOKEN = credentials('coveralls_repo_token_restrict_imports_rule')
        BUILD_CACHE = credentials('build_cache')
        GRADLE_CACHE = '/tmp/gradle-user-home'
        GRADLE_USER_HOME = '/home/jenkins/.gradle'
        MAVEN_CONFIG = ''
        ORG_GRADLE_PROJECT_sonatype = credentials('SONATYPE_NEXUS')
        ORG_GRADLE_PROJECT_signingPassword = credentials('gpg_password')
        ORG_GRADLE_PROJECT_base64EncodedAsciiArmoredSigningKey  = credentials('gpg_private_key')
    }
    stages {
        stage('Load Gradle Cache from host') {
            steps {
                // Copy the Gradle cache from the host, so we can write to it
                sh "rsync -a --include /jdks --include /caches --include /wrapper --exclude '/*' ${GRADLE_CACHE}/ ${GRADLE_USER_HOME} || true"
            }
        }
        stage('Quickcheck') {
            steps {
                withGradle {
                    sh './gradlew quickCheck'
                }
            }
        }
        stage('Coverage') {
            steps {
                withGradle {
                    sh './gradlew coveralls'
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
        success {
            // Write updates to the Gradle cache back to the host
            sh "rsync -au ${GRADLE_USER_HOME}/jdks ${GRADLE_USER_HOME}/caches ${GRADLE_USER_HOME}/wrapper ${GRADLE_CACHE}/ || true"
        }
        always {
            archiveArtifacts(artifacts: '*.md')
            junit (testResults: '**/build/test-results/test/**.xml,**/build/*/reports/**.xml', allowEmptyResults: true)
        }
    }
}
