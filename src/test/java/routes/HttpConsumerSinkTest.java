package routes;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;
import configuration.AppConfig;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;

@QuarkusTest
class HttpConsumerSinkTest {

    @Inject
    AppConfig appConfig;

    String endpointUri;

    @BeforeEach
    void setup() {
        endpointUri = "http://" + appConfig.http().hostname() + ":" + appConfig.http().port();
    }

    @Test
    void assertThatHttpSinkAcceptsInvalidMessages() throws Exception {
        // Given
        String message = "This isn't were parked my car.";
        // When
        var body =
            given()
                .body(message)
                .contentType(ContentType.TEXT)
            .when()
                .post(endpointUri)
            .getBody();
        Terser hl7MessageTerser = toTerser(body.asString());
        // Then
        Assertions.assertEquals("AR",  hl7MessageTerser.get("MSA-1"));
    }

    @Test
    void assertThatHttpSinkAcceptsMessages() throws Exception {
        // Given
        String message = "MSH|^~\\&|^Receiving system with facility||||20150508115035.02+0200||SIU^S12^SIU_S12|729729477590387447|P|2.5.1||||||8859/1\r" +
                "SCH||46374637\r" +
                "TQ1||||||37^Minutes|20160120210000+0100";
        // When
        var body =
            given()
                .body(message)
                .contentType(ContentType.TEXT)
            .when()
                .post(endpointUri)
            .getBody();
        Terser hl7MessageTerser = toTerser(body.asString());
        // Then
        Assertions.assertEquals("AA",  hl7MessageTerser.get("MSA-1"));

    }

    public static Terser toTerser(String hl7message) throws HL7Exception
    {
        PipeParser pipeParser = new PipeParser();
        Message message = pipeParser.parse(hl7message);
        return new Terser(message);
    }
}