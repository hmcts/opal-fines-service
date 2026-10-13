package uk.gov.hmcts.opal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.opal.config.RefDataServiceBusSessionReceiverConfig.MISSING_FULLY_QUALIFIED_NAMESPACE_MESSAGE;

import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import org.junit.jupiter.api.Test;

class RefDataServiceBusSessionReceiverConfigTest {

    @Test
    void buildsSessionReceiverClientUsingConnectionStringWhenConfigured() {
        RefDataServiceBusProperties refDataServiceBusProperties = new RefDataServiceBusProperties();
        refDataServiceBusProperties.setConnectionString(
            "Endpoint=sb://example.servicebus.windows.net/;"
                + "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key"
        );
        refDataServiceBusProperties.setTopicName("ref-data-topic");
        refDataServiceBusProperties.setSubscriptionName("ref-data-subscription");

        RefDataServiceBusSessionReceiverConfig config = new RefDataServiceBusSessionReceiverConfig();
        ServiceBusSessionReceiverClient client = config.refDataServiceBusSessionReceiverClient(
            refDataServiceBusProperties
        );

        assertThat(client).isNotNull();

        client.close();
    }

    @Test
    void buildsSessionReceiverClientUsingDefaultAzureCredentialWhenConnectionStringIsBlank() {
        RefDataServiceBusProperties refDataServiceBusProperties = new RefDataServiceBusProperties();
        refDataServiceBusProperties.setFullyQualifiedNamespace("example.servicebus.windows.net");
        refDataServiceBusProperties.setTopicName("ref-data-topic");
        refDataServiceBusProperties.setSubscriptionName("ref-data-subscription");

        RefDataServiceBusSessionReceiverConfig config = new RefDataServiceBusSessionReceiverConfig();
        ServiceBusSessionReceiverClient client = config.refDataServiceBusSessionReceiverClient(
            refDataServiceBusProperties
        );

        assertThat(client).isNotNull();

        client.close();
    }

    @Test
    void throwsExceptionWhenConnectionStringAndFullyQualifiedNamespaceAreBlank() {
        RefDataServiceBusProperties refDataServiceBusProperties = new RefDataServiceBusProperties();
        refDataServiceBusProperties.setTopicName("ref-data-topic");
        refDataServiceBusProperties.setSubscriptionName("ref-data-subscription");

        RefDataServiceBusSessionReceiverConfig config = new RefDataServiceBusSessionReceiverConfig();

        assertThatThrownBy(() -> config.refDataServiceBusSessionReceiverClient(refDataServiceBusProperties))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage(MISSING_FULLY_QUALIFIED_NAMESPACE_MESSAGE);
    }
}
