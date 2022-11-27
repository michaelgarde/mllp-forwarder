package routes;

import configuration.AppConfig;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import processors.GenerateGenericExceptionNackMessageProcessor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MllpConsumerSink extends RouteBuilder {

    @Inject
    AppConfig appConfig;

    @Inject
    GenerateGenericExceptionNackMessageProcessor generateGenericExceptionNackMessage;

    @Override
    public void configure() {

        String mllpEndpoint = appConfig.mllp().hostname() + ":" + appConfig.mllp().port();

        onException(Exception.class)
                .routeId(this.getClass().getSimpleName() + "-MllpAcknowledgementGenerationException")
                .continued(true)
                .log(LoggingLevel.INFO, "Failed to create a hl7 ack/nack")
                .process(generateGenericExceptionNackMessage)
                .to("log:" + this.getClass() + "OnException?level=INFO&showBody=true&showHeaders=true&multiline=true")
                ;

        from("mllp:" + mllpEndpoint + "?autoAck=true&validatePayload=true")
                .routeId("mllp-consumer-sink")
                .to("direct:log.received.message")
        ;

        from("direct:log.received.message")
                .routeId("log-received-message")
                .log(LoggingLevel.INFO, "MLLP Consumer Sink received message:")
                .to("log:" + this.getClass() + "?level=INFO&showBody=true&showHeaders=true&multiline=true")
        ;
    }
}
