package uk.gov.hmcts.opal.service.messaging;

import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Component
@Slf4j(topic = "opal.AllocateTillsQueueListener")
@RequiredArgsConstructor
@ConditionalOnExpression("${opal.tills.service-bus.consumer-enabled} == true "
    + "and '${opal.automated-task:}' == ''")
public class AllocateTillsQueueListener implements ServiceBusRecordMessageListener {

    private final AllocateTillsQueueConsumerService consumer;

    @Override
    public void onMessage(ServiceBusReceivedMessageContext context) {
        try {
            consumer.consume(context.getMessage().getBody().toString());
        } catch (TillNotProcessingException exception) {
            context.abandon();
            return;
        } catch (RuntimeException exception) {
            log.error("Allocate Tills consumption failed; abandoning message. Failure type: {}",
                exception.getClass().getSimpleName());
            context.abandon();
            return;
        }
        context.complete();
    }
}
