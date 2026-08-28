pipeline {
    // The Maven cache must be mounted at the home directory of the image's uid 1000 user:
    // Maven resolves its local repository from the JVM's user.home, which comes from the
    // passwd entry and can not be redirected with the HOME environment variable.
    agent {
        docker {
            image 'gradle:jdk21'
            args '-v /home/jenkins/caches/restrict-imports/.m2:/home/gradle/.m2:rw -v /home/jenkins/caches/restrict-imports/.gradle:/tmp/gradle-user-home:rw -v /home/jenkins/.gnupg:/.gnupg:ro'
        }
    }
    environment {
        // Enables Build Scan publishing and pushing to the remote build cache, both of which
        // the build gates on this variable being set. Jenkins does not set it by itself.
        CI = 'true'
        COVERALLS_REPO_TOKEN = credentials('coveralls_repo_token_restrict_imports_rule')
        DEVELOCITY_ACCESS_KEY = credentials('develocity_access_key')
        GRADLE_CACHE = '/tmp/gradle-user-home'
        GRADLE_USER_HOME = '/tmp/gradle-home'
        MAVEN_CONFIG = ''
        ORG_GRADLE_PROJECT_sonatype = credentials('SONATYPE_NEXUS')
        ORG_GRADLE_PROJECT_signingPassword = credentials('gpg_password')
        ORG_GRADLE_PROJECT_base64EncodedAsciiArmoredSigningKey = credentials('gpg_private_key')
    }
    stages {
        stage('Load Gradle Cache from host') {
            steps {
                sh './.jenkins/load-gradle-cache.sh'
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
            sh './.jenkins/store-gradle-cache.sh'
        }
        always {
            archiveArtifacts(artifacts: '*.md')
            junit(testResults: '**/build/test-results/test/**.xml,**/build/*/reports/**.xml', allowEmptyResults: true)
        }
    }
}
