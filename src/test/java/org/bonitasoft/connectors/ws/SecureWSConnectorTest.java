package org.bonitasoft.connectors.ws;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.soap.SOAPBinding;

import org.bonitasoft.engine.connector.ConnectorException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import junit.framework.Assert;

public class SecureWSConnectorTest {

    private static Server server;

    protected static final Logger LOG = Logger.getLogger(SecureWSConnectorTest.class.getName());

    @Rule
    public TestRule testWatcher = new TestWatcher() {

        @Override
        public void starting(final Description d) {
            LOG.info("==== Starting test: " + SecureWSConnectorTest.class.getName() + "." + d.getMethodName() + "() ====");
        }

        @Override
        public void failed(final Throwable e, final Description d) {
            LOG.info("==== Failed test: " + SecureWSConnectorTest.class.getName() + "." + d.getMethodName() + "() ====");
        }

        @Override
        public void succeeded(final Description d) {
            LOG.info("==== Succeeded test: " + SecureWSConnectorTest.class.getName() + "." + d.getMethodName() + "() ====");
        }

    };

    @BeforeClass
    public static void setUp() throws Exception {
        server = new Server(9002);
        server.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void testCustomer() throws Exception {
        final StringBuilder request = new StringBuilder("");

        request.append(
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:man=\"http://www.orangecaraibe.com/soa/v2/Interfaces/ManageCustomerOrderInternal\">");
        request.append("<soapenv:Header/>");
        request.append("<soapenv:Body>");
        request.append("  <man:executeStep>");
        request.append("    <!--Optional:-->");
        request.append("    <man:processStepId>7586</man:processStepId>");
        request.append("    <man:processStepDate>20110713</man:processStepDate>");
        request.append("  </man:executeStep>");
        request.append("</soapenv:Body>");
        request.append("</soapenv:Envelope>");

        final String response = execute(request.toString(), SOAPBinding.SOAP11HTTP_BINDING, "http://localhost:9002/Customer",
                "ManageCustomerOrderInternalImplService", "ManageCustomerOrderInternalImplPort",
                "http://hello.cxf.ws.connectors.bonitasoft.org/", null,
                "guest", "guest");
        assertTrue(response, response.contains("false"));

    }

    @Test
    public void testBasicHTTPAuth() throws Exception {

        final StringBuilder request = new StringBuilder("");
        request.append(
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:spr=\"http://hello.cxf.ws.connectors.bonitasoft.org/\">");
        request.append(" <soapenv:Header/>");
        request.append(" <soapenv:Body>");
        request.append("    <spr:sayHi>");
        request.append("       <arg0>Rodrigue test</arg0>");
        request.append("    </spr:sayHi>");
        request.append(" </soapenv:Body>");
        request.append("</soapenv:Envelope>");

        final String response = execute(request.toString(), SOAPBinding.SOAP11HTTP_BINDING,
                "http://localhost:9002/HelloWorld", "HelloWorldImplService",
                "HelloWorldImplPort", "http://hello.cxf.ws.connectors.bonitasoft.org/", null, "guest", "guest");
        assertTrue(response, response.contains("Rodrigue test"));

    }

    @Test
    public void testHTTPHeaderOK() throws Exception {

        final StringBuilder request = new StringBuilder("");
        request.append(
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:spr=\"http://hello.cxf.ws.connectors.bonitasoft.org/\">");
        request.append(" <soapenv:Header/>");
        request.append(" <soapenv:Body>");
        request.append("    <spr:sayHi>");
        request.append("       <arg0>Rodrigue test</arg0>");
        request.append("    </spr:sayHi>");
        request.append(" </soapenv:Body>");
        request.append("</soapenv:Envelope>");

        final String headerName = "testName";
        final String headerValue = "testValue";

        final Map<String, List<String>> requestHeaders = new HashMap<>();
        final List<String> header = new ArrayList<>();
        header.add(headerValue);
        requestHeaders.put(headerName, header);

        execute(request.toString(), SOAPBinding.SOAP11HTTP_BINDING, "http://localhost:9002/HelloHeader",
                "HelloHeaderImplService", "HelloWorldImplPort",
                "http://hello.cxf.ws.connectors.bonitasoft.org/", null, "guest", "guest", requestHeaders);
    }

    @Test
    public void testHTTPHeaderOK2() throws Exception {
        final StringBuilder request = new StringBuilder("");
        request.append(
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:spr=\"http://hello.cxf.ws.connectors.bonitasoft.org/\">");
        request.append(" <soapenv:Header/>");
        request.append(" <soapenv:Body>");
        request.append("    <spr:sayHi>");
        request.append("       <arg0>Rodrigue test</arg0>");
        request.append("    </spr:sayHi>");
        request.append(" </soapenv:Body>");
        request.append("</soapenv:Envelope>");
        final String headerName = "testName";
        final String headerValue = "testValue";
        final List<String> header = new ArrayList<>();
        final Map<String, List<String>> requestHeaders = new HashMap<>();
        header.add(headerValue);
        requestHeaders.put(headerName, header);

        execute(request.toString(), SOAPBinding.SOAP11HTTP_BINDING, "http://localhost:9002/HelloHeader",
                "HelloHeaderImplService", "HelloWorldImplPort",
                "http://hello.cxf.ws.connectors.bonitasoft.org/", null, "guest", "guest", requestHeaders);
    }

    @Test(expected = ConnectorException.class)
    public void testHTTPHeaderKO() throws Exception {

        final StringBuilder request = new StringBuilder("");
        request.append(
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:spr=\"http://hello.cxf.ws.connectors.bonitasoft.org/\">");
        request.append(" <soapenv:Header/>");
        request.append(" <soapenv:Body>");
        request.append("    <spr:sayHi>");
        request.append("       <arg0>Rodrigue test</arg0>");
        request.append("    </spr:sayHi>");
        request.append(" </soapenv:Body>");
        request.append("</soapenv:Envelope>");

        final String headerName = "testName";
        final String headerValue = "testValue2";

        final Map<String, List<String>> requestHeaders = new HashMap<>();
        final List<String> header = new ArrayList<>();
        header.add(headerValue);
        requestHeaders.put(headerName, header);

        execute(request.toString(), SOAPBinding.SOAP11HTTP_BINDING, "http://localhost:9002/HelloHeader",
                "HelloHeaderImplService", "HelloWorldImplPort",
                "http://hello.cxf.ws.connectors.bonitasoft.org/", null, "guest", "guest", requestHeaders);
        Assert.fail("This call should fail...");

    }

    @Test
    public void testReadTimeoutOK() throws Exception {

        // ms
        final long timeout = 10000;
        final long timeToWait = 2000;

        final StringBuilder request = new StringBuilder("");
        request.append(
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:spr=\"http://hello.cxf.ws.connectors.bonitasoft.org/\">");
        request.append(" <soapenv:Header/>");
        request.append(" <soapenv:Body>");
        request.append("    <spr:sayHi>");
        request.append("       <arg0>" + timeToWait + "</arg0>");
        request.append("    </spr:sayHi>");
        request.append(" </soapenv:Body>");
        request.append("</soapenv:Envelope>");

        final List<String> timeoutList = Collections.singletonList(String.valueOf(timeout));
        final Map<String, List<String>> requestHeaders = Collections.singletonMap("com.sun.xml.ws.request.timeout",
                timeoutList);

        execute(request.toString(), SOAPBinding.SOAP11HTTP_BINDING, "http://localhost:9002/HelloTimeout",
                "HelloWorldImplService", "HelloWorldImplPort",
                "http://hello.cxf.ws.connectors.bonitasoft.org/", null, "guest", "guest", requestHeaders);
    }

    private String execute(final String request, final String binding, final String endpoint, final String service,
            final String port, final String ns,
            final String soapAction, final String username, final String password) throws Exception {
        return execute(request, binding, endpoint, service, port, ns, soapAction, username, password, null);
    }

    private String execute(final String request, final String binding, final String endpoint, final String service,
            final String port, final String ns,
            final String soapAction, final String username, final String password,
            final Map<String, List<String>> requestHeaders) throws Exception {
        return execute(request, binding, endpoint, service, port, ns, soapAction, username, password, requestHeaders, null);
    }

    private String execute(final String request, final String binding, final String endpoint, final String service,
            final String port, final String ns,
            final String soapAction, final String username, final String password,
            final Map<String, List<String>> requestHeaders,
            final List<List<Object>> requestHeadersAsList) throws Exception {

        if (requestHeadersAsList != null && requestHeaders != null) {
            throw new RuntimeException("only one of requestHeaders and requestHeadersAsList can be specified");
        }

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("envelope", request);
        parameters.put("binding", binding);
        parameters.put("endpointAddress", endpoint);
        parameters.put("serviceName", service);
        parameters.put("portName", port);
        parameters.put("serviceNS", ns);
        parameters.put("soapAction", soapAction);
        parameters.put("userName", username);
        parameters.put("password", password);
        parameters.put("buildResponseDocumentEnvelope", true);
        parameters.put("buildResponseDocumentBody", true);
        parameters.put("printRequestAndResponse", true);

        if (requestHeaders != null) {
            final List<List<Object>> requestHeadersList = new ArrayList<>();
            for (final String key : requestHeaders.keySet()) {
                final List<Object> row = new ArrayList<>();
                row.add(key);
                row.add(requestHeaders.get(key));
                requestHeadersList.add(row);
            }

            parameters.put("httpHeaders", requestHeadersList);
        } else {
            parameters.put("httpHeaders", null);
        }

        final SecureWSConnector webservice = new SecureWSConnector();
        webservice.setInputParameters(parameters);
        webservice.validateInputParameters();
        final Map<String, Object> outputs = webservice.execute();

        final Source response = (Source) outputs.get("sourceResponse");
        final String resultAsString = parse(response);
        printResponse(resultAsString);
        return resultAsString;
    }

    private String parse(final Source response) throws TransformerFactoryConfigurationError, TransformerException {
        assertNotNull(response);
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        final StringWriter writer = new StringWriter();
        final StreamResult result = new StreamResult(writer);
        transformer.transform(response, result);
        return writer.toString();
    }

    private void printResponse(final String response) {
        assertNotNull(response);
        System.out.println("response=\n" + response);
    }

}
