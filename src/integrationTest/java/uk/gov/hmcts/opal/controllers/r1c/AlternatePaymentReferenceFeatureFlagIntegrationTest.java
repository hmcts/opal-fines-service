package uk.gov.hmcts.opal.controllers.r1c;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.MANAGE_ALTERNATE_PAYMENT_REFERENCES;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.service.AlternatePaymentReferenceService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1c-banking-interfaces=false"
})
@DisplayName("Alternate payment references with the banking interfaces feature disabled")
class AlternatePaymentReferenceFeatureFlagIntegrationTest extends AbstractIntegrationTest {

    @MockitoSpyBean
    private AlternatePaymentReferenceService alternatePaymentReferenceService;

    @Nested
    @DisplayName("Create an alternate payment reference for a defendant account")
    class CreateAlternatePaymentReference {

        @Test
        @JiraStory("PO-10878")
        @JiraEpic("PO-7465")
        @DisplayName("Returns the feature-disabled problem without creating a reference for an authorised user")
        void returnsFeatureDisabledWithoutCallingService() throws Exception {
            userStateStub.setupWithNoPermissions();
            userStateStub.addPermissions((short) 78, MANAGE_ALTERNATE_PAYMENT_REFERENCES);

            mockMvc.perform(post("/defendant-accounts/{defendantAccountId}/alternate-payment-references", 77L)
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .header(HttpHeaders.IF_MATCH, "\"0\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content("""
                        {"apr_text": "NEW-APR-REFERENCE"}
                        """))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().doesNotExist(HttpHeaders.ETAG))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Feature Disabled"))
                .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"))
                .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/feature-disabled"))
                .andExpect(jsonPath("$.retriable").value(false));

            verifyNoInteractions(alternatePaymentReferenceService);
        }
    }

    @Nested
    @DisplayName("Update an alternate payment reference for a defendant account")
    class UpdateAlternatePaymentReference {

        @Test
        @JiraStory("PO-10879")
        @JiraEpic("PO-7465")
        @DisplayName("Returns the feature-disabled problem without updating a reference for an authorised user")
        void returnsFeatureDisabledWithoutCallingService() throws Exception {
            userStateStub.setupWithNoPermissions();
            userStateStub.addPermissions((short) 78, MANAGE_ALTERNATE_PAYMENT_REFERENCES);

            mockMvc.perform(patch("/defendant-accounts/{defendantAccountId}/alternate-payment-references"
                    + "/{alternatePaymentReferenceId}", 77L, 456L)
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .header(HttpHeaders.IF_MATCH, "\"2\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content("""
                        {"apr_text": "UPDATED-REFERENCE"}
                        """))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().doesNotExist(HttpHeaders.ETAG))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Feature Disabled"))
                .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"))
                .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/feature-disabled"))
                .andExpect(jsonPath("$.retriable").value(false));

            verifyNoInteractions(alternatePaymentReferenceService);
        }
    }

    @Nested
    @DisplayName("Get alternate payment references for a defendant account")
    class GetAlternatePaymentReferences {

        @Test
        @JiraStory("PO-10877")
        @JiraEpic("PO-7465")
        @DisplayName("Returns the feature-disabled problem without calling the service for an authorised user")
        void returnsFeatureDisabledWithoutCallingService() throws Exception {
            userStateStub.setupWithNoPermissions();
            userStateStub.addPermissions((short) 78, MANAGE_ALTERNATE_PAYMENT_REFERENCES);

            mockMvc.perform(get("/defendant-accounts/{defendantAccountId}/alternate-payment-references", 77L)
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().doesNotExist(HttpHeaders.ETAG))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Feature Disabled"))
                .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"))
                .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/feature-disabled"))
                .andExpect(jsonPath("$.retriable").value(false));

            verifyNoInteractions(alternatePaymentReferenceService);
        }
    }
}
