package uk.gov.hmcts.opal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.service.messaging.RefDataServiceBusMessageConsumer;

class RefDataServiceBusSessionProcessorConfigTest {

    @Test
    void buildsSessionProcessorClientForConfiguredTopicAndSubscription() {
        ServiceBusProperties serviceBusProperties = new ServiceBusProperties();
        serviceBusProperties.setConnectionString(
            "Endpoint=sb://example.servicebus.windows.net/;"
                + "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key"
        );

        RefDataServiceBusProperties refDataServiceBusProperties = new RefDataServiceBusProperties();
        refDataServiceBusProperties.setTopicName("ref-data-topic");
        refDataServiceBusProperties.setSubscriptionName("ref-data-subscription");

        RefDataServiceBusSessionProcessorConfig config = new RefDataServiceBusSessionProcessorConfig();
        ServiceBusProcessorClient client = config.refDataServiceBusSessionProcessorClient(
            serviceBusProperties,
            refDataServiceBusProperties,
            mock(RefDataServiceBusMessageConsumer.class)
        );

        assertThat(client.getTopicName()).isEqualTo("ref-data-topic");
        assertThat(client.getSubscriptionName()).isEqualTo("ref-data-subscription");

        client.close();
    }
}
