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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.translate.LookupTranslator;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 */
public class SecureWSConnector extends AbstractConnector {

    private static final String KEY_VALUE_PATTERN = "%s: %s";

    private static final String HTTPS_PROXY_PORT = "https.proxyPort";

    private static final String HTTPS_PROXY_HOST = "https.proxyHost";

    private static final String HTTP_PROXY_PORT = "http.proxyPort";

    private static final String HTTP_PROXY_HOST = "http.proxyHost";

    private static final String SOCKS_PROXY_PORT = "socksProxyPort";

    private static final String SOCKS_PROXY_HOST = "socksProxyHost";

    private static final String SOCKS = "SOCKS";

    private static final String OUTPUT_RESPONSE_DOCUMENT_BODY = "responseDocumentBody";

    private static final String OUTPUT_RESPONSE_DOCUMENT_ENVELOPE = "responseDocumentEnvelope";

    private static final String OUTPUT_SOURCE_RESPONSE = "sourceResponse";

    private static final String PRINT_REQUEST_AND_RESPONSE = "printRequestAndResponse";

    private static final String PASSWORD = "password";

    private static final String USER_NAME = "userName";

    private static final String ONE_WAY_INVOKE = "oneWayInvoke";

    private static final String BUILD_RESPONSE_DOCUMENT_BODY = "buildResponseDocumentBody";

    private static final String BUILD_RESPONSE_DOCUMENT_ENVELOPE = "buildResponseDocumentEnvelope";

    private static final String BINDING = "binding";

    private static final String SOAP_ACTION = "soapAction";

    private static final String ENDPOINT_ADDRESS = "endpointAddress";

    private static final String ENVELOPE = "envelope";

    private static final String PORT_NAME = "portName";

    private static final String SERVICE_NAME = "serviceName";

    private static final String SERVICE_NS = "serviceNS";

    private static final String HTTP_HEADERS = "httpHeaders";

    private static final String PROXY_HOST = "proxyHost";

    private static final String PROXY_PORT = "proxyPort";

    private static final String PROXY_PROTOCOL = "proxyProtocol";

    private static final String PROXY_USER = "proxyUser";

    private static final String PROXY_PASSWORD = "proxyPassword";

    private static final Logger LOGGER = Logger.getLogger(SecureWSConnector.class.getName());

    private Transformer transformer;

    private LookupTranslator lookupTranslator;

    private Map<String, String> saveProxyConfiguration = new HashMap<>();

