pipeline {
  agent {
    docker {
      image 'maven:3.6.1-jdk-8-alpine'
      args '-v $HOME/.m2:/root/.m2 -m 3g -e MAVEN_OPTS="-Xmx300m"'
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
