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

import java.util.logging.Logger;

public class ServerThread extends Thread {

    private static final Logger LOG = Logger.getLogger(ServerThread.class.getName());

    private final org.eclipse.jetty.server.Server server;

    private boolean startFailed = false;

    public ServerThread(final  org.eclipse.jetty.server.Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            server.start();
            LOG.info("Server ready...");
            server.join();
        } catch (final Exception e) {
            startFailed = true;
            e.printStackTrace();
        }
    }

    public void shutdown() throws Exception {
        server.stop();
    }

    public boolean isServerStarted() {
        return server.isStarted();
    }

    /**
     * @return the startFailed
     */
    public boolean isStartFailed() {
        return startFailed;
    }

}
