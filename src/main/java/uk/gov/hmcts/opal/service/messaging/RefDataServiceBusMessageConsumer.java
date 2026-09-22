package uk.gov.hmcts.opal.service.messaging;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.service.refdata.framework.RefDataMessageProcessor;

@Slf4j(topic = "opal.RefDataServiceBusMessageHandler")
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "opal.ref-data.service-bus", name = "consumer-enabled", havingValue = "true")
public class RefDataServiceBusMessageConsumer {

    private final RefDataMessageProcessor processor;

    public void consumeMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();
        try {
            processor.processMessage(message.getBody().toString());
            context.complete();
        } catch (RuntimeException ex) {
            log.warn(
                "Unable to process ref-data Service Bus message. messageId={}, sessionId={}",
                message.getMessageId(),
                message.getSessionId(),
                ex
            );
            abandonMessage(context, ex);
            throw ex;
        }
    }

    public void processError(ServiceBusErrorContext context) {
        log.error(
            "Error receiving ref-data Service Bus message. namespace={}, entityPath={}, errorSource={}",
            context.getFullyQualifiedNamespace(),
            context.getEntityPath(),
            context.getErrorSource(),
            context.getException()
        );
    }

    private void abandonMessage(ServiceBusReceivedMessageContext context, RuntimeException processingException) {
        try {
            context.abandon();
        } catch (RuntimeException abandonException) {
            processingException.addSuppressed(abandonException);
        }
    }
}
