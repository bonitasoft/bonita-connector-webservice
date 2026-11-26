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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.bonitasoft.engine.connector.ConnectorException;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author Emmanuel Duchastenier
 */
public class SecureWSConnectorUnitTest {

    final SecureWSConnector webservice = new SecureWSConnector();

    @Test
    public void getEnvelopeBodyContent_should_get_body_on_non_formatted_xml() throws Exception {
        // given:
        String myXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
                "<soap-env:Envelope" +
                " xmlns:soap-env=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soap-env:Body>" +
                "<c />" +
                "</soap-env:Body>" +
                "</soap-env:Envelope>";

        // when:
        final Node envelopeBodyContent = webservice.getEnvelopeBodyContent(buildXml(myXml));

        // then:
        assertThat(envelopeBodyContent.getLocalName()).as(envelopeBodyContent.getNodeName()).isEqualTo("c");
    }

    @Test
    public void getEnvelopeBodyContent_should_get_body_on_formatted_xml() throws Exception {
        // given:
        String myXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<soap-env:Envelope xmlns:soap-env=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "   <soap-env:Body>\n" +
                "       <formatted />\n" +
                "   </soap-env:Body>\n" +
                "</soap-env:Envelope>\n";

        // when:
        final Node envelopeBodyContent = webservice.getEnvelopeBodyContent(buildXml(myXml));

        // then:
        assertThat(envelopeBodyContent.getLocalName()).as(envelopeBodyContent.getNodeName()).isEqualTo("formatted");
    }

    @Test
    public void getEnvelopeBodyContent_should_get_body_on_xml_with_comments() throws Exception {
        // given:
        String myXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<soap-env:Envelope xmlns:soap-env=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "   <soap-env:Body><!-- This is the response of the Webservice: --><realResponse /></soap-env:Body>" +
                "</soap-env:Envelope>";

        // when:
        final Node envelopeBodyContent = webservice.getEnvelopeBodyContent(buildXml(myXml));

        // then:
        assertThat(envelopeBodyContent.getLocalName()).as(envelopeBodyContent.getNodeName()).isEqualTo("realResponse");
    }

    @Test
    void should_restore_the_proxy_system_properties_when_the_call_fails() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("serviceNS", "http://hello.ws.connectors.bonitasoft.org/");
        parameters.put("serviceName", "HelloWorldImplService");
        parameters.put("portName", "HelloWorldImplPort");
        parameters.put("binding", "http://schemas.xmlsoap.org/wsdl/soap/http");
        parameters.put("endpointAddress", "http://localhost:1/HelloWorld");
        parameters.put("envelope", "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body/></soapenv:Envelope>");
        parameters.put("proxyProtocol", "HTTP");
        parameters.put("proxyHost", "localhost");
        parameters.put("proxyPort", "1");
        webservice.setInputParameters(parameters);
        webservice.validateInputParameters();

        assertThatThrownBy(webservice::execute).isInstanceOf(ConnectorException.class);

        assertThat(System.getProperty("http.proxyHost")).isNull();
        assertThat(System.getProperty("http.proxyPort")).isNull();
    }

    private Document buildXml(String xml) throws Exception {
        final DOMResult result = new DOMResult();
        webservice.getTransformer().transform(new StreamSource(new StringReader(xml)), result);
        return (Document) result.getNode();
    }
}
