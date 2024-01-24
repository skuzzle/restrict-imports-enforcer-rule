pipeline {
	options {
		disableConcurrentBuilds()
	}
	agent {
		docker {
			// Need an image with git installed that is why we stick with maven image for now though we're using gradle
			image 'maven:3.9.4-eclipse-temurin-11'
			args '-v /home/jenkins/.m2:/var/maven/.m2 -v /home/jenkins/.gradle:/tmp/gradle-user-home:rw -v /home/jenkins/.gnupg:/.gnupg -e MAVEN_OPTS=-Duser.home=/var/maven -e MAVEN_CONFIG='
		}
	}
	environment {
		COVERALLS_REPO_TOKEN = credentials('coveralls_repo_token_restrict_imports_rule')
		BUILD_CACHE = credentials('build_cache')
		GRADLE_CACHE = '/tmp/gradle-user-home'
		HOME = '~/'
		ORG_GRADLE_PROJECT_sonatype = credentials('SONATYPE_NEXUS')
		ORG_GRADLE_PROJECT_signingPassword = credentials('gpg_password')
		ORG_GRADLE_PROJECT_base64EncodedAsciiArmoredSigningKey  = credentials('gpg_private_key')
	}
	stages {
		stage('Prepare container') {
			steps {
				// Copy the Gradle cache from the host, so we can write to it
				sh "rsync -a --include /caches --include /wrapper --exclude '/*' ${GRADLE_CACHE}/ ${HOME}/.gradle || true"
			}
		}
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
		success {
			// Write updates to the Gradle cache back to the host
			sh "rsync -au ${HOME}/.gradle/caches ${HOME}/.gradle/wrapper ${GRADLE_CACHE}/ || true"
		}
		always {
			archiveArtifacts(artifacts: '*.md')
			junit (testResults: '**/build/test-results/test/**.xml,**/build/*/reports/**.xml', allowEmptyResults: true)
		}
	}
}
