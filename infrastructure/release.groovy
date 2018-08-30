timestamps {
        node {
            stage('Setup') {
                checkout scm
            }

            stage('Release version') {
                  withCredentials([usernamePassword(
                            credentialsId: 'github',
                            passwordVariable: 'GIT_PASSWORD',
                            usernameVariable: 'GIT_USERNAME')]) {
                        sh "./mvnw -B release:prepare release:perform -Darguments=-DaltDeploymentRepository=${env.ALT_DEPLOYMENT_REPOSITORY_TAG}"
                  }
             }
        }
}
