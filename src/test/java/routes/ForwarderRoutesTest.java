package routes;

import ca.uhn.hl7v2.util.Terser;
import configuration.AppConfig;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static routes.HttpConsumerSinkTest.toTerser;

@QuarkusTest
class ForwarderRoutesTest {

    @Inject
    AppConfig appConfig;

    private static final String VALID_MESSAGE = "MSH|^~\\&|^Receiving system with facility||||20150508115035.02+0200||SIU^S12^SIU_S12|729729477590387447|P|2.5.1||||||8859/1\r" +
                                         "SCH||46374637\r" +
                                         "TQ1||||||37^Minutes|20160120210000+0100";
    private static final String NAUGHTY_MESSAGE = "I'm a bad message.";

    @Test
    void assertThatHttpForwarderAcceptsMessages() throws Exception {

        var body =
        given()
            .body(VALID_MESSAGE)
            .contentType(ContentType.TEXT)
        .when()
            .queryParams(Map.of(
                "receiving-system", appConfig.mllp().hostname(),
                "receiving-system-port", appConfig.mllp().port()))
            .post("/mllp/forward")
        .getBody()
        ;

        Terser terser = toTerser(body.asString());
        assertEquals("AA",  terser.get("MSA-1"));
    }

    @Test
    void assertThatHttpForwarderRejectsInvalidMessages() throws Exception {

        var body =
        given()
            .body(NAUGHTY_MESSAGE)
            .contentType(ContentType.TEXT)
        .when()
            .queryParams(Map.of(
                "receiving-system", appConfig.mllp().hostname(),
                "receiving-system-port", appConfig.mllp().port()))
            .post("/mllp/forward")
       .getBody()
        ;

        Terser terser = toTerser(body.asString());
        assertEquals("AR",  terser.get("MSA-1"));
        assertTrue(terser.get("ERR-3-9").contains("Message encoding is not recognized"), "Expected: 'Message encoding is not recognized', got: " + terser.get("ERR-3-9"));
    }

    @Test
    void assertThatHttpForwarderRejectsOnIncorrectReceivingSystem() throws Exception {

        var body =
        given()
            .body(VALID_MESSAGE)
            .contentType(ContentType.TEXT)
        .when()
            .queryParams(Map.of(
                "receiving-system", appConfig.mllp().hostname(),
                "receiving-system-port", 1234))
            .post("/mllp/forward")
        .getBody();

        Terser terser = toTerser(body.asString());
        assertEquals("AR",  terser.get("MSA-1"));
        assertTrue(terser.get("ERR-3-9").contains("Connection refused: no further information"), "Expected: 'Connection refused: no further information', got: " + terser.get("ERR-3-9"));
    }

    @Test
    void testMissingEndpoint() {
        given()
        .when()
            .get("/hello")
        .then()
            .statusCode(404)
        ;
    }
}