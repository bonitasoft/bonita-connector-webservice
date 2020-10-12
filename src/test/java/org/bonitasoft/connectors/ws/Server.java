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
