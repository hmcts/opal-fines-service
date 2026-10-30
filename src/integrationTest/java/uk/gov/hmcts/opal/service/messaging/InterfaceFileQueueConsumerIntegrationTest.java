package uk.gov.hmcts.opal.service.messaging;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import jakarta.jms.TextMessage;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.github.tomakehurst.wiremock.client.WireMock;
import uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileObject;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileTypeEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.StatusEnum;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = {
    "opal.interface-file.service-bus.consumer-enabled=true",
    "opal.report.service-bus.consumer-enabled=true",
    "opal.automated-task=",
    "opal.interface-file.service-bus.queue-name=test-queue",
    "opal.common.system-users.token-url=http://localhost:4553",
    "file-handler.service.url=http://localhost:4553"
})
class InterfaceFileQueueConsumerIntegrationTest extends AbstractIntegrationWithSecurityTest {

    private static final String INTERFACE_FILE_ID_1 = """
        {
          "interfaceFileId": 1
        }
         """;

    @Autowired
    private InterfaceFileQueueListener listener;

    @Test
    @DisplayName("AC1 - Valid message is processed successfully")
    @JiraStory("PO-6459")
    @JiraEpic("PO-6390")
    void ac1ValidMessageIsProcessedSuccessfully() throws Exception {

        WireMock.configureFor("localhost", 4553);

        stubFor(
            post(urlEqualTo("/"))
                .willReturn(
                    okJson("""
                    {
                      "token_type":"Bearer",
                      "access_token":"token-value"
                    }
                    """)
                )
        );

        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(INTERFACE_FILE_ID_1);

        InterfaceFileObject interfaceFileObject = sourceJsonInterfaceFile();
        InterfaceFileObject transformedFile = transformedJsonInterfaceFile();

        stubFor(
            get(urlEqualTo("/interface-files/1"))
                .willReturn(
                    okJson(
                        objectMapper.writeValueAsString(interfaceFileObject)
                    )
                )
        );

        stubFor(
            get(urlEqualTo("/interface-files/1/content"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody(sourceJson().getByteArray())
                )
        );

        stubFor(
            post(urlEqualTo("/interface-files"))
                .willReturn(
                    okJson(
                        objectMapper.writeValueAsString(transformedFile)
                    )
                )
        );

        assertThatCode(() -> listener.onMessage(textMessage))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Invalid payload throws exception")
    @JiraStory("PO-6459")
    @JiraEpic("PO-6390")
    void invalidPayloadThrowsException() throws Exception {
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn("{invalid");

        assertThatThrownBy(() -> listener.onMessage(textMessage))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unable to parse");
    }

    @Test
    @DisplayName("Blank payload throws exception")
    @JiraStory("PO-6459")
    @JiraEpic("PO-6390")
    void blankPayloadThrowsException() throws Exception {
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(" ");

        assertThatThrownBy(() -> listener.onMessage(textMessage))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("payload is blank");
    }

    private static @NonNull ByteArrayResource sourceJson() {
        ByteArrayResource sourceJson = new ByteArrayResource("""
{
  "payment_type": "CASH",
  "destination_details": {
    "bank_details": {
      "sort_code": "123456",
      "account_number": "01234567"
    }
  },
  "transactions": [
    {
      "transaction_code": "00",
      "amount": 100,
      "date_entry_applied": "2026-10-28",
      "originator_details": {
        "account_reference": "99000001A"
      }
    }
  ]
}
            """.getBytes());
        return sourceJson;
    }

    private InterfaceFileObject sourceJsonInterfaceFile() {
        return new InterfaceFileObject()
            .interfaceFileId(1L)
            .type(InterfaceFileTypeEnum.SOURCE_JSON)
            .domain(InterfaceFileObject.DomainEnum.FINES)
            .target(InterfaceFileEnum.OPAL)
            .source(InterfaceFileEnum.NATWEST)
            .status(StatusEnum.SUCCESS)
            .fileName("test.json");
    }

    private InterfaceFileObject transformedJsonInterfaceFile() {
        return new InterfaceFileObject()
            .interfaceFileId(999L)
            .type(InterfaceFileTypeEnum.TRANSFORMED_JSON)
            .domain(InterfaceFileObject.DomainEnum.FINES)
            .target(InterfaceFileEnum.OPAL)
            .source(InterfaceFileEnum.NATWEST)
            .status(StatusEnum.SUCCESS)
            .fileName("transformed.json");
    }
}