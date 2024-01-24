pipeline {
	options {
		disableConcurrentBuilds()
	}
	agent {
		docker {
			// Need an image with git installed that is why we stick with maven image for now though we're using gradle
			image 'maven:3.9.4-eclipse-temurin-11'
			args '-v /home/jenkins/.m2:/var/maven/.m2 -v /home/jenkins/.gradle:/var/gradle/.gradle -v /home/jenkins/.gnupg:/.gnupg -e GRADLE_OPTS=-Duser.home=/var/gradle -e MAVEN_OPTS=-Duser.home=/var/maven -e MAVEN_CONFIG='
		}
	}
	environment {
		COVERALLS_REPO_TOKEN = credentials('coveralls_repo_token_restrict_imports_rule')
		BUILD_CACHE = credentials('build_cache')
		GRADLE_USER_HOME = '/var/gradle'
		ORG_GRADLE_PROJECT_sonatype = credentials('SONATYPE_NEXUS')
		ORG_GRADLE_PROJECT_signingPassword = credentials('gpg_password')
		ORG_GRADLE_PROJECT_base64EncodedAsciiArmoredSigningKey  = credentials('gpg_private_key')
	}
	stages {
		stage('Quickcheck') {
			steps {
				withGradle {
					sh './gradlew quickCheck'
				}
			}
		}
		stage('Test') {
			parallel {
				stage('Func-tests') {
					steps {
						withGradle {
							sh './gradlew functionalTest'
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
				stage('readme') {
					steps {
						withGradle {
							sh './gradlew generateReadmeAndReleaseNotes'
						}
					}
				}
			}
		}
	}
	post {
		always {
			archiveArtifacts(artifacts: '*.md')
			junit (testResults: '**/build/test-results/test/**.xml,**/build/*/reports/**.xml', allowEmptyResults: true)
		}
	}
}
