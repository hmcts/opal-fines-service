package uk.gov.hmcts.opal.service.messaging;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Component
@Slf4j(topic = "opal.AllocateTillsQueueErrorHandler")
@ConditionalOnExpression("${opal.tills.service-bus.consumer-enabled:false} == true "
    + "and '${opal.automated-task:}' == ''")
public class AllocateTillsQueueErrorHandler implements ServiceBusErrorHandler {

    @Override
    public void accept(ServiceBusErrorContext context) {
        log.error("Error receiving Allocate Tills messages from {}. Error source: {}",
            context.getEntityPath(), context.getErrorSource(), context.getException());
    }
}
