package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

@ActiveProfiles({"integration", "opal"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/insertData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.OpalDefendantAccountsIntegrationTest")
class OpalDefendantAccountsIntegrationTest extends DefendantAccountsControllerIntegrationTest {

    @Test
    void testGetHeaderSummary() throws Exception {
        super.getHeaderSummaryImpl(log);
    }

    @Test
    void testGetHeaderSummary_500Error() throws Exception {
        super.getHeaderSummaryImpl_500Error(log);
    }

    @Test
    void testGetHeaderSummary_NotFound() throws Exception {
        super.getHeaderSummary_NotFound(log);
    }

    @Test
    void testGetHeaderSummary_Forbidden() throws Exception {
        super.getHeaderSummary_Forbidden(log);
    }

    @Test
    void testSearch_Opal_Happy() throws Exception {
        super.testPostDefendantAccountsSearch_Opal(log);
    }

    @Test
    void testSearch_Opal_NoResults() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_NoResults(log);
    }

    @Test
    void opal_search_by_name_and_bu() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_ByNameAndBU(log);
    }

    @Test
    void opal_postcode_ignores_spaces() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_Postcode_IgnoresSpaces(log);
    }

    @Test
    void opal_account_number_starts_with() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_AccountNumberStartsWith(log);
    }

    @Test
    void opal_pcr_exact() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_PcrExact(log);
    }

    @Test
    void opal_pcr_no_match() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_PcrNoMatch(log);
    }

    @Test
    void opal_ni_starts_with() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_NiStartsWith(log);
    }

    @Test
    void opal_address_starts_with() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_AddressStartsWith(log);
    }

    @Test
    void opal_dob_exact() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_DobExact(log);
    }

    @Test
    void opal_alias_flag_uses_main_name() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_AliasFlag_UsesMainName(log);
    }

    @Test
    void opal_active_accounts_only_false() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_ActiveAccountsOnlyFalse(log);
    }

    @Test
    void opal_accountNumber_with_check_letter() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_AccountNumber_WithCheckLetter(log);
    }

    @Test
    void opal_accountNumber_with_check_letter_and_space() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_NoDefendantObject_StillResolvesParty(log);
    }

    @Test
    void opal_without_business_unit_filter() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_WithoutBusinessUnitFilter(log);
    }

    @Test
    void opal_personal_party_with_full_details() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_AnnaGraham_FullDetails(log);
    }

    @Test
    void opal_organisation_with_no_personal_names() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_OrganisationWithNoPersonalNames(log);
    }

    @Test
    void opal_alias_fallback_to_main_name() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_AliasFallbackToMainName(log);
    }

    @Test
    void opal_optional_fields_present_and_missing() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_OptionalFieldsPresentAndMissing(log);
    }

    @Test
    void get_header_summary_throws_not_found() throws Exception {
        super.testGetHeaderSummary_ThrowsNotFound(log);
    }

    @Test
    void opal_defendant_with_null_party_fields_uses_aliases() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_AliasFieldsMapped(log);
    }

    @Test
    void opal_business_unit_is_null_in_summary_dto() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_BusinessUnitNullFallback(log);
    }

    @Test
    void opal_surname_partial_match() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_SurnamePartialMatch(log);
    }

    @Test
    void opal_matchOnAlias_over_main() throws Exception {
        super.testPostDefendantAccountsSearch_Opal_MatchOnAlias_WhenMainPresent(log);
    }

    @Test
    void testAC1_MultiParameter_SurnameAndPostcode() throws Exception {
        super.testPostDefendantAccountsSearch_AC1_SurnameAndPostcode(log);
    }

    @Test
    void testAC1_MultiParameter_SurnameAndWrongPostcode() throws Exception {
        super.testPostDefendantAccountsSearch_AC1_SurnameAndWrongPostcode(log);
    }

    @Test
    void testAC1_MultiParameter_CompletePersonalDetails() throws Exception {
        super.testPostDefendantAccountsSearch_AC1_CompletePersonalDetails(log);
    }

    @Test
    void testAC1_MultiParameter_AddressAndNI() throws Exception {
        super.testPostDefendantAccountsSearch_AC1_AddressAndNI(log);
    }

    @Test
    void testAC1_MultiParameter_WrongBusinessUnitExcludes() throws Exception {
        super.testPostDefendantAccountsSearch_AC1_WrongBusinessUnitExcludes(log);
    }

    @Test
    void testAC2_BusinessUnitFiltering() throws Exception {
        super.testPostDefendantAccountsSearch_AC2_BusinessUnitFiltering(log);
    }

    @Test
    void testAC3a_ActiveAccountsOnlyFalse() throws Exception {
        super.testPostDefendantAccountsSearch_AC3a_ActiveAccountsOnlyFalse(log);
    }

    @Test
    void testAC5a_ForenamesPartialMatch() throws Exception {
        super.testPostDefendantAccountsSearch_AC5a_ForenamesPartialMatch(log);
    }

    @Test
    void testAC9_CompanyNameAndAddress() throws Exception {
        super.testPostDefendantAccountsSearch_AC9_CompanyNameAndAddress(log);
    }

    @Test
    void testAC9_CompanyNameAndPostcode() throws Exception {
        super.testPostDefendantAccountsSearch_AC9_CompanyNameAndPostcode(log);
    }

    @Test
    void testAC9_CompanyPartialNameAndAddress() throws Exception {
        super.testPostDefendantAccountsSearch_AC9_CompanyPartialNameAndAddress(log);
    }

    @Test
    void testAC9_CompanyNameAndWrongAddress() throws Exception {
        super.testPostDefendantAccountsSearch_AC9_CompanyNameAndWrongAddress(log);
    }

    @Test
    void testAC9_CompanyMultipleAddressFields() throws Exception {
        super.testPostDefendantAccountsSearch_AC9_CompanyMultipleAddressFields(log);
    }

    @Test
    void testAC9a_CompanyBusinessUnitFiltering() throws Exception {
        super.testPostDefendantAccountsSearch_AC9a_CompanyBusinessUnitFiltering(log);
    }

    @Test
    void testAC9b_CompanyActiveAccountsOnly() throws Exception {
        super.testPostDefendantAccountsSearch_AC9b_CompanyActiveAccountsOnly(log);
    }

    @Test
    void testAC9d_CompanyAliasExactMatch() throws Exception {
        super.testPostDefendantAccountsSearch_AC9d_CompanyAliasExactMatch(log);
    }

    @Test
    void testAC9di_CompanyAliasPartialMatch() throws Exception {
        super.testPostDefendantAccountsSearch_AC9di_CompanyAliasPartialMatch(log);
    }

    @Test
    void testAC9e_CompanyAddressPartialMatch() throws Exception {
        super.testPostDefendantAccountsSearch_AC9e_CompanyAddressPartialMatch(log);
    }

    @Test
    void testAC9ei_CompanyPostcodePartialMatch() throws Exception {
        super.testPostDefendantAccountsSearch_AC9ei_CompanyPostcodePartialMatch(log);
    }

}