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
    agent any // 1. Agent: Specifies where the pipeline will run. 'any' means Jenkins can use any available agent/node.

    environment { // 2. Environment: Defines environment variables for the pipeline.
        // Jenkins credentials configuration
        DOCKER_HUB_CREDENTIALS = credentials('dockerhub_credentials') // Retrieves the Docker Hub credentials stored in Jenkins with the ID 'dockerhub_credentials'.
                                                                    // The actual username and password will be available in variables like DOCKER_HUB_CREDENTIALS_USR and DOCKER_HUB_CREDENTIALS_PSW.
        // Docker Hub Repository's name
        DOCKER_IMAGE = 'vanndoublen/teedy' // IMPORTANT: Replace 'xx' with YOUR Docker Hub username. This is the name of the image to be built and pushed.
        DOCKER_TAG = "${env.BUILD_NUMBER}" // Uses the Jenkins built-in BUILD_NUMBER as the Docker image tag (e.g., 1, 2, 3...).
    }

    stages { // 3. Stages: Defines the different stages of your pipeline.

        stage('Build') { // 4. Build Stage: Compiles the application (if necessary).
            steps {
                checkout scmGit( // Checks out code from a Git repository.
                    branches: [[name: '*/master']], // Specifies the branch to checkout (master branch here).
                    extensions: [],
                    userRemoteConfigs: [[url: 'https://github.com/vanndoublen/Teedy.git']] // IMPORTANT: Replace with YOUR Git repository URL for the Teedy project.
                )
                sh 'mvn -B -DskipTests clean package' // Assumes Teedy is a Maven project. This command cleans, compiles, and packages the application (likely creating a .war file).
                                                    // -B: Batch mode (non-interactive).
                                                    // -DskipTests: Skips running tests (for faster build in this example).
            }
        }

        // Building Docker images
        stage('Building image') { // 5. Building Docker Image Stage
            steps {
                script { // Allows running Groovy script code.
                    // assume Dockerfile locate at root
                    docker.build("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}") // Uses the Docker Pipeline plugin's 'docker.build()' command.
                                                                        // It builds an image using the Dockerfile in the current directory (workspace root).
                                                                        // The image will be named, e.g., 'your_username/teedy-app:1'.
                }
            }
        }

        // Uploading Docker images into Docker Hub
        stage('Upload image') { // 6. Upload Image Stage
            steps {
                script {
                    // sign in Docker Hub
                    docker.withRegistry('https://registry.hub.docker.com', 'DOCKER_HUB_CREDENTIALS') { // Uses 'docker.withRegistry()' to authenticate with Docker Hub.
                                                                                                    // 'DOCKER_HUB_CREDENTIALS' is the Jenkins credential ID.
                        // push image
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push() // Pushes the tagged image to Docker Hub.

                        //: optional: label latest
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push('latest') // Also pushes the same image with the 'latest' tag.
                    }
                }
            }
        }

        // Running Docker container
        stage('Run containers') { // 7. Run Containers Stage (Page 10)
            steps {
                script {
                    // stop then remove containers if exists
                    sh 'docker stop teedy-container-8081 || true' // Stops a container named 'teedy-container-8081' if it exists. '|| true' prevents the pipeline from failing if the container doesn't exist.
                    sh 'docker rm teedy-container-8081 || true'   // Removes the container if it exists.

                    // run Container
                    docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").run( // Runs a new container from the just-built and pushed image.
                        '--name teedy-container-8081 -d -p 8081:8080' // Container name, detached mode, port mapping (host 8081 to container 8080).
                    )
                }
            }
        }
    }


}
