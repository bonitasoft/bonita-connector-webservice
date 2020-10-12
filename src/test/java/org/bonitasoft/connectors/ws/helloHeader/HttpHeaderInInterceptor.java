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
package org.bonitasoft.connectors.ws.helloHeader;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;

public class HttpHeaderInInterceptor extends AbstractSoapInterceptor {

    private static final Logger LOG = Logger.getLogger(HttpHeaderInInterceptor.class.getName());

    public HttpHeaderInInterceptor() {
        super(Phase.READ);
    }

    @Override
    public void handleMessage(final SoapMessage message) throws Fault {
        @SuppressWarnings("unchecked")
        final Map<String, List<String>> headers = (Map<String, List<String>>) message.get(org.apache.cxf.message.Message.PROTOCOL_HEADERS);
        LOG.info("HTTP HEADERS : " + headers);

        if (!headers.containsKey("testName")) {
            throw new Fault(new Exception("TestName not found"));
        }
        final List<String> testNameValue = headers.get("testName");
        if (testNameValue.size() != 1) {
            throw new Fault(new Exception("TestName contains more than 1 element"));
        }
        final String value = testNameValue.get(0);
        if (!"testValue".equals(value)) {
            throw new Fault(new Exception("TestName value is not equals to testValue: " + value));
        }
    }

}
