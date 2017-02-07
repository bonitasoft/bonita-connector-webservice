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

public class ServerThread extends Thread {

    private final org.mortbay.jetty.Server server;

    private boolean startFailed = false;

    public ServerThread(final org.mortbay.jetty.Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            server.start();
            System.out.println("Server ready...");
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