    public SecureWSConnector() {
        Map<CharSequence, CharSequence> escapeXml10Map = new HashMap<>();
        escapeXml10Map.put("\u0000", StringUtils.EMPTY);
        escapeXml10Map.put("\u0001", StringUtils.EMPTY);
        escapeXml10Map.put("\u0002", StringUtils.EMPTY);
        escapeXml10Map.put("\u0003", StringUtils.EMPTY);
        escapeXml10Map.put("\u0004", StringUtils.EMPTY);
        escapeXml10Map.put("\u0005", StringUtils.EMPTY);
        escapeXml10Map.put("\u0006", StringUtils.EMPTY);
        escapeXml10Map.put("\u0007", StringUtils.EMPTY);
        escapeXml10Map.put("\u0008", StringUtils.EMPTY);
        escapeXml10Map.put("\u000b", StringUtils.EMPTY);
        escapeXml10Map.put("\u000c", StringUtils.EMPTY);
        escapeXml10Map.put("\u000e", StringUtils.EMPTY);
        escapeXml10Map.put("\u000f", StringUtils.EMPTY);
        escapeXml10Map.put("\u0010", StringUtils.EMPTY);
        escapeXml10Map.put("\u0011", StringUtils.EMPTY);
        escapeXml10Map.put("\u0012", StringUtils.EMPTY);
        escapeXml10Map.put("\u0013", StringUtils.EMPTY);
        escapeXml10Map.put("\u0014", StringUtils.EMPTY);
        escapeXml10Map.put("\u0015", StringUtils.EMPTY);
        escapeXml10Map.put("\u0016", StringUtils.EMPTY);
        escapeXml10Map.put("\u0017", StringUtils.EMPTY);
        escapeXml10Map.put("\u0018", StringUtils.EMPTY);
        escapeXml10Map.put("\u0019", StringUtils.EMPTY);
        escapeXml10Map.put("\u001a", StringUtils.EMPTY);
        escapeXml10Map.put("\u001b", StringUtils.EMPTY);
        escapeXml10Map.put("\u001c", StringUtils.EMPTY);
        escapeXml10Map.put("\u001d", StringUtils.EMPTY);
        escapeXml10Map.put("\u001e", StringUtils.EMPTY);
        escapeXml10Map.put("\u001f", StringUtils.EMPTY);
        escapeXml10Map.put("\ufffe", StringUtils.EMPTY);
        escapeXml10Map.put("\uffff", StringUtils.EMPTY);
        lookupTranslator = new LookupTranslator(Collections.unmodifiableMap(escapeXml10Map));
    }

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        String serviceNS = (String) getInputParameter(SERVICE_NS);
        if (serviceNS == null) {
            throw new ConnectorValidationException("Service NS is required");
        }
        String serviceName = (String) getInputParameter(SERVICE_NAME);
        if (serviceName == null) {
            throw new ConnectorValidationException("Service Name is required");
        }
        String portName = (String) getInputParameter(PORT_NAME);
        if (portName == null) {
            throw new ConnectorValidationException("Port Name is required");
        }
        String envelope = (String) getInputParameter(ENVELOPE);
        if (envelope == null) {
            throw new ConnectorValidationException("Envelope is required");
        }
        String endpointAddress = (String) getInputParameter(ENDPOINT_ADDRESS);
        if (endpointAddress == null) {
            throw new ConnectorValidationException("endpointAddress is required");
        }
        String binding = (String) getInputParameter(BINDING);
        if (binding == null) {
            throw new ConnectorValidationException("binding is required");
        }
    }

    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        configureProxy();

        Dispatch<Source> dispatch = createDispatch();
        configureCredentials(dispatch);
        configureSoapAction(dispatch);
        configureHeaders(dispatch);

        String sanitizedEnvelope = retrieveAndSanitizeEnvelope();
        Source sourceResponse = invoke(dispatch, sanitizedEnvelope);

        restoreConfiguration();

        boolean buildResponseDocumentEnvelope = getAndLogOptionalBooleanParameter(BUILD_RESPONSE_DOCUMENT_ENVELOPE);
        boolean buildResponseDocumentBody = getAndLogOptionalBooleanParameter(BUILD_RESPONSE_DOCUMENT_BODY);
        Document responseDocumentEnvelope = null;
        Document responseDocumentBody = null;

        if (sourceResponse != null) {
            final DOMResult result = new DOMResult();
            try {
                getTransformer().transform(sourceResponse, result);
            } catch (TransformerException e) {
                throw new ConnectorException(e);
            }
            responseDocumentEnvelope = buildResponseDocumentEnvelope(result);
            if (buildResponseDocumentBody) {
                responseDocumentBody = buildResponseDocumentBody(responseDocumentEnvelope);
            }
            boolean printRequestAndResponse = getAndLogOptionalBooleanParameter(PRINT_REQUEST_AND_RESPONSE);
            if (printRequestAndResponse) {
                printRequestAndResponse(newSourceFromDOM(result),
                        buildResponseDocumentEnvelope,
                        buildResponseDocumentBody,
                        responseDocumentEnvelope,
                        responseDocumentBody);
            }
            setOutputParameter(OUTPUT_SOURCE_RESPONSE, newSourceFromDOM(result));
        }
        setOutputParameter(OUTPUT_RESPONSE_DOCUMENT_ENVELOPE,
                buildResponseDocumentEnvelope ? responseDocumentEnvelope : null);
        setOutputParameter(OUTPUT_RESPONSE_DOCUMENT_BODY, buildResponseDocumentBody ? responseDocumentBody : null);
    }

    private Source newSourceFromDOM(DOMResult result) {
        return new DOMSource(result.getNode());
    }

    private Dispatch<Source> createDispatch() {
        String serviceNS = (String) getAndLogInputParameter(SERVICE_NS);
        String serviceName = (String) getAndLogInputParameter(SERVICE_NAME);
        String portName = (String) getAndLogInputParameter(PORT_NAME);
        String binding = (String) getAndLogInputParameter(BINDING);
        String endpointAddress = (String) getAndLogInputParameter(ENDPOINT_ADDRESS);

        QName serviceQName = new QName(serviceNS, serviceName);
        QName portQName = new QName(serviceNS, portName);
        Service service = Service.create(serviceQName);
        service.addPort(portQName, binding, endpointAddress);

        Dispatch<Source> dispatch = service.createDispatch(portQName, Source.class, Service.Mode.MESSAGE);
        dispatch.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
        return dispatch;
    }

    private Source invoke(Dispatch<Source> dispatch, String sanitizedEnvelope) throws ConnectorException {
        boolean oneWayInvoke = getAndLogOptionalBooleanParameter(ONE_WAY_INVOKE);
        Source sourceResponse = null;
        try {
            Source message = new StreamSource(new StringReader(sanitizedEnvelope));
            if (oneWayInvoke) {
                dispatch.invokeOneWay(message);
            } else {
                sourceResponse = dispatch.invoke(message);
            }
        } catch (Exception e) {
            throw new ConnectorException("Exception trying to call remote webservice", e);
        }
        return sourceResponse;
    }

    private String retrieveAndSanitizeEnvelope() {
        String initialEnvelope = (String) getInputParameter(ENVELOPE);
        String sanitizedEnvelope = sanitizeString(initialEnvelope);
        if (!Objects.equals(initialEnvelope, sanitizedEnvelope)) {
            LOGGER.warning("Invalid XML characters have been detected in the envelope, they will be removed.");
        }
        LOGGER.info(() -> String.format(KEY_VALUE_PATTERN,ENVELOPE, sanitizedEnvelope));
        return sanitizedEnvelope;
    }

    private void configureHeaders(Dispatch<Source> dispatch) {
        @SuppressWarnings("unchecked")
        List<List<Object>> httpHeadersList = (List<List<Object>>) getInputParameter(HTTP_HEADERS);
        if (httpHeadersList != null) {
            Map<String, List<String>> httpHeadersMap = new HashMap<>();
            httpHeadersList.stream()
                    .filter(row -> row.size() == 2)
                    .forEach(row -> addHeader(httpHeadersMap, row));
            dispatch.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, httpHeadersMap);
        }
    }

    @SuppressWarnings("unchecked")
    private void addHeader(Map<String, List<String>> httpHeadersMap, List<Object> headerRow) {
        List<String> parameters = new ArrayList<>();
        Object value = headerRow.get(1);
        if (value instanceof Collection) {
            ((Collection<Object>) value).stream()
                    .map(Object::toString)
                    .forEach(parameters::add);
        } else {
            parameters.add(value.toString());
        }
        httpHeadersMap.put((String) headerRow.get(0), parameters);
    }

    private void configureSoapAction(Dispatch<Source> dispatch) {
        Object soapAction = getAndLogInputParameter(SOAP_ACTION);
        if (soapAction != null) {
            dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, true);
            dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, soapAction);
        }
    }

    private void configureCredentials(Dispatch<Source> dispatch) {
        Object authUserName = getInputParameter(USER_NAME);
        if (authUserName != null) {
            LOGGER.info(() -> String.format(KEY_VALUE_PATTERN, USER_NAME, authUserName));
            dispatch.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, authUserName);
            Object authPassword = getInputParameter(PASSWORD);
            dispatch.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, authPassword);
        }
    }

    /**
     * @return the Boolean value, or false if the value is null
     */
    private boolean getAndLogOptionalBooleanParameter(String parameterName) {
        Object value = getAndLogInputParameter(parameterName);
        return value != null && (boolean) value;
    }

    private Object getAndLogInputParameter(String parameterName) {
        Object value = getInputParameter(parameterName);
        LOGGER.info(() -> parameterName + ": " + value);
        return value;
    }

    private String sanitizeString(String stringToSanitize) {
        return lookupTranslator.translate(stringToSanitize);
    }

    private void restoreConfiguration() {
        for (Entry<String, String> entry : saveProxyConfiguration.entrySet()) {
            if (entry.getValue() != null) {
                System.setProperty(entry.getKey(), entry.getValue());
            } else {
                System.clearProperty(entry.getKey());
            }
        }
        Authenticator.setDefault(null);
    }

    private void configureProxy() {
        saveProxyConfiguration = saveProxyConfiguration();
        String host = (String) getInputParameter(PROXY_HOST);
        if (host == null || host.isEmpty()) {
            return;
        }
        LOGGER.info(() -> String.format(KEY_VALUE_PATTERN, PROXY_HOST, host));
        String protocol = (String) getAndLogInputParameter(PROXY_PROTOCOL);
        String port = (String) getAndLogInputParameter(PROXY_PORT);

        if (SOCKS.equals(protocol)) {
            System.setProperty(SOCKS_PROXY_HOST, host);
            LOGGER.info(() -> String.format("Setting environment variable: socksProxyHost=%s", host));
            System.setProperty(SOCKS_PROXY_PORT, port);
            LOGGER.info(() -> String.format("Setting environment variable: socksProxyPort=%s", port));
        } else {
            String hostKey = String.format("%s.proxyHost", protocol.toLowerCase());
            System.setProperty(hostKey, host);
            LOGGER.info(() -> String.format("Setting environment variable: %s=%s", hostKey,host));
            String portKey = String.format("%s.proxyPort", protocol.toLowerCase());
            System.setProperty(portKey, port);
            LOGGER.info(() -> String.format("Setting environment variable: %s=%s", portKey,port));
        }

        String user = (String) getAndLogInputParameter(PROXY_USER);
        String password = (String) getInputParameter(PROXY_PASSWORD);
        if (user != null && !user.isEmpty()) {
            Authenticator.setDefault(new Authenticator() {

                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user,
                            password != null ? password.toCharArray() : "".toCharArray());
                }

            });
        }
    }

    private Map<String, String> saveProxyConfiguration() {
        final Map<String, String> configuration = new HashMap<>();
        configuration.put(HTTP_PROXY_HOST, System.getProperty(HTTP_PROXY_HOST));
        configuration.put(HTTP_PROXY_PORT, System.getProperty(HTTP_PROXY_PORT));
        configuration.put(HTTPS_PROXY_HOST, System.getProperty(HTTPS_PROXY_HOST));
        configuration.put(HTTPS_PROXY_PORT, System.getProperty(HTTPS_PROXY_PORT));
        configuration.put(SOCKS_PROXY_HOST, System.getProperty(SOCKS_PROXY_HOST));
        configuration.put(SOCKS_PROXY_PORT, System.getProperty(SOCKS_PROXY_PORT));
        return configuration;
    }

    private Document buildResponseDocumentEnvelope(DOMResult result) {
        return result.getNode() instanceof Document ? 
                (Document) result.getNode() : result.getNode().getOwnerDocument();
    }

    private Document buildResponseDocumentBody(Document responseDocumentEnvelope) throws ConnectorException {
        Document responseDocumentBody = null;
        if (responseDocumentEnvelope != null) {
            try {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
                responseDocumentBody = documentBuilderFactory.newDocumentBuilder().newDocument();
            } catch (final ParserConfigurationException pce) {
                throw new ConnectorException(pce);
            }
            final Node bodyContent = getEnvelopeBodyContent(responseDocumentEnvelope);
            final Node clonedBodyContent = bodyContent.cloneNode(true);
            responseDocumentBody.adoptNode(clonedBodyContent);
            responseDocumentBody.importNode(clonedBodyContent, true);
            responseDocumentBody.appendChild(clonedBodyContent);
        }
        return responseDocumentBody;
    }

    private void printRequestAndResponse(Source sourceResponse, boolean buildResponseDocumentEnvelope,
            boolean buildResponseDocumentBody,
            Document responseDocumentEnvelope, Document responseDocumentBody) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            getTransformer().transform(sourceResponse, new StreamResult(os));
            if (buildResponseDocumentEnvelope) {
                getTransformer().transform(new DOMSource(responseDocumentEnvelope), new StreamResult(os));
            } else if (buildResponseDocumentBody) {
                getTransformer().transform(new DOMSource(responseDocumentEnvelope), new StreamResult(os));
                getTransformer().transform(new DOMSource(responseDocumentBody), new StreamResult(os));
            }
            LOGGER.info(os::toString);
        } catch (final TransformerException | IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    Node getEnvelopeBodyContent(final Document envelope) {
        final Node envelopeNode = envelope.getFirstChild();
        final NodeList children = envelopeNode.getChildNodes();
        Node envelopeBody = null;
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (child instanceof Element) {
                final Element element = (Element) child;
                if ("Body".equalsIgnoreCase(element.getLocalName())) {
                    envelopeBody = child;
                    break;
                }
            }
        }
        if (envelopeBody == null) {
            return envelopeNode;
        }

        final NodeList bodyChildren = envelopeBody.getChildNodes();
        for (int i = 0; i < bodyChildren.getLength(); i++) {
            final Node child = bodyChildren.item(i);
            if (child instanceof Element) {
                return child;
            }
        }

        return envelopeBody;
    }

    Transformer getTransformer() throws TransformerConfigurationException {
        if (transformer == null) {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        }
        return transformer;
    }
}
