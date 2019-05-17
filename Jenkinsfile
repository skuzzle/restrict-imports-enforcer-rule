pipeline {
  agent {
    docker {
      image 'maven:3.6-jdk-8-alpine'
      args '-v $HOME/.m2:/root/.m2 -m 1G -e MAVEN_OPTS="-Xmx300m"'
    }
  }
  stages {
    stage('Build') {
      steps {
        sh 'mvn clean package -DargLine="-Xmx300m"'
      }
    }
    stage('javadoc') {
      steps {
        sh 'mvn javadoc:javadoc'
      }
    }
  }
}
