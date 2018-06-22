package org.bonitasoft.connectors.ws;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
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

    private Document buildXml(String xml) throws Exception {
        final DOMResult result = new DOMResult();
        webservice.getTransformer().transform(new StreamSource(new StringReader(xml)), result);
        return (Document) result.getNode();
    }
}
