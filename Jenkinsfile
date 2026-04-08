pipeline {
    agent any

    environment {
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
        DOG_API_MAX_RESPONSE_TIME_MS = '5000'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'chmod +x mvnw'
                        sh "./mvnw --batch-mode --no-transfer-progress clean test -Pregression -Ddog.api.maxResponseTimeMs=${DOG_API_MAX_RESPONSE_TIME_MS} -Dsurefire.rerunFailingTestsCount=1"
                    } else {
                        bat "call .\\mvnw.cmd --batch-mode --no-transfer-progress clean test -Pregression -Ddog.api.maxResponseTimeMs=${env.DOG_API_MAX_RESPONSE_TIME_MS} -Dsurefire.rerunFailingTestsCount=1"
                    }
                }
            }
        }

        stage('Generate Allure HTML') {
            steps {
                script {
                    if (isUnix()) {
                        sh './mvnw --batch-mode --no-transfer-progress allure:report'
                    } else {
                        bat 'call .\\mvnw.cmd --batch-mode --no-transfer-progress allure:report'
                    }
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            archiveArtifacts allowEmptyArchive: true, artifacts: 'target/allure-results/**,target/site/allure-maven-plugin/**,target/surefire-reports/**'
        }
    }
}
