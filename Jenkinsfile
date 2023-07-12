pipeline {
  options {
    disableConcurrentBuilds()
  }
  agent {
    docker {
      image 'maven:3.6-jdk-11'
      args '-v /home/jenkins/.m2:/var/maven/.m2 -v /home/jenkins/.gnupg:/.gnupg -e MAVEN_CONFIG=/var/maven/.m2 -e MAVEN_OPTS=-Duser.home=/var/maven'
    }
  }
  environment {
    COVERALLS_REPO_TOKEN = credentials('coveralls_repo_token_restrict_imports_rule')
    GPG_SECRET = credentials('gpg_password')
  }
  stages {
    stage('Build') {
      steps {
        sh 'mvn -B clean install -Pwith-coverage'
      }
    }
    stage('Coverage') {
      steps {
        sh 'mvn -B jacoco:report jacoco:report-integration coveralls:report -DrepoToken=$COVERALLS_REPO_TOKEN -Pwith-coverage'
      }
    }
    stage('javadoc') {
      steps {
        sh 'mvn -B javadoc:javadoc'
      }
    }
    stage('Deploy SNAPSHOT') {
      when {
        branch 'develop'
      }
      steps {
        sh 'mvn -B -Prelease -DskipTests=true -Dgpg.passphrase=${GPG_SECRET} deploy'
      }
    }
  }
  post {
    always {
      junit (testResults: '**/target/surefire-reports/**.xml,**/target/*/reports/**.xml', allowEmptyResults: true)
    }
  }
}
