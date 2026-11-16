package uk.gov.hmcts.opal.controllers.r1b;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.generated.model.MinorCreditorSearchRequest;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

abstract class MinorCreditorSearchPdplTestSupport extends AbstractIntegrationTest {

    @MockitoBean
    protected LoggingService loggingService;

    @BeforeEach
    void authoriseSearch() {
        userStateStub.addPermissions((short) 10, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);
    }

    protected ResultActions search(String accountNumber) throws Exception {
        MinorCreditorSearchRequest request = MinorCreditorSearchRequest.builder()
            .businessUnitIds(List.of((short) 10)).accountNumber(accountNumber).activeAccountsOnly(false).build();

        return mockMvc.perform(post("/minor-creditor-accounts/search")
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header("Authorization", userStateStub.getBearerToken())
            .header("X-User-IP", "203.0.113.10")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));
    }

    protected List<PersonalDataProcessingLogDetails> logs(int count) {
        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService, times(count)).personalDataAccessLogAsync(captor.capture());
        return captor.getAllValues();
    }

    protected void assertLog(PersonalDataProcessingLogDetails details, String identifier,
                             PdplIdentifierType type, String... ids) {

        assertThat(details.getBusinessIdentifier()).isEqualTo(identifier);
        assertThat(details.getCategory()).isEqualTo(PersonalDataProcessingCategory.CONSULTATION);
        assertThat(details.getCreatedBy().getIdentifier()).isEqualTo("500000000");
        assertThat(details.getCreatedBy().getType()).isEqualTo(PdplIdentifierType.OPAL_USER_ID);
        assertThat(details.getRecipient()).isNull();
        assertThat(details.getIpAddress()).isEqualTo("203.0.113.10");
        assertThat(details.getCreatedAt()).isNotNull();
        assertThat(details.getIndividuals()).extracting(ParticipantIdentifier::getIdentifier)
            .containsExactlyInAnyOrder(ids);
        assertThat(details.getIndividuals())
            .allSatisfy(participant -> assertThat(participant.getType()).isEqualTo(type));
    }

    protected void rejectQueueCall(int failingCall, boolean raisesError) {
        AtomicInteger calls = new AtomicInteger();
        when(loggingService.personalDataAccessLogAsync(any())).thenAnswer(invocation -> {
            if (calls.incrementAndGet() == failingCall) {
                if (raisesError) {
                    throw new IllegalStateException("Queue unavailable");
                }
                return false;
            }
            return true;
        });
    }

    protected void assertQueueFailure(ResultActions result, int expectedCalls) throws Exception {
        result.andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/pdpl-logging-unavailable"))
            .andExpect(jsonPath("$.retriable").value(true))
            .andExpect(jsonPath("$.creditor_accounts").doesNotExist())
            .andExpect(content().string(not(containsString("SearchCreditor"))))
            .andExpect(content().string(not(containsString("SearchDefendant"))));
        verify(loggingService, times(expectedCalls)).personalDataAccessLogAsync(any());
    }
}
