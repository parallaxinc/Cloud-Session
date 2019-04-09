pipeline {
  agent any
  stages {
    stage('Deploy to Demo') {
      steps {
        echo 'Building application tarballs.'
        sh '''tar czf CloudSession-Pkg-${BUILD_NUMBER}.tar.gz config.py  Failures.py 
  FakeSecHead.py 
  run.py 
  Validation.py 
  app database'''
        sh 'tar czf CloudSession-Templates-${BUILD_NUMBER}.tar.gz templates'
        sh 'cp CloudSession-Pkg-${BUILD_NUMBER}.tar.gz CloudSession-Pkg-LATEST.tar.gz'
        sh 'cp CloudSession-Templates-${BUILD_NUMBER}.tar.gz CloudSession-Templates-LATEST.tar.gz '
      }
    }
  }
}