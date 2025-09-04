// Jenkinsfile
pipeline {
    agent any

    tools {
        nodejs 'NodeJS-18' 
    }

    environment {
        PROJECT_NAME        = 'my-node-app'
        AWS_ACCESS_KEY_ID   = credentials('aws-access-key-id')
        AWS_SECRET_ACCESS_KEY = credentials('aws-secret-access-key')
        AWS_REGION          = 'us-east-1'
        SONAR_SERVER_URL    = 'http://your-sonarqube-server-ip:9000' // Manually update this IP after terraform apply
        SONAR_LOGIN_TOKEN   = credentials('sonarqube-token')
        DOCKER_REGISTRY     = 'your-dockerhub-username'
        DOCKER_CREDENTIALS  = credentials('dockerhub-credentials')
    }

    stages {
        stage('Initial Cleanup') {
            steps {
                cleanWs()
            }
        }

        stage('🚚 Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('📦 Install Dependencies') {
            steps {
                dir('app') {
                    sh 'npm install'
                }
            }
        }

        stage('🔍 Code Quality Analysis') {
            steps {
                withSonarQubeEnv('MySonarQubeServer') {
                    dir('app') {
                        sh """
                          sonar-scanner \
                            -Dsonar.projectKey=${PROJECT_NAME} \
                            -Dsonar.sources=. \
                            -Dsonar.host.url=${SONAR_SERVER_URL} \
                            -Dsonar.login=${SONAR_LOGIN_TOKEN}
                        """
                    }
                }
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('🐳 Build & Push Docker Image') {
            steps {
                script {
                    def dockerImage = docker.build("${DOCKER_REGISTRY}/${PROJECT_NAME}:${env.BUILD_NUMBER}", "-f app/nodejsdockerfile ./app")
                    docker.withRegistry("https://index.docker.io/v1/", DOCKER_CREDENTIALS) {
                        dockerImage.push()
                    }
                }
            }
        }

        stage('🚢 Deploy to Kubernetes') {
            steps {
                // Use the Credentials Binding plugin to access the kubeconfig file securely
                withCredentials([file(credentialsId: 'kubeconfig-creds', variable: 'KUBECONFIG_FILE')]) {
                    script {
                        // Dynamically update the image tag in the deployment YAML
                        sh "sed -i 's|image:.*|image: ${DOCKER_REGISTRY}/${PROJECT_NAME}:${env.BUILD_NUMBER}|g' kubernetes/deployment.yml"
                        
                        // Tell kubectl to use our specific, securely-provided config file
                        sh 'kubectl --kubeconfig ${KUBECONFIG_FILE} apply -f kubernetes/deployment.yml'
                        sh 'kubectl --kubeconfig ${KUBECONFIG_FILE} apply -f kubernetes/service.yml'
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline finished.'
        }
        success {
            echo 'Pipeline succeeded!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}