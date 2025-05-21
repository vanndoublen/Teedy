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
                    // Use withCredentials instead of environment variables for Docker Hub authentication
                    withCredentials([usernamePassword(credentialsId: 'dockerhub_credentials',
                                                     usernameVariable: 'DOCKER_USERNAME',
                                                     passwordVariable: 'DOCKER_PASSWORD')]) {
                        // Log in to Docker Hub
                        sh "docker login -u ${DOCKER_USERNAME} -p ${DOCKER_PASSWORD}"

                        // Push the image
                        sh "docker push ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}"

                        // Optional: Push as latest
                        sh "docker tag ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} ${env.DOCKER_IMAGE}:latest"
                        sh "docker push ${env.DOCKER_IMAGE}:latest"
                    }
                }
            }
        }

stage('Run containers') {
    steps {
        script {
            // Stop and remove existing containers if they exist
            sh 'docker stop teedy-container-1 || true'
            sh 'docker rm teedy-container-1 || true'
            sh 'docker stop teedy-container-2 || true'
            sh 'docker rm teedy-container-2 || true'
            sh 'docker stop teedy-container-3 || true'
            sh 'docker rm teedy-container-3 || true'

            // Run three containers with different port mappings
            sh "docker run -d -p 8082:8080 --name teedy-container-1 ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}"
            sh "docker run -d -p 8083:8080 --name teedy-container-2 ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}"
            sh "docker run -d -p 8087:8080 --name teedy-container-3 ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}"

            // List all teedy containers
            sh 'docker ps --filter "name=teedy-container"'
        }
    }
}

    }
}
