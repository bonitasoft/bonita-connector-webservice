#!/usr/bin/env groovy

timestamps {
    ansiColor('xterm') {
        node {
            stage('Setup') {
                 checkout scm
            }

            stage('Build') {
                try {
                    sh "./mvnw verify"
                } finally {
                    junit allowEmptyResults: false, testResults: '**/target/*-reports/*.xml'
                }
            }

            stage('Archive') {
                archiveArtifacts '**/target/*.zip'
            }
        }
    }
}
