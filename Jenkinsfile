pipeline {
    agent {
        docker {
            image 'gradle:jdk21'
            args '-v /home/jenkins/caches/restrict-imports/.m2:/tmp/jenkins-home/.m2:rw -v /home/jenkins/caches/restrict-imports/.gradle:/tmp/gradle-user-home:rw -v /home/jenkins/.gnupg:/.gnupg:ro'
        }
    }
    environment {
        COVERALLS_REPO_TOKEN = credentials('coveralls_repo_token_restrict_imports_rule')
        BUILD_CACHE = credentials('build_cache')
        GRADLE_CACHE = '/tmp/gradle-user-home'
        GRADLE_USER_HOME = '/tmp/gradle-home'
        HOME = '/tmp/jenkins-home'
        MAVEN_CONFIG = ''
        ORG_GRADLE_PROJECT_sonatype = credentials('SONATYPE_NEXUS')
        ORG_GRADLE_PROJECT_signingPassword = credentials('gpg_password')
        ORG_GRADLE_PROJECT_base64EncodedAsciiArmoredSigningKey = credentials('gpg_private_key')
    }
    stages {
        stage('Load Gradle Cache from host') {
            steps {
                // Copy the Gradle cache from the host, so we can write to it
                sh '''
                    for dir in jdks caches wrapper; do
                        [ -d "$GRADLE_CACHE/$dir" ] || continue
                        mkdir -p "$GRADLE_USER_HOME/$dir"
                        cp -a "$GRADLE_CACHE/$dir/." "$GRADLE_USER_HOME/$dir/" || true
                    done
                '''
            }
        }
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
        success {
            // Write updates to the Gradle cache back to the host
            sh '''
                for dir in jdks caches wrapper; do
                    [ -d "$GRADLE_USER_HOME/$dir" ] || continue
                    mkdir -p "$GRADLE_CACHE/$dir"
                    cp -au "$GRADLE_USER_HOME/$dir/." "$GRADLE_CACHE/$dir/" || true
                done
            '''
        }
        always {
            archiveArtifacts(artifacts: '*.md')
            junit(testResults: '**/build/test-results/test/**.xml,**/build/*/reports/**.xml', allowEmptyResults: true)
        }
    }
}
