package routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hl7.HL7DataFormat;
import processors.GenerateGenericExceptionNackMessageProcessor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static org.apache.camel.component.hl7.HL7.convertLFToCR;

@ApplicationScoped
public class ForwarderRoutes extends RouteBuilder {

    HL7DataFormat hl7DataFormat = new HL7DataFormat();

    @Inject
    GenerateGenericExceptionNackMessageProcessor nackMessageProcessor;

    @Override
    public void configure() {

        onException(org.apache.camel.component.mllp.MllpAcknowledgementReceiveException.class)
                .handled(true)
                .log(LoggingLevel.ERROR, "${exception}")
                .to("direct:generate-nack-message")
        ;

        onException(org.apache.camel.component.mllp.MllpApplicationErrorAcknowledgementException.class)
                .handled(true)
                .log(LoggingLevel.ERROR, "${exception}")
                .to("direct:generate-nack-message")
        ;


        onException(org.apache.camel.component.mllp.MllpSocketException.class)
                .handled(true)
                .log(LoggingLevel.ERROR, "${exception}")
                .to("direct:generate-nack-message")
        ;

        onException(org.apache.camel.InvalidPayloadException.class)
                .handled(true)
                .log(LoggingLevel.ERROR, "${exception}")
                .to("direct:generate-nack-message")
        ;

        onException(java.net.ConnectException.class)
                .handled(true)
                .log(LoggingLevel.ERROR, "${exception}")
                .to("direct:generate-nack-message")
        ;

        onException(ca.uhn.hl7v2.HL7Exception.class)
                .handled(true)
                .log(LoggingLevel.ERROR, "${exception}")
                .to("direct:generate-nack-message")
        ;


        rest("/")
                .post("/mllp/forward?receiving-system={receiving-system}&receiving-system-port={receiving-system-port}")
                    .to("direct:send-to-external-mllp-endpoint")
                .post("/http/forward?receiving-system={receiving-system}&receiving-system-port={receiving-system-port}")
                    .to("direct:send-to-external-http-endpoint")
                ;

        from("direct:send-to-external-mllp-endpoint")
                .routeId("send-to-external-mllp-endpoint")
                .to("direct:preprocess-message")
                .log("Sending message to ${header.receiving-system}:${header.receiving-system-port} using mllp")
                .toD("mllp:${header.receiving-system}:${header.receiving-system-port}")
                .setBody(header("CamelMllpAcknowledgement"))
                .to("direct:handle-response-from-external-system")
        ;

        from("direct:send-to-external-http-endpoint")
                .routeId("send-to-external-http-endpoint")
                .to("direct:preprocess-message")
                .log("Sending message to ${header.receiving-system}:${header.receiving-system-port} using http")
                .setHeader("Content-Type", constant("application/hl7-v2; charset=utf-8"))
                .toD("netty-http:${header.receiving-system}:${header.receiving-system-port}")
                .to("direct:handle-response-from-external-system")
        ;

        from("direct:preprocess-message")
                .routeId("preprocess-message")
                .unmarshal(hl7DataFormat)
                .setBody(convertLFToCR())
                .log("Received message:")
        ;

        from("direct:handle-response-from-external-system")
                .routeId("handle-response-from-external-system")
                .log(LoggingLevel.INFO, "Received response")
                .to("log:" + this.getClass() + "?level=INFO&showBody=true&showHeaders=true&multiline=true")
        ;

        from("direct:generate-nack-message")
                .routeId("generate-nack-message")
                .process(nackMessageProcessor)
                .marshal(hl7DataFormat)
        ;
    }
}
