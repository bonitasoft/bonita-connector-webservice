properties([[$class: 'jenkins.model.BuildDiscarderProperty',
            strategy: [$class: 'LogRotator',
                        numToKeepStr: '10',
                        artifactNumToKeepStr: '10']]])

node {
	
	stage 'Checkout', {
	     checkout scm
	}

    stage 'Build', {
        try {
            def buildEnv =
            ["JAVA_HOME=${ tool 'OpenJDK 1.8.0' }",
              "PATH+MAVEN=${tool 'maven-3.3.9'}/bin:${env.JAVA_HOME}/bin",
            ]
            
            withEnv(buildEnv){
                sh "mvn clean verify"
            }
            
        } finally {
            junit allowEmptyResults: false, testResults: '**/target/*-reports/*.xml'
        }
    }

    stage 'Archive', {
        archive '**/target/*.zip'
    }
}



