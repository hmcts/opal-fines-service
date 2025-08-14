package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

@ActiveProfiles({"integration", "opal"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Slf4j(topic = "opal.OpalDefendantAccountsIntegrationTest")
class OpalDefendantAccountsIntegrationTest extends DefendantAccountsControllerIntegrationTest {

    @Disabled("Legacy has been implemented. But Opal still to be completed - see ticket PO-985")
    @Test
    void testGetHeaderSummary() throws Exception {
        super.getHeaderSummaryImpl(log);
    }

    @Disabled("Legacy has been implemented. But Opal still to be completed - see ticket PO-985")
    @Test
    void testGetHeaderSummary_500Error() throws Exception {
        super.getHeaderSummaryImpl(log);
    }

    @Test
    void testSearch_Opal_Happy() throws Exception {
        super.testPostDefendantAccountsSearch_Opal(log);
    }

    @Test
    void testSearch_Opal_NoResults() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_NoResults(log);
    }

    @Test void opal_search_by_name_and_bu() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_ByNameAndBU(log);
    }

    @Test void opal_postcode_ignores_spaces() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_Postcode_IgnoresSpaces(log);
    }

    @Test void opal_account_number_starts_with() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_AccountNumberStartsWith(log);
    }

    @Test void opal_pcr_exact() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_PcrExact(log);
    }

    @Test void opal_pcr_no_match() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_PcrNoMatch(log);
    }

    @Test void opal_ni_starts_with() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_NiStartsWith(log);
    }

    @Test void opal_address_starts_with() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_AddressStartsWith(log);
    }

    @Test void opal_dob_exact() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_DobExact(log);
    }

    @Test void opal_alias_flag_uses_main_name() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_AliasFlag_UsesMainName(log);
    }

    @Test
    void opal_active_accounts_only_false() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_ActiveAccountsOnlyFalse(log);
    }

}
