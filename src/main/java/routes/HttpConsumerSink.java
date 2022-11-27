package routes;

import ca.uhn.hl7v2.model.Message;
import configuration.AppConfig;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hl7.HL7DataFormat;
import processors.GenerateGenericExceptionNackMessageProcessor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class HttpConsumerSink extends RouteBuilder {

    HL7DataFormat hl7DataFormat = new HL7DataFormat();

    @Inject
    AppConfig appConfig;

    @Inject
    GenerateGenericExceptionNackMessageProcessor generateGenericExceptionNackMessage;

    @Override
    public void configure() {

        String httpEndpoint = appConfig.http().hostname() + ":" + appConfig.http().port();

        onException(Exception.class)
                .routeId(this.getClass().getSimpleName() + "-MllpAcknowledgementGenerationException")
                .handled(true)
                .log(LoggingLevel.INFO, "Failed to create a hl7 ack/nack")
                .process(generateGenericExceptionNackMessage)
                .to("log:" + this.getClass() + "OnException?level=INFO&showBody=true&showHeaders=true&multiline=true")
        ;

        from("netty-http:http://" + httpEndpoint)
                .routeId("http-receiver-dummy")
                .log(LoggingLevel.INFO, "HTTP Consumer Dummy received message:")
                .unmarshal(hl7DataFormat)
                .to("log:" + this.getClass() + "?level=INFO&showBody=true&showHeaders=true&multiline=true")
                .to("direct:generate-response")
        ;

        from("direct:generate-response")
                .process(exchange -> {
                    Message hl7Input = exchange.getIn().getBody(Message.class);
                    exchange.getIn().setBody(hl7Input.generateACK());
                })
        ;

    }
}
