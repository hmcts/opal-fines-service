package uk.gov.hmcts.opal.controllers.r1c;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = "launchdarkly.default-flag-values.release-1c-payment=false")
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Tag("ExtendedTest")
class DefendantSearchPaymentFeatureDisabledIntegrationTest extends AbstractIntegrationTest {

    private static final String DEFENDANTS_SEARCH_URL = "/defendant-accounts/search";

    @MockitoBean
    private AccessTokenService accessTokenService;

    @Test
    @DisplayName("PO-3631: collection_order is omitted when release-1c-payment is disabled")
    @JiraStory("PO-3631")
    void postDefendantAccountSearch_omitsCollectionOrderWhenPaymentFeatureIsDisabled() throws Exception {
        mockMvc.perform(post(DEFENDANTS_SEARCH_URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [78],
                      "reference_number": {
                        "account_number": "177A",
                        "prosecutor_case_reference": null,
                        "organisation": false
                      },
                      "defendant": null,
                      "consolidation_search": false
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].collection_order").doesNotExist());
    }
}
