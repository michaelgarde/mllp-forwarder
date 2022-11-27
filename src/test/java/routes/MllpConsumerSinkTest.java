package routes;

import configuration.AppConfig;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.component.mllp.MllpAcknowledgementTimeoutException;
import org.apache.camel.component.mllp.MllpConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.nio.charset.Charset;

@QuarkusTest
class MllpConsumerSinkTest {

    @Inject
    AppConfig appConfig;

    @Inject
    CamelContext camelContext;

    String endpointUri;

    @BeforeEach
    void setup() {
        endpointUri = "mllp:" + appConfig.mllp().hostname() + ":" + appConfig.mllp().port();
    }

    @Test
    void assertThatMllpConsumerSinkReceivesMessage() {
        // Given
        String message = "MSH|^~\\&|^Receiving system with facility||||20150508115035.02+0200||SIU^S12^SIU_S12|729729477590387447|P|2.5.1||||||8859/1\r" +
                "SCH||46374637\r" +
                "TQ1||||||37^Minutes|20160120210000+0100";
        // When
        Exchange result = camelContext.createFluentProducerTemplate()
                .withBody(message.getBytes(Charset.defaultCharset()))
                .to(endpointUri)
                .send();
        //Then
        Assertions.assertEquals("AA", result.getIn().getHeader(MllpConstants.MLLP_ACKNOWLEDGEMENT_TYPE));
        Assertions.assertNull(result.getException());
    }

    @Test
    void assertThatMllpConsumerSinkAcceptsInvalidMessage() {
        // Given
        String badMessage = "This isn't were parked my car.";
        // When
        Exchange result = camelContext.createFluentProducerTemplate()
                .withBody(badMessage.getBytes(Charset.defaultCharset()))
                .to(endpointUri)
                .send();
        // Then
        Assertions.assertEquals(MllpAcknowledgementTimeoutException.class, result.getException().getClass());
    }
}