package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.jms.TextMessage;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.opal.service.filehandler.FileHandlerAPIService;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileObject;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileTypeEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.StatusEnum;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserEnum;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = {
    "opal.interface-file.service-bus.consumer-enabled=true",
    "opal.report.service-bus.consumer-enabled=true",
    "opal.automated-task=",
    "opal.interface-file.service-bus.queue-name=test-queue"
})
class InterfaceFileQueueConsumerIntegrationTest extends AbstractIntegrationTest {

    public static final String INTERFACE_FILE_ID_1 = """
        {
          "interfaceFileId": 1
        }
        """;
    @Autowired
    private InterfaceFileQueueListener listener;

    @MockitoBean
    private FileHandlerAPIService fileHandlerAPIService;

    @Test
    @DisplayName("AC1 - Valid message is processed successfully")
    @JiraStory("PO-6459")
    @JiraEpic("PO-6390")
    void ac1ValidMessageIsProcessedSuccessfully() throws Exception {

        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(INTERFACE_FILE_ID_1);

        InterfaceFileObject interfaceFileObject = sourceJsonInterfaceFile();

        when(fileHandlerAPIService.getInterfaceFile(SystemUserEnum.OPAL_SYSTEM_USER,1L))
            .thenReturn(interfaceFileObject);
        ByteArrayResource sourceJson = sourceHson();
        when(fileHandlerAPIService.getInterfaceFileContent(SystemUserEnum.OPAL_SYSTEM_USER,1L))
            .thenReturn(sourceJson);
        InterfaceFileObject transformedFile = transformedJsonInterfaceFile();
        when(fileHandlerAPIService.addInterfaceFile(any(), any(), any())).thenReturn(transformedFile);
        assertThatCode(() -> listener.onMessage(textMessage)).doesNotThrowAnyException();

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

    private static @NonNull ByteArrayResource sourceHson() {
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