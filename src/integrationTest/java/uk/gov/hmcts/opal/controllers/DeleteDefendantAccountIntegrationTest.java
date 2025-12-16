package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import uk.gov.hmcts.opal.AbstractIntegrationTest;

/**
 * Integration tests for DELETE /defendant-accounts/{id}
 * Test-support only endpoint.
 * Intentionally destructive.
 */

@ActiveProfiles({"integration", "opal"})
@Sql(
    scripts = "classpath:db/insertData/insert_into_defendants_for_deletion_test.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql",
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class DeleteDefendantAccountIntegrationTest extends AbstractIntegrationTest {

    @DisplayName("OPAL: DELETE Defendant Account – Happy Path (204 No Content)")
    @Test
    void deleteDefendantAccount_success_returns204() throws Exception {

        // defendant_account_id seeded by insert_into_defendants_for_deletion_test.sql
        long defendantAccountId = 1001L;

        mockMvc.perform(
                delete("/defendant-accounts/{defendantAccountId}", defendantAccountId)
                    .header("Authorization", "Bearer some_value")
            )
            .andExpect(status().isNoContent());
    }
}
