timestamps {
    ansiColor('xterm') {
        node {
            stage('Setup') {
                checkout scm
            }

            stage('Release version') {
                sh './mvnw release:prepare -B'
            }
        }
    }
}