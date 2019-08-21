# Webservice Connector [![Build Status](https://travis-ci.org/bonitasoft/bonita-connector-webservice.svg?branch=master)](https://travis-ci.org/bonitasoft/bonita-connector-webservice)

Enables interactions in your BonitaBPM processes with a SOAP Webservices.

## Description

This connector provides a webservice connector implementation. 

## Build

__Clone__ or __fork__ this repository, then at the root of the project run:

`mvn clean verify`

## Release

Before releasing a new version make sure that the version of `.def` files are consistent with the implementation. In order to create a new release of the connector use the [_maven release plugin_](http://maven.apache.org/maven-release/maven-release-plugin/):

`mvn release:prepare release:perform -Darguments="-DaltDeploymentRepository=<your_deployment_repo>`"

It creates a new git _tag_ and update the current development version. New tags are automatically built on our CI infrastructure.
