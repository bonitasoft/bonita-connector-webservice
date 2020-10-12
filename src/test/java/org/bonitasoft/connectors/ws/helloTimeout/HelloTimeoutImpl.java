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
package org.bonitasoft.connectors.ws.helloTimeout;

import java.util.logging.Logger;
import javax.jws.WebService;

@WebService(endpointInterface = "org.bonitasoft.connectors.ws.helloTimeout.HelloTimeout")
public class HelloTimeoutImpl implements HelloTimeout {

    private static final Logger LOG = Logger.getLogger(HelloTimeoutImpl.class.getName());

    @Override
    public String sayHi(String text) throws InterruptedException {
        LOG.info("sayHi called");
        Thread.sleep(Long.valueOf(text).intValue());
        return "Hello Timeout";
    }

}
