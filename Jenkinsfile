properties([
        [
            $class: 'jenkins.model.BuildDiscarderProperty', 
            strategy: [$class: 'LogRotator',numToKeepStr: '10', artifactNumToKeepStr: '10']
        ],
        [
            $class: 'ParametersDefinitionProperty',
            parameterDefinitions: [
                [$class: 'StringParameterDefinition', defaultValue: '', description: 'Some Description', name : 'MY_PARAM']
            ]
         ]
    ])

node {
	
	stage 'Checkout', {
	     checkout scm
	}

    stage 'Build', {
        try {
            def buildEnv =
            [
              "JAVA_HOME=${ tool 'OpenJDK 1.8.0' }",
              "PATH+MAVEN=${tool 'maven-3.3.9'}/bin:${env.JAVA_HOME}/bin",
            ]
            
            withEnv(buildEnv){
                print "$MY_PARAM"
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



