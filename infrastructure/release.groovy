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
                        sh './mvnw -B release:prepare release:perform -Darguments="-DskipTests -DaltDeploymentRepository=internal.releases::default::http://repositories.rd.lan/maven/internal.releases"'
                  }
             }
        }
}
