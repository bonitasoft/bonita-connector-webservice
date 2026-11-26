/*
 * Copyright (C) 2009 - 2020 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.connectors.ws.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import jakarta.xml.ws.soap.SOAPBinding;

import org.bonitasoft.connectors.ws.Server;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor;
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.Testcontainers;
import org.w3c.dom.Document;

/**
 * Deploys the connector archive produced by the build in a Bonita runtime (Docker image given by the
 * {@code bonita.image} system property) and calls the test SOAP server from a process.
 * {@code connector.zip} overrides the archive under test, e.g. to check a previous release.
 */
class SecureWSConnectorRuntimeIT {

    private static final int SOAP_PORT = 9002;
    private static final String PROCESS_NAME = "WS_PROCESS";

    private static Server soapServer;
    private static BonitaContainer bonita;

    @BeforeAll
    static void start() throws Exception {
        soapServer = new Server(SOAP_PORT);
        soapServer.start();
        Testcontainers.exposeHostPorts(SOAP_PORT);
        bonita = new BonitaContainer();
        bonita.start();
    }

    @AfterAll
    static void stop() throws Exception {
        if (bonita != null) {
            bonita.stop();
        }
        if (soapServer != null) {
            soapServer.stop();
        }
    }

    @Test
    void should_call_a_soap_service_from_a_process() throws Exception {
        bonita.installProcess(businessArchive());
        long caseId = bonita.startProcess(PROCESS_NAME);

        // started: waiting on its user task; error: the connector failed
        await().atMost(Duration.ofMinutes(2)).until(bonita.pollStateOf(caseId), state -> !"initializing".equals(state));
        assertThat(bonita.pollStateOf(caseId).call()).as("process state (see the Bonita container logs on error)")
                .isEqualTo("started");
        assertThat(bonita.getProcessVariableValue(caseId, "response")).isEqualTo("Hello Bonita 12");
    }

    private BusinessArchive businessArchive() throws Exception {
        var exp = new ExpressionBuilder();
        var process = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, "1.0");
        process.addActor("system");
        process.addData("response", String.class.getName(), null);
        var connector = process.addConnector("ws", "webservice", "1.1.0", ConnectorEvent.ON_ENTER);
        Map.of("serviceNS", "http://hello.ws.connectors.bonitasoft.org/",
                "serviceName", "HelloWorldImplService",
                "portName", "HelloWorldImplPort",
                "endpointAddress", "http://host.testcontainers.internal:" + SOAP_PORT + "/HelloWorld",
                "binding", SOAPBinding.SOAP11HTTP_BINDING,
                "userName", "guest",
                "password", "guest",
                "envelope", "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\""
                        + " xmlns:spr=\"http://hello.ws.connectors.bonitasoft.org/\">"
                        + "<soapenv:Body><spr:sayHi><arg0>Bonita 12</arg0></spr:sayHi></soapenv:Body>"
                        + "</soapenv:Envelope>")
                .forEach((name, value) -> {
                    try {
                        connector.addInput(name, exp.createConstantStringExpression(value));
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                });
        connector.addInput("buildResponseDocumentBody", exp.createConstantBooleanExpression(true));
        connector.addOutput(new OperationBuilder().createSetDataOperation("response",
                exp.createGroovyScriptExpression("bodyText", "responseDocumentBody.documentElement.textContent",
                        String.class.getName(),
                        exp.createInputExpression("responseDocumentBody", Document.class.getName()))));
        // keeps the case open once the connector has run
        process.addUserTask("waiting task", "system");

        var bar = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process.done());
        try (var zip = new ZipFile(connectorArchive().toFile())) {
            for (ZipEntry entry : zip.stream().filter(e -> !e.isDirectory()).toList()) {
                var resource = new BarResource(Paths.get(entry.getName()).getFileName().toString(),
                        zip.getInputStream(entry).readAllBytes());
                if (entry.getName().endsWith(".impl")) {
                    bar.addConnectorImplementation(resource);
                } else if (entry.getName().startsWith("classpath/")) {
                    bar.addClasspathResource(resource);
                }
            }
        }
        var actorMapping = new ActorMapping();
        var system = new Actor("system");
        system.addUser(bonita.getDefaultUser().getUserName());
        actorMapping.addActor(system);
        bar.setActorMapping(actorMapping);
        return bar.done();
    }

    private static Path connectorArchive() {
        var override = System.getProperty("connector.zip");
        if (override != null) {
            return Paths.get(override);
        }
        try (var files = Files.list(Paths.get("target"))) {
            return files.filter(f -> f.getFileName().toString().endsWith("-secured-webservice.zip"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No connector archive in target/, run the package phase first"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
