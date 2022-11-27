package processors;

import ca.uhn.hl7v2.*;
import ca.uhn.hl7v2.model.Message;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GenerateGenericExceptionNackMessageProcessor implements Processor {

    Logger log = Logger.getLogger(GenerateGenericExceptionNackMessageProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        Exception e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        log.error("An exception occurred: " + e);

        HL7Exception hl7Exception =  new HL7Exception(e.getMessage(), ErrorCode.APPLICATION_INTERNAL_ERROR);
        Message tempMessage = new DefaultHapiContext().newMessage("ACK", "U", Version.V251);

        exchange.getIn().setBody(tempMessage.generateACK(AcknowledgmentCode.AR, hl7Exception));

    }
}