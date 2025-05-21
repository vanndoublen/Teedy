// pipeline {
//     agent any
//     stages {
//         stage('Clean') {
//             steps {
//                 sh 'mvn clean'
//             }
//         }
//         stage('Compile') {
//             steps {
//                 sh 'mvn compile'
//             }
//         }
//         stage('Test') {
//             steps {
//                 sh 'mvn test -Dmaven.test.failure.ignore=true'
//             }
//         }
//         stage('PMD') {
//             steps {
//                 sh 'mvn pmd:pmd'
//             }
//         }
//         stage('JaCoCo') {
//             steps {
//                 sh 'mvn jacoco:report'
//             }
//         }
//         stage('Javadoc') {
//             steps {
//                 sh 'mvn javadoc:javadoc'
//             }
//         }
//         stage('Site') {
//             steps {
//                 sh 'mvn site'
//             }
//         }
//         stage('Package') {
//             steps {
//                 sh 'mvn package -DskipTests'
//             }
//         }
//     }
//     post {
//         always {
//             archiveArtifacts artifacts: '**/target/site/**/*.*', fingerprint: true
//             archiveArtifacts artifacts: '**/target/**/*.jar', fingerprint: true
//             archiveArtifacts artifacts: '**/target/**/*.war', fingerprint: true
//             junit '**/target/surefire-reports/*.xml'
//         }
//     }
// }
pipeline {
    agent any
    tools {
        maven 'M3'
    }
    environment {
        // Define environment variables
        DOCKER_IMAGE = 'vanndoublen/teedy'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        // Use credentials binding for Docker Hub
        DOCKER_CREDENTIALS = credentials('dockerhub_credentials')
    }
    stages {
        stage('Build') {
            steps {
                checkout scmGit(
                    branches: [[name: '*/master']],
                    extensions: [],
                    userRemoteConfigs: [[url: 'https://github.com/vanndoublen/Teedy.git']]
                )
                sh 'mvn -B -DskipTests clean package'
            }
        }

        stage('Building image') {
            steps {
                script {
                    // Build Docker image
                    docker.build("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}")
                }
            }
        }

        stage('Upload image') {
            steps {
                script {
                    // Login to Docker Hub
                    sh "echo ${DOCKER_CREDENTIALS_PSW} | docker login -u ${DOCKER_CREDENTIALS_USR} --password-stdin"

                    // Push the image with build number tag
                    sh "docker push ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}"

                    // Tag and push as latest
                    sh "docker tag ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} ${env.DOCKER_IMAGE}:latest"
                    sh "docker push ${env.DOCKER_IMAGE}:latest"
                }
            }
        }

        stage('Cleanup existing containers') {
            steps {
                script {
                    // Find and stop any containers using the required ports
                    sh '''
                    # Find containers using port 8082
                    CONTAINERS_8082=$(docker ps -q --filter publish=8082)
                    if [ ! -z "$CONTAINERS_8082" ]; then
                        docker stop $CONTAINERS_8082 || true
                        docker rm $CONTAINERS_8082 || true
                    fi

                    # Find containers using port 8083
                    CONTAINERS_8083=$(docker ps -q --filter publish=8083)
                    if [ ! -z "$CONTAINERS_8083" ]; then
                        docker stop $CONTAINERS_8083 || true
                        docker rm $CONTAINERS_8083 || true
                    fi

                    # Find containers using port 8084
                    CONTAINERS_8084=$(docker ps -q --filter publish=8084)
                    if [ ! -z "$CONTAINERS_8084" ]; then
                        docker stop $CONTAINERS_8084 || true
                        docker rm $CONTAINERS_8084 || true
                    fi

                    # Also stop and remove any existing teedy containers
                    docker stop $(docker ps -q --filter name=teedy-container) || true
                    docker rm $(docker ps -a -q --filter name=teedy-container) || true
                    '''
                }
            }
        }

        stage('Run containers') {
            steps {
                script {
                    // Run three containers with different port mappings
                    sh "docker run -d -p 8082:8080 --name teedy-container-8082 ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} || echo 'Failed to start container on port 8082'"
                    sh "docker run -d -p 8083:8080 --name teedy-container-8083 ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} || echo 'Failed to start container on port 8083'"
                    sh "docker run -d -p 8084:8080 --name teedy-container-8084 ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} || echo 'Failed to start container on port 8084'"

                    // List all teedy containers
                    sh 'docker ps --filter "name=teedy-container"'
                }
            }
        }
    }
    post {
        always {
            // Logout from Docker Hub
            sh 'docker logout'
        }
    }
}
