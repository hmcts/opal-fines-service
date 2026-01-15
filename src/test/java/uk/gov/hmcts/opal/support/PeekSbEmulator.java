package uk.gov.hmcts.opal.support;

import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;

public class PeekSbEmulator {

    public static void main(String[] args) {
        String cs =
            "Endpoint=sb://localhost;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=local;UseDevelopmentEmulator=true;";
        String queue = args.length > 0 ? args[0] : "logging-pdpl";

        try (ServiceBusReceiverClient receiver =
            new ServiceBusClientBuilder()
                .connectionString(cs)
                .receiver()
                .queueName(queue)
                .buildClient()) {

            receiver.peekMessages(10).forEach(m -> {
                System.out.println("MessageId: " + m.getMessageId());
                System.out.println("Props:    " + m.getApplicationProperties());

                AmqpAnnotatedMessage amqp = m.getRawAmqpMessage();
                switch (amqp.getBody().getBodyType()) {
                    case VALUE:
                        System.out.println("Body(VALUE): " + amqp.getBody().getValue());
                        break;
                    case DATA:
                        System.out.println("Body(DATA bytes): " + amqp.getBody().getData());
                        break;
                    case SEQUENCE:
                        System.out.println("Body(SEQUENCE): " + amqp.getBody().getSequence());
                        break;
                    default:
                        System.out.println("Body(unknown): " + amqp.getBody());
                }

                System.out.println("------===== Message =====------");
            });
        }
    }
}
