/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.bonitasoft.connectors.ws;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.jetty.webapp.WebAppContext;

public class Server {

    private static final Logger LOG = Logger.getLogger(Server.class.getName());

    private static final long TIMEOUT = 3 * 60000;

    private final ServerThread thread;

    public Server() throws IOException {
        this(9002);
    }

    public Server(final int connectorPort) throws IOException {
        final org.mortbay.jetty.Server server = new org.mortbay.jetty.Server();

        final SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(connectorPort);
        server.setConnectors(new Connector[] { connector });

        final WebAppContext webappcontext = new WebAppContext();
        webappcontext.setContextPath("/");

        webappcontext.setWar("src/test/resources/webapp");

        final HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[] { webappcontext, new DefaultHandler() });

        server.setHandler(handlers);

        final HashUserRealm myrealm = new HashUserRealm("MyRealm", "src/test/resources/org/bonitasoft/connectors/ws/realm.properties");
        server.setUserRealms(new UserRealm[] { myrealm });

        thread = new ServerThread(server);
    }

    public void start() throws Exception {
        thread.start();
        LOG.info("Starting server...");
        final long startTime = System.currentTimeMillis();

        do {
            LOG.info("Waiting...");
            Thread.sleep(200);
        } while (!thread.isServerStarted() && !thread.isStartFailed() && System.currentTimeMillis() < startTime + TIMEOUT);
        if (System.currentTimeMillis() >= startTime + TIMEOUT) {
            throw new TimeoutException("Timeout starting the server");
        } else if (thread.isStartFailed()) {
            throw new Exception("Unable to start the server, see log");
        }
        LOG.info("Server started.");
        LOG.info("Waiting for WS to be deployed...");
        Thread.sleep(7000);
        LOG.info("Assuming WS are deployed.");
    }

    public void stop() throws Exception {
        thread.shutdown();
    }

}
