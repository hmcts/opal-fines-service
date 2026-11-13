package uk.gov.hmcts.opal.controllers.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.config.TestingSupportReferenceDataPublisherConfig.REF_DATA_SESSION_ID;
import static uk.gov.hmcts.opal.testutil.JsonErrorAssertions.expectBadRequestWithoutStatus;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaType;
import uk.gov.hmcts.opal.refdata.RefDataHandlerRegistry;
import uk.gov.hmcts.opal.refdata.RefDataMessageDecoder;
import uk.gov.hmcts.opal.refdata.RefDataMessageProcessor;
import uk.gov.hmcts.opal.refdata.RefDataRefDataMessageProcessorImpl;
import uk.gov.hmcts.opal.refdata.schemavalidation.SchemaValidationService;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;

@Import(TestingSupportReferenceDataIntegrationTest.RefDataProcessingTestConfiguration.class)
@ActiveProfiles({"integration", "testing-support-reference-data"})
class TestingSupportReferenceDataIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/testing-support/ref-data";
    private static final Short DELETED_LJA_ID = (short) 9980;
    private static final Short PUBLISHED_LJA_ID = (short) 9981;
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
              "lja_code": "9981",
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

    @MockitoBean
    private ServiceBusSenderClient senderClient;

    @Autowired
    private RefDataMessageProcessor refDataMessageProcessor;

    @Autowired
    private LocalJusticeAreaRepository localJusticeAreaRepository;

    @BeforeEach
    @AfterEach
    void cleanUpReferenceData() {
        localJusticeAreaRepository.deleteAllById(List.of(DELETED_LJA_ID, PUBLISHED_LJA_ID));
        localJusticeAreaRepository.flush();
    }

    @Test
    @JiraStory("PO-10705")
    @JiraEpic("PO-9031")
    void deleteReferenceData_deletesFromDatabaseUsingMatchingHandler() throws Exception {
        localJusticeAreaRepository.saveAndFlush(LocalJusticeAreaEntity.builder()
            .localJusticeAreaId(DELETED_LJA_ID)
            .name("LJA to delete")
            .addressLine1("Test address")
            .ljaType(LocalJusticeAreaType.CRWCRT)
            .build());

        mockMvc.perform(delete(URL_BASE + "/LJA/" + DELETED_LJA_ID))
            .andExpect(status().isNoContent());

        assertThat(localJusticeAreaRepository.findById(DELETED_LJA_ID)).isEmpty();
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
        mockMvc.perform(delete(URL_BASE + "/LJA/not-a-short"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(expectBadRequestWithoutStatus(
                "Invalid business key received: not-a-short",
                "https://hmcts.gov.uk/problems/invalid-reference-validation"
            ));
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
        assertThat(message.getSessionId()).isEqualTo(REF_DATA_SESSION_ID);
    }

    @Test
    @JiraStory("PO-10705")
    @JiraEpic("PO-9031")
    void publishReferenceData_messageCanBeProcessedByBackend() throws Exception {
        mockMvc.perform(post(URL_BASE + "/topic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_MESSAGE))
            .andExpect(status().isAccepted());

        ArgumentCaptor<ServiceBusMessage> messageCaptor = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(senderClient).sendMessage(messageCaptor.capture());

        refDataMessageProcessor.processMessage(messageCaptor.getValue().getBody().toString());

        LocalJusticeAreaEntity localJusticeArea = localJusticeAreaRepository.findById(PUBLISHED_LJA_ID).orElseThrow();
        assertThat(localJusticeArea.getName()).isEqualTo("Test LJA");
        assertThat(localJusticeArea.getAddressLine1()).isEqualTo("Test address");
        assertThat(localJusticeArea.getPostcode()).isEqualTo("GIR 0AA");
    }

    @Test
    @JiraStory("PO-10705")
    @JiraEpic("PO-9031")
    void publishReferenceData_rejectsMessageThatDoesNotMatchSchema() throws Exception {
        mockMvc.perform(post(URL_BASE + "/topic")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(expectBadRequestWithoutStatus(
                "The request does not conform to the required JSON schema",
                "https://hmcts.gov.uk/problems/json-schema-validation"
            ));

        verify(senderClient, never()).sendMessage(any(ServiceBusMessage.class));
    }

    @TestConfiguration
    @Profile("testing-support-reference-data")
    static class RefDataProcessingTestConfiguration {

        @Bean
        SchemaValidationService refDataSchemaValidationService() {
            return new SchemaValidationService();
        }

        @Bean
        RefDataMessageDecoder refDataMessageDecoder(
            ObjectMapper objectMapper,
            SchemaValidationService schemaValidationService,
            RefDataHandlerRegistry handlerRegistry) {

            return new RefDataMessageDecoder(objectMapper, schemaValidationService, handlerRegistry);
        }

        @Bean
        RefDataMessageProcessor refDataMessageProcessor(
            CacheManager cacheManager,
            RefDataMessageDecoder decoder,
            RefDataHandlerRegistry handlerRegistry) {

            return new RefDataRefDataMessageProcessorImpl(cacheManager, decoder, handlerRegistry);
        }
    }
}
