pipeline {
  agent {
    docker {
      image 'maven:3.6.1-jdk-8'
      args '-v /home/jenkins/.m2:/var/maven/.m2 -e MAVEN_CONFIG=/var/maven/.m2 -e MAVEN_OPTS=-Duser.home=/var/maven'
    }
  }
  stages {
    stage('Build') {
      steps {
        sh 'mvn clean package'
      }
    }
    stage('javadoc') {
      steps {
        sh 'mvn javadoc:javadoc'
      }
    }
  }
}
