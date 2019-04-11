pipeline {
  agent {
    docker {
      image 'maven:3.6-jdk-8'
      args '-v $HOME/.m2:/root/.m2 -u 0:0 -e JAVA_OPTS="-Xms64m -Xmx64m" -e MAVEN_OPTS="-Xms512m -Xmx512m -DargLine=${env.SUREFIRE_OPTS}" -e SUREFIRE_OPTS="-Xms512m -Xmx512m"'
    }
  }
  stages {
    stage('Build') {
      steps {
        sh 'mvn clean deploy'
      }
    }
    stage('javadoc') {
      steps {
        sh 'mvn javadoc:javadoc'
      }
    }
  }
}
