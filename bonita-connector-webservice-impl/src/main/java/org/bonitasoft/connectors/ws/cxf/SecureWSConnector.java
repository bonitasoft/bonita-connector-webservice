/**
 * Copyright (C) 2009-2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.connectors.ws.cxf;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;

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

    private static final String OUTPUT_RESPONSE_DOCUMENT_BODY = "responseDocumentBody";

    private static final String OUTPUT_RESPONSE_DOCUMENT_ENVELOPE = "responseDocumentEnvelope";

    private static final String OUTPUT_SOURCE_RESPONSE = "sourceResponse";

    private static final String PRINT_REQUEST_AND_RESPONSE = "printRequestAndResponse";

    private static final String PASSWORD = "password";

    private static final String USER_NAME = "userName";

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

    private Logger LOGGER = Logger.getLogger(this.getClass().getName());

    private Transformer transformer;

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        final String serviceNS = (String) getInputParameter(SERVICE_NS);
        if (serviceNS == null) {
            throw new ConnectorValidationException("Service NS is required");
        }
        final String serviceName = (String) getInputParameter(SERVICE_NAME);
        if (serviceName == null) {
            throw new ConnectorValidationException("Service Name is required");
        }
        final String portName = (String) getInputParameter(PORT_NAME);
        if (portName == null) {
            throw new ConnectorValidationException("Port Name is required");
        }
        final String envelope = (String) getInputParameter(ENVELOPE);
        if (envelope == null) {
            throw new ConnectorValidationException("Envelope is required");
        }
        final String endpointAddress = (String) getInputParameter(ENDPOINT_ADDRESS);
        if (endpointAddress == null) {
            throw new ConnectorValidationException("endpointAddress is required");
        }
        final String binding = (String) getInputParameter(BINDING);
        if (binding == null) {
            throw new ConnectorValidationException("binding is required");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        final String serviceNS = (String) getInputParameter(SERVICE_NS);
        LOGGER.info(SERVICE_NS + " " + serviceNS);
        final String serviceName = (String) getInputParameter(SERVICE_NAME);
        LOGGER.info(SERVICE_NAME + " " + serviceName);
        final String portName = (String) getInputParameter(PORT_NAME);
        LOGGER.info(PORT_NAME + " " + portName);
        final String binding = (String) getInputParameter(BINDING);
        LOGGER.info(BINDING + " " + binding);
        final String endpointAddress = (String) getInputParameter(ENDPOINT_ADDRESS);
        LOGGER.info(ENDPOINT_ADDRESS + " " + endpointAddress);

        final QName serviceQName = new QName(serviceNS, serviceName);
        final QName portQName = new QName(serviceNS, portName);
        final Service service = Service.create(serviceQName);
        service.addPort(portQName, binding, endpointAddress);

        final Dispatch<Source> dispatch = service.createDispatch(portQName, Source.class, Service.Mode.MESSAGE);
        final Object authUserName = getInputParameter(USER_NAME);
        if (authUserName != null) {
            LOGGER.info(USER_NAME + " " + authUserName);
            dispatch.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, authUserName);
            final Object authPassword = getInputParameter(PASSWORD);
            LOGGER.info(PASSWORD + " " + authPassword);
            dispatch.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, authPassword);
        }

        final String soapAction = (String) getInputParameter(SOAP_ACTION);
        LOGGER.info(SOAP_ACTION + " " + soapAction);

        if (soapAction != null) {
            dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, true);
            dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, soapAction);
        }

        final List<List<Object>> httpHeadersList = (List<List<Object>>) getInputParameter(HTTP_HEADERS);

        if (httpHeadersList != null) {
            final Map<String, List<String>> httpHeadersMap = new HashMap<String, List<String>>();
            for (final List<Object> row : httpHeadersList) {
                final List<String> parameters = new ArrayList<String>();
                for (final Object parameter : (List<Object>) row.get(1)) {
                    parameters.add(parameter.toString());
                }
                httpHeadersMap.put((String) row.get(0), parameters);
            }
            dispatch.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, httpHeadersMap);
        }

        final String envelope = (String) getInputParameter(ENVELOPE);
        LOGGER.info(ENVELOPE + " " + envelope);
        final Source sourceResponse;
        try {
            sourceResponse = dispatch.invoke(new StreamSource(new StringReader(envelope)));
        } catch (Exception e) {
            throw new ConnectorException("Exception trying to call remote webservice", e);
        }
        Boolean buildResponseDocumentEnveloppe = (Boolean) getInputParameter(BUILD_RESPONSE_DOCUMENT_ENVELOPE);
        LOGGER.info(BUILD_RESPONSE_DOCUMENT_ENVELOPE + " " + buildResponseDocumentEnveloppe);
        Boolean buildResponseDocumentBody = (Boolean) getInputParameter(BUILD_RESPONSE_DOCUMENT_BODY);
        LOGGER.info(BUILD_RESPONSE_DOCUMENT_BODY + " " + buildResponseDocumentBody);
        if (buildResponseDocumentEnveloppe == null) {
            buildResponseDocumentEnveloppe = false;
        }
        if (buildResponseDocumentBody == null) {
            buildResponseDocumentBody = false;
        }
        Document responseDocumentEnvelope = null;

        if (buildResponseDocumentEnveloppe || buildResponseDocumentBody) {
            responseDocumentEnvelope = buildResponseDocumentEnveloppe(sourceResponse);
        }
        Document responseDocumentBody = null;
        if (buildResponseDocumentBody) {
            responseDocumentBody = buildResponseDocumentBody(responseDocumentEnvelope);
        }

        Boolean printRequestAndResponse = (Boolean) getInputParameter(PRINT_REQUEST_AND_RESPONSE);
        LOGGER.info(PRINT_REQUEST_AND_RESPONSE + " " + printRequestAndResponse);
        if (printRequestAndResponse == null) {
            printRequestAndResponse = false;
        }
        if (printRequestAndResponse) {
            printRequestAndResponse(sourceResponse, buildResponseDocumentEnveloppe, buildResponseDocumentBody, responseDocumentEnvelope, responseDocumentBody);
        }

        setOutputParameter(OUTPUT_SOURCE_RESPONSE, sourceResponse);
        setOutputParameter(OUTPUT_RESPONSE_DOCUMENT_ENVELOPE, responseDocumentEnvelope);
        setOutputParameter(OUTPUT_RESPONSE_DOCUMENT_BODY, responseDocumentBody);
    }

    private Document buildResponseDocumentEnveloppe(Source sourceResponse) throws ConnectorException {
        final DOMResult result = new DOMResult();
        Document responseDocumentEnvelope;
        try {
            getTransformer().transform(sourceResponse, result);
        } catch (final TransformerConfigurationException tce) {
            throw new ConnectorException(tce);
        } catch (final TransformerException te) {
            throw new ConnectorException(te);
        } catch (final TransformerFactoryConfigurationError tfce) {
            throw new ConnectorException(tfce);
        }
        if (result.getNode() instanceof Document) {
            responseDocumentEnvelope = (Document) result.getNode();
        } else {
            responseDocumentEnvelope = result.getNode().getOwnerDocument();
        }
        return responseDocumentEnvelope;
    }

    private Document buildResponseDocumentBody(Document responseDocumentEnvelope) throws ConnectorException {
        Document responseDocumentBody = null;
        if (responseDocumentEnvelope != null) {
            try {
                responseDocumentBody = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            } catch (final ParserConfigurationException pce) {
                throw new ConnectorException(pce);
            }
            final Node bodyContent = getEnveloppeBodyContent(responseDocumentEnvelope);
            final Node clonedBodyContent = bodyContent.cloneNode(true);
            responseDocumentBody.adoptNode(clonedBodyContent);
            responseDocumentBody.importNode(clonedBodyContent, true);
            responseDocumentBody.appendChild(clonedBodyContent);
        }
        return responseDocumentBody;
    }

    private void printRequestAndResponse(Source sourceResponse, boolean buildResponseDocumentEnveloppe, boolean buildResponseDocumentBody,
            Document responseDocumentEnvelope, Document responseDocumentBody) {
        try {
            getTransformer().transform(sourceResponse, new StreamResult(System.err));
            if (buildResponseDocumentEnveloppe) {
                getTransformer().transform(new DOMSource(responseDocumentEnvelope), new StreamResult(System.err));
            } else if (buildResponseDocumentBody) {
                getTransformer().transform(new DOMSource(responseDocumentEnvelope), new StreamResult(System.err));
                getTransformer().transform(new DOMSource(responseDocumentBody), new StreamResult(System.err));
            }
        } catch (final TransformerException e) {
            e.printStackTrace();
        }
    }

    private Node getEnveloppeBodyContent(final Document enveloppe) {
        final Node enveloppeNode = enveloppe.getFirstChild();
        final NodeList children = enveloppeNode.getChildNodes();
        Node enveloppeBody = null;
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (child instanceof Element) {
                final Element element = (Element) child;
                if ("Body".equalsIgnoreCase(element.getLocalName())) {
                    enveloppeBody = child;
                    break;
                }
            }
        }
        if (enveloppeBody == null) {
            return enveloppeNode;
        }

        return enveloppeBody.getFirstChild();
    }

    private Transformer getTransformer() throws TransformerConfigurationException, TransformerFactoryConfigurationError {
        if (transformer == null) {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        }
        return transformer;
    }
}
