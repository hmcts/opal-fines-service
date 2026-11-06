package uk.gov.hmcts.opal.controllers.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.gov.hmcts.opal.controllers.TestingSupportReferenceDataApiController;
import uk.gov.hmcts.opal.controllers.advice.GlobalExceptionHandler;
import uk.gov.hmcts.opal.refdata.RefDataUpdateHandler;
import uk.gov.hmcts.opal.service.TestingSupportReferenceDataService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

@SpringBootTest(classes = TestingSupportReferenceDataIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc(addFilters = false,
    htmlUnit = @AutoConfigureMockMvc.HtmlUnit(webClient = false, webDriver = false))
class TestingSupportReferenceDataIntegrationTest {

    private static final String URL_BASE = "/testing-support/ref-data";
    private static final String VALID_MESSAGE = """
        {
          "header": {
            "message_id": "4d71a80b-538b-4f21-a64a-25fa00be66f3",
            "message_type": "ReferenceData",
            "data_product": "LJA",
            "operation": "PUBLISH",
            "source_system": "Semarchy",
            "created_date_time": "2026-01-01T00:00:00Z",
            "release_package_id": 1,
            "record_count": 1
          },
          "payload": {
            "records": [{
              "lja_code": "0810",
              "lja_name": "Test LJA",
              "lja_type": "CRWCRT",
              "start_date": "2026-01-01",
              "end_date": null,
              "publishing_status": "Active",
              "cja_code": "41",
              "addresses": [{
                "address_type": "Court Address",
                "address_line_1": "Test address",
                "postcode": "GIR 0AA"
              }],
              "contact_information": []
            }]
          }
        }
        """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RefDataUpdateHandler<?, ?> handler;

    @Autowired
    private ServiceBusSenderClient senderClient;

    @Test
    @JiraStory("PO-10705")
    @JiraEpic("PO-9031")
    void deleteReferenceData_deletesUsingMatchingHandler() throws Exception {
        mockMvc.perform(delete(URL_BASE + "/LJA/08104"))
            .andExpect(status().isNoContent());

        verify(handler).deleteEntity("08104");
    }

    @Test
    @JiraStory("PO-10705")
    @JiraEpic("PO-9031")
    void deleteReferenceData_rejectsUnsupportedReferenceDataType() throws Exception {
        mockMvc.perform(delete(URL_BASE + "/UNKNOWN/123"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @JiraStory("PO-10705")
    @JiraEpic("PO-9031")
    void deleteReferenceData_rejectsInvalidBusinessKey() throws Exception {
        doThrow(new IllegalArgumentException("Invalid business key received: not-a-short"))
            .when(handler).deleteEntity("not-a-short");

        mockMvc.perform(delete(URL_BASE + "/LJA/not-a-short"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @JiraStory("PO-10705")
    @JiraEpic("PO-9031")
    void publishReferenceData_publishesValidMessageWithSessionId() throws Exception {
        mockMvc.perform(post(URL_BASE + "/topic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_MESSAGE))
            .andExpect(status().isAccepted());

        ArgumentCaptor<ServiceBusMessage> messageCaptor = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(senderClient).sendMessage(messageCaptor.capture());
        ServiceBusMessage message = messageCaptor.getValue();
        assertThat(objectMapper.readTree(message.getBody().toString())).isEqualTo(objectMapper.readTree(VALID_MESSAGE));
        assertThat(UUID.fromString(message.getSessionId())).isNotNull();
    }

    @Test
    @JiraStory("PO-10705")
    @JiraEpic("PO-9031")
    void publishReferenceData_rejectsMessageThatDoesNotMatchSchema() throws Exception {
        mockMvc.perform(post(URL_BASE + "/topic")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());

        verify(senderClient, never()).sendMessage(any(ServiceBusMessage.class));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        FlywayAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
    })
    @Import({
        TestingSupportReferenceDataApiController.class,
        TestingSupportReferenceDataService.class,
        GlobalExceptionHandler.class,
        JsonSchemaValidationService.class
    })
    static class TestApplication {

        @Bean
        @SuppressWarnings("unchecked")
        RefDataUpdateHandler<?, ?> refDataUpdateHandler() {
            RefDataUpdateHandler<?, ?> handler = mock(RefDataUpdateHandler.class);
            when(handler.refDataType()).thenReturn("LJA");
            return handler;
        }

        @Bean
        ServiceBusSenderClient testingSupportReferenceDataSender() {
            return mock(ServiceBusSenderClient.class);
        }
    }
}
