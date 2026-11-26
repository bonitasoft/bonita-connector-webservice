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

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.web.client.BonitaClient;
import org.bonitasoft.web.client.api.ArchivedProcessInstanceApi;
import org.bonitasoft.web.client.api.ProcessApi.SearchProcessesQueryParams;
import org.bonitasoft.web.client.api.ProcessInstanceApi;
import org.bonitasoft.web.client.api.ProcessInstanceVariableApi;
import org.bonitasoft.web.client.exception.NotFoundException;
import org.bonitasoft.web.client.model.ArchivedProcessInstance;
import org.bonitasoft.web.client.model.ProcessDefinition;
import org.bonitasoft.web.client.model.User;
import org.bonitasoft.web.client.model.UserCreateRequest;
import org.bonitasoft.web.client.services.policies.ProcessImportPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

public class BonitaContainer extends GenericContainer<BonitaContainer> {

    private static final String TECH_USER = "install";
    private static final String TECH_PASSWORD = "install";
    private static final Logger LOGGER = LoggerFactory.getLogger(BonitaContainer.class);
    private static final String DEFAULT_USER = "test.user";
    private static final String DEFAULT_PASSWORD = "bpm";
    private User user;

    public BonitaContainer() {
        this(System.getProperty("bonita.image"));
    }

    public BonitaContainer(String image) {
        super(requireNonNull(image, "Bonita image not defined (bonita.image system property) !"));
        withExposedPorts(8080);
        // a Bonita start easily exceeds the 60 s default on a loaded machine or CI runner
        waitingFor(Wait.forHttp("/bonita").withStartupTimeout(Duration.ofMinutes(5)));
        withLogConsumer(new Slf4jLogConsumer(LOGGER));
        withAccessToHost(true);
    }

    @Override
    public void start() {
        super.start();
        user = createDefaultUser();
    }

    public User getDefaultUser() {
        return user;
    }

    private User createDefaultUser() {
        var client = newClient();
        try {
            client.login(TECH_USER, TECH_PASSWORD);
            var user = client.users().createUser(new UserCreateRequest()
                    .userName(DEFAULT_USER)
                    .password(DEFAULT_PASSWORD)
                    .passwordConfirm(DEFAULT_PASSWORD)
                    .firstname("")
                    .lastname("")
                    .enabled(Boolean.TRUE.toString()));
            LOGGER.info("Default user created.");
            return user;
        } finally {
            client.logout();
        }
    }

    public void installProcess(BusinessArchive bar) {
        File processFile = null;
        var client = newClient();
        try {
            processFile = Files.createTempFile("process", ".bar").toFile();
            processFile.delete();
            BusinessArchiveFactory.writeBusinessArchiveToFile(bar, processFile);
            client.login(TECH_USER, TECH_PASSWORD);
            client.processes().importProcess(processFile, ProcessImportPolicy.REPLACE_DUPLICATES);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (processFile != null) {
                processFile.delete();
            }
            client.logout();
        }
    }

    public long startProcess(String processName) {
        var client = newClient();
        try {
            client.login(TECH_USER, TECH_PASSWORD);
            List<ProcessDefinition> result = client.processes()
                    .searchProcesses(new SearchProcessesQueryParams().p(0).c(1).f(List.of("name=" + processName)));
            if (result.isEmpty()) {
                throw new IllegalArgumentException(String.format("Process '%s' not found.", processName));
            }
            return Long.valueOf(client.processes().startProcess(result.get(0).getId(), Map.of()).getCaseId());
        } finally {
            client.logout();
        }
    }

    private BonitaClient newClient() {
        return BonitaClient
                .builder(String.format("http://%s:%s/bonita", getHost(), getFirstMappedPort()))
                .build();
    }

    public Callable<String> pollStateOf(long caseId) {
        return () -> {
            var client = newClient();
            client.login(TECH_USER, TECH_PASSWORD);
            try {
                var instance = client.get(ProcessInstanceApi.class).getProcessInstanceById(String.valueOf(caseId), (String) null);
                return instance.getState().name().toLowerCase();
            } catch (NotFoundException e) {
                return getCompletedProcess(client, String.valueOf(caseId)).getState().name().toLowerCase();
            }finally {
                client.logout();
            }
        };
    }
    
    private ArchivedProcessInstance getCompletedProcess(BonitaClient client, String id) {
        var archivedInstances = client.get(ArchivedProcessInstanceApi.class)
                .searchArchivedProcessInstances(
                        new ArchivedProcessInstanceApi.SearchArchivedProcessInstancesQueryParams()
                                .c(1)
                                .p(0)
                                .f(List.of("caller=any", "sourceObjectId=" + id)));
        if (!archivedInstances.isEmpty()) {
            return archivedInstances.get(0);
        }
        return null;
    }

    public String getProcessVariableValue(long caseId, String variableName) {
        var client = newClient();
        try {
            client.login(TECH_USER, TECH_PASSWORD); 
            return client.get(ProcessInstanceVariableApi.class)
                    .getVariableByProcessInstanceId(String.valueOf(caseId), variableName)
                    .getValue();
        }finally {
            client.logout();
        }
      
    }

}
