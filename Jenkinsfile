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
             sh "mvn clean verify"
        } finally {
            junit allowEmptyResults: false, testResults: '**/target/*-reports/*.xml'
        }
    }

    stage 'Archive', {
        archive '**/target/*.zip'
    }
}



