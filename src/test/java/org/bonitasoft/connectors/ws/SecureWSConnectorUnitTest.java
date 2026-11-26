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
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
        String proxyHostBefore = System.getProperty("http.proxyHost");
        String proxyPortBefore = System.getProperty("http.proxyPort");
        Map<String, Object> parameters = proxyParameters();
        parameters.put("proxyPort", "1");
        webservice.setInputParameters(parameters);
        webservice.validateInputParameters();

        assertThatThrownBy(webservice::execute).isInstanceOf(ConnectorException.class);

        assertThat(System.getProperty("http.proxyHost")).isEqualTo(proxyHostBefore);
        assertThat(System.getProperty("http.proxyPort")).isEqualTo(proxyPortBefore);
    }

    @Test
    void should_restore_the_proxy_system_properties_when_the_proxy_configuration_fails() throws Exception {
        String proxyHostBefore = System.getProperty("http.proxyHost");
        // no proxyPort: configuring the proxy fails after writing http.proxyHost
        // (execute() does not validate, as when a caller skips validateInputParameters)
        webservice.setInputParameters(proxyParameters());

        assertThatThrownBy(webservice::execute).isInstanceOf(NullPointerException.class);

        assertThat(System.getProperty("http.proxyHost")).isEqualTo(proxyHostBefore);
    }

    @ParameterizedTest
    @ValueSource(strings = { "proxyProtocol", "proxyPort" })
    void should_require_proxy_protocol_and_port_when_a_proxy_host_is_set(String missingInput) {
        Map<String, Object> parameters = proxyParameters();
        parameters.put("proxyPort", "3128");
        parameters.remove(missingInput);
        webservice.setInputParameters(parameters);

        assertThatThrownBy(webservice::validateInputParameters)
                .isInstanceOf(ConnectorValidationException.class)
                .hasMessageContaining(missingInput);
    }

    private static Map<String, Object> proxyParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("serviceNS", "http://hello.ws.connectors.bonitasoft.org/");
        parameters.put("serviceName", "HelloWorldImplService");
        parameters.put("portName", "HelloWorldImplPort");
        parameters.put("binding", "http://schemas.xmlsoap.org/wsdl/soap/http");
        parameters.put("endpointAddress", "http://localhost:1/HelloWorld");
        parameters.put("envelope", "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body/></soapenv:Envelope>");
        parameters.put("proxyProtocol", "HTTP");
        parameters.put("proxyHost", "localhost");
        return parameters;
    }

    private Document buildXml(String xml) throws Exception {
        final DOMResult result = new DOMResult();
        webservice.getTransformer().transform(new StreamSource(new StringReader(xml)), result);
        return (Document) result.getNode();
    }
}
