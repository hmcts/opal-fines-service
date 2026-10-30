package uk.gov.hmcts.opal.controllers.r1c;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.shared.util.DefendantSearchRequestUtil.searchRequest;
import static uk.gov.hmcts.opal.dto.PdplIdentifierType.DEFENDANT;
import static uk.gov.hmcts.opal.dto.PdplIdentifierType.OPAL_USER_ID;
import static uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory.CONSULTATION;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_WRITE_OFF;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_WRITE_OFF_ENABLED_PROPERTY;

import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchRequestDefendantAccount;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountServiceProxy;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1b=true",
    "launchdarkly.default-flag-values.release-1c-write-off=true",
    "launchdarkly.default-flag-values.release-1c-payment=false"
})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Tag("ExtendedTest")
class DefendantSearchPdplTest extends AbstractIntegrationTest {

    private static final String BUSINESS_IDENTIFIER = "Search Account Results - Consolidation - Individual Defendants";

    @MockitoBean
    private LoggingService loggingService;

    @MockitoSpyBean
    private FeatureToggleApi featureToggleApi;

    @MockitoSpyBean
    private DefendantAccountServiceProxy defendantAccountServiceProxy;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Test
    @JiraStory("PO-10775")
    @JiraEpic("PO-10729")
    void consolidationLogsAllReturnedIndividualsAfterFlagResolution() throws Exception {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        String body = search(searchRequest("17", false, true))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.defendant_accounts[0].collection_order").doesNotExist())
            .andReturn().getResponse().getContentAsString();
        jsonSchemaValidationService.validateOrError(body, SchemaPaths.POST_DEFENDANT_ACCOUNT_SEARCH_RESPONSE);
        List<String> returnedIds = objectMapper.readTree(body).path("defendant_accounts").valueStream()
            .map(account -> account.path("defendant_account_id").asString()).toList();
        assertThat(returnedIds).containsExactlyInAnyOrder("77", "78");

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        InOrder order = inOrder(featureToggleApi, defendantAccountServiceProxy, loggingService);
        order.verify(featureToggleApi).isFeatureEnabledWithPropertyValueDefault(
            RELEASE_1C_WRITE_OFF, RELEASE_1C_WRITE_OFF_ENABLED_PROPERTY, false);
        order.verify(defendantAccountServiceProxy).searchDefendantAccounts(any());
        order.verify(loggingService).personalDataAccessLogAsync(captor.capture());
        PersonalDataProcessingLogDetails log = captor.getValue();
        assertThat(log.getBusinessIdentifier()).isEqualTo(BUSINESS_IDENTIFIER);
        assertThat(log.getCategory()).isEqualTo(CONSULTATION);
        assertThat(log.getCreatedBy().getIdentifier()).isEqualTo(userStateStub.getUserState().getUserId().toString());
        assertThat(log.getCreatedBy().getType()).isEqualTo(OPAL_USER_ID);
        assertThat(log.getIndividuals()).extracting(ParticipantIdentifier::getIdentifier)
            .containsExactlyInAnyOrderElementsOf(returnedIds).doesNotHaveDuplicates();
        assertThat(log.getIndividuals()).extracting(ParticipantIdentifier::getType).containsOnly(DEFENDANT);
        verify(loggingService).personalDataAccessLogAsync(any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @JiraStory("PO-10775")
    @JiraEpic("PO-10729")
    void queueFailureDoesNotReturnPersonalData(boolean throwsException) throws Exception {
        if (throwsException) {
            when(loggingService.personalDataAccessLogAsync(any())).thenThrow(new RuntimeException("Queue unavailable"));
        } else {
            when(loggingService.personalDataAccessLogAsync(any())).thenReturn(false);
        }

        String body = search(searchRequest("177A", false, true))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/pdpl-logging-unavailable"))
            .andExpect(jsonPath("$.retriable").value(true))
            .andExpect(jsonPath("$.defendant_accounts").doesNotExist())
            .andExpect(jsonPath("$.count").doesNotExist())
            .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain("Anna", "Graham", "Lumber", "A11111A");
        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService).personalDataAccessLogAsync(captor.capture());
        assertThat(captor.getValue().getBusinessIdentifier()).isEqualTo(BUSINESS_IDENTIFIER);
    }

    @Test
    @JiraStory("PO-10775")
    @JiraEpic("PO-10729")
    void companyResultsDoNotRequireQueueing() throws Exception {
        String body = search(searchRequest("333A", true, true))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation").value(true))
            .andReturn().getResponse().getContentAsString();

        jsonSchemaValidationService.validateOrError(body, SchemaPaths.POST_DEFENDANT_ACCOUNT_SEARCH_RESPONSE);
        verifyNoInteractions(loggingService);
    }

    @Test
    @JiraStory("PO-10775")
    @JiraEpic("PO-10729")
    void emptyResultsDoNotRequireQueueing() throws Exception {
        String body = search(searchRequest("PDPO-NO-MATCH", false, true))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0))
            .andExpect(jsonPath("$.defendant_accounts").isEmpty())
            .andReturn().getResponse().getContentAsString();

        jsonSchemaValidationService.validateOrError(body, SchemaPaths.POST_DEFENDANT_ACCOUNT_SEARCH_RESPONSE);
        verifyNoInteractions(loggingService);
    }

    private ResultActions search(PostDefendantAccountSearchRequestDefendantAccount request) throws Exception {
        return mockMvc.perform(post("/defendant-accounts/search")
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header("authorization", userStateStub.getBearerToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));
    }
}
