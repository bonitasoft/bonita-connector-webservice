timestamps {
    ansiColor('xterm') {
        node {
            stage('Setup') {
                checkout scm
            }

            stage('Release version') {
                sh './mvnw -B release:prepare release:perform -Darguments="-DskipTests -DaltDeploymentRepository=internal.releases::default::http://repositories.rd.lan/maven/internal.releases"'
            }
        }
    }
}