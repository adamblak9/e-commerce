pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    environment {
        APP_NAME     = 'commerce'
        DOCKER_IMAGE = 'adam969/commerce'
        DOCKER_TAG   = "${BUILD_NUMBER}"
        COMPOSE_FILE = 'docker-compose.yml'
        DOCKER_CREDS = 'dockerhub-credentials'
        SLACK_CREDS  = 'slack-webhook-url'
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {

        stage('Checkout') {
            steps {
                echo '📥 Pulling latest code...'
                checkout scm
                script {
                    env.GIT_SHORT_SHA = bat(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim().readLines().last()
                    echo "Commit: ${env.GIT_SHORT_SHA}"
                }
            }
        }

        stage('Build') {
            steps {
                echo '🔨 Building WAR with Maven...'
                bat 'mvn clean package -DskipTests=true --batch-mode'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.war', fingerprint: true
                }
            }
        }

        stage('Test') {
            steps {
                echo '🧪 Running tests...'
                bat 'mvn test --batch-mode'
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                echo '🐳 Building and pushing Docker image...'
                script {
                    docker.withRegistry('https://index.docker.io/v1/', DOCKER_CREDS) {
                        def image = docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                        image.push()
                        image.push('latest')
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                echo '🚀 Deploying with docker-compose...'
                bat """
                    set DOCKER_TAG=${DOCKER_TAG}
                    set DOCKER_IMAGE=${DOCKER_IMAGE}
                    docker-compose -f ${COMPOSE_FILE} down
                    docker-compose -f ${COMPOSE_FILE} up -d --build
                    docker-compose -f ${COMPOSE_FILE} ps
                """
            }
        }

        stage('Notify') {
            steps {
                script { notifySlack('SUCCESS') }
            }
        }
    }

    post {
        failure {
            script { notifySlack('FAILURE') }
        }
        always {
            bat 'docker image prune -f'
            cleanWs()
        }
    }
}

def notifySlack(String status) {
    def color   = status == 'SUCCESS' ? '#36a64f' : '#ff0000'
    def message = "${status} - commerce | Build #${env.BUILD_NUMBER} | Commit: ${env.GIT_SHORT_SHA} | ${env.BUILD_URL}"
    def payload = groovy.json.JsonOutput.toJson([
        attachments: [[
            color: color,
            text: message
        ]]
    ])

    // Write payload to a temp file to avoid Windows variable escaping issues
    writeFile file: 'slack_payload.json', text: payload

    withCredentials([string(credentialsId: SLACK_CREDS, variable: 'WEBHOOK')]) {
        bat 'curl -s -X POST "%WEBHOOK%" -H "Content-type: application/json" --data @slack_payload.json'
    }
}