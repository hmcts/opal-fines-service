package uk.gov.hmcts.opal.controllers.defendant;


import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Compatibility bridge class to preserve existing super.*(log) calls
 * in OpalDefendantAccountsIntegrationTest and LegacyDefendantAccountsIntegrationTest.
 * Delegates to the new split integration test classes grouped by API area.
 */

@SpringBootTest
public abstract class DefendantAccountsControllerIntegrationTest extends BaseDefendantAccountsIntegrationTest {


    // --- Test class delegates (sharing Spring context via base injection) ---
    protected final DefendantAccountPartyIntegrationTest party =
        new DefendantAccountPartyIntegrationTest();
    protected final DefendantAccountPaymentTermsIntegrationTest paymentTerms =
        new DefendantAccountPaymentTermsIntegrationTest();
    protected final DefendantAccountSearchIntegrationTest search = new DefendantAccountSearchIntegrationTest();
    protected final DefendantAccountUpdateIntegrationTest update = new DefendantAccountUpdateIntegrationTest();

    @BeforeEach
    void propagateBaseContextToDelegates() {
//        injectBaseDependencies(headerSummary);
//        injectBaseDependencies(atAGlance);
//        injectBaseDependencies(party);
//        injectBaseDependencies(paymentTerms);
//        injectBaseDependencies(search);
//        injectBaseDependencies(update);
    }

    /*
      @Autowired protected DefendantAccountHeaderSummaryIntegrationTest headerSummary;
      @Autowired protected DefendantAccountAtAGlanceIntegrationTest atAGlance;
      @Autowired protected DefendantAccountPartyIntegrationTest party;
      @Autowired protected DefendantAccountPaymentTermsIntegrationTest paymentTerms;
      @Autowired protected DefendantAccountSearchIntegrationTest search;
      @Autowired protected DefendantAccountUpdateIntegrationTest update;
     */



    // Payment Terms
    public void testGetPaymentTerms(Logger log) throws Exception {
        paymentTerms.testGetPaymentTerms(log);
    }

    public void testGetPaymentTermsLatest_NoPaymentTermFoundForId(Logger log) throws Exception {
        paymentTerms.testGetPaymentTermsLatest_NoPaymentTermFoundForId(log);
    }

    public void getDefendantAccountPaymentTerms_500Error(Logger log) throws Exception {
        paymentTerms.getDefendantAccountPaymentTerms_500Error(log);
    }

    public void testLegacyGetPaymentTerms(Logger log) throws Exception {
        paymentTerms.testLegacyGetPaymentTerms(log);
    }

    // Defendant Account Party
    public void opalGetDefendantAccountParty_Happy(Logger log) throws Exception {
        party.opalGetDefendantAccountParty_Happy(log);
    }

    public void opalGetDefendantAccountParty_Organisation(Logger log) throws Exception {
        party.opalGetDefendantAccountParty_Organisation(log);
    }

    public void opalGetDefendantAccountParty_NullFields(Logger log) throws Exception {
        party.opalGetDefendantAccountParty_NullFields(log);
    }

    public void legacyGetDefendantAccountParty_Happy(Logger log) throws Exception {
        party.legacyGetDefendantAccountParty_Happy(log);
    }

    public void legacyGetDefendantAccountParty_Organisation(Logger log) throws Exception {
        party.legacyGetDefendantAccountParty_Organisation(log);
    }

    public void legacyGetDefendantAccountParty_500Error(Logger log) throws Exception {
        party.legacyGetDefendantAccountParty_500Error(log);
    }

    // Search
    public void testPostDefendantAccountsSearch_Opal(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal(log);
    }

    public void testPostDefendantAccountsSearch_Opal_NoResults(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal_NoResults(log);
    }

    public void testPostDefendantAccountsSearch_PO2241_Core177_InactiveStillReturned(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_PO2241_Core177_InactiveStillReturned(log);
    }

    public void testPostDefendantAccountsSearch_OrganisationFlagRespected(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_OrganisationFlagRespected(log);
    }

    public void patch_returnsETag_andResponseConformsToSchema(Logger log) throws Exception {
        update.patch_returnsETag_andResponseConformsToSchema(log);
    }

    public void testPostDefendantAccountsSearch_AC9_CompanyMultipleAddressFields(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_AC9_CompanyMultipleAddressFields(log);
    }

    public void testPostDefendantAccountsSearch_AC9a_CompanyBusinessUnitFiltering(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_AC9a_CompanyBusinessUnitFiltering(log);
    }

    public void testPostDefendantAccountsSearch_AC9b_CompanyActiveAccountsOnly(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_AC9b_CompanyActiveAccountsOnly(log);
    }

    public void testPostDefendantAccountsSearch_AC9d_CompanyAliasExactMatch(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_AC9d_CompanyAliasExactMatch(log);
    }

    public void testPostDefendantAccountsSearch_AC9di_CompanyAliasPartialMatch(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_AC9di_CompanyAliasPartialMatch(log);
    }

    public void testPostDefendantAccountsSearch_AC9e_CompanyAddressPartialMatch(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_AC9e_CompanyAddressPartialMatch(log);
    }

    public void testPostDefendantAccountsSearch_AC9ei_CompanyPostcodePartialMatch(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_AC9ei_CompanyPostcodePartialMatch(log);
    }

    public void testPostDefendantAccountsSearch_Opal_ByNameAndBU(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal_ByNameAndBU(log);
    }

    public void testPostDefendantAccountsSearch_Opal_Postcode_IgnoresSpaces(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal_Postcode_IgnoresSpaces(log);
    }

    public void testPostDefendantAccountsSearch_Opal_AccountNumberStartsWith(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal_AccountNumberStartsWith(log);
    }

    public void testPostDefendantAccountsSearch_Opal_PcrExact(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal_PcrExact(log);
    }

    public void testPostDefendantAccountsSearch_Opal_PcrNoMatch(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal_PcrNoMatch(log);
    }

    public void testPostDefendantAccountsSearch_Opal_NiStartsWith(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal_NiStartsWith(log);
    }

    public void testPostDefendantAccountsSearch_Opal_AddressStartsWith(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal_AddressStartsWith(log);
    }

    public void testPostDefendantAccountsSearch_Opal_DobExact(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal_DobExact(log);
    }

    public void testPostDefendantAccountsSearch_Opal_AliasFlag_UsesMainName(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal_AliasFlag_UsesMainName(log);
    }

    public void testPostDefendantAccountsSearch_Opal_ActiveAccountsOnlyFalse(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal_ActiveAccountsOnlyFalse(log);
    }

    public void testPostDefendantAccountsSearch_Opal_AccountNumber_WithCheckLetter(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal_AccountNumber_WithCheckLetter(log);
    }

    public void testPostDefendantAccountsSearch_Opal_NoDefendantObject_StillResolvesParty(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal_NoDefendantObject_StillResolvesParty(log);
    }

    public void testPostDefendantAccountsSearch_Opal_WithoutBusinessUnitFilter(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal_WithoutBusinessUnitFilter(log);
    }

    public void testPostDefendantAccountsSearch_Opal_AnnaGraham_FullDetails(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal_AnnaGraham_FullDetails(log);
    }

    public void testPostDefendantAccountsSearch_Opal_OrganisationWithNoPersonalNames(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal_OrganisationWithNoPersonalNames(log);
    }

    public void testPostDefendantAccountsSearch_Opal_AliasFallbackToMainName(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal_AliasFallbackToMainName(log);
    }

    public void testPostDefendantAccountsSearch_Opal_OptionalFieldsPresentAndMissing(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal_OptionalFieldsPresentAndMissing(log);
    }

    public void testPostDefendantAccountsSearch_Opal_AliasFieldsMapped(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal_AliasFieldsMapped(log);
    }

    public void testPostDefendantAccountsSearch_Opal_BusinessUnitNullFallback(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal_BusinessUnitNullFallback(log);
    }

    public void testPostDefendantAccountsSearch_Opal_SurnamePartialMatch(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal_SurnamePartialMatch(log);
    }

    public void testPostDefendantAccountsSearch_Opal_MatchOnAlias_WhenMainPresent(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_Opal_MatchOnAlias_WhenMainPresent(log);
    }

    // --- AC1–AC9 Multi-Parameter Search Scenarios (added for full parity) ---

    public void testPostDefendantAccountsSearch_AC1_SurnameAndPostcode(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_AC1_SurnameAndPostcode(log);
    }

    public void testPostDefendantAccountsSearch_AC1_SurnameAndWrongPostcode(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_AC1_SurnameAndWrongPostcode(log);
    }

    public void testPostDefendantAccountsSearch_AC1_CompletePersonalDetails(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_AC1_CompletePersonalDetails(log);
    }

    public void testPostDefendantAccountsSearch_AC1_AddressAndNI(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_AC1_AddressAndNI(log);
    }

    public void testPostDefendantAccountsSearch_AC1_WrongBusinessUnitExcludes(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_AC1_WrongBusinessUnitExcludes(log);
    }

    public void testPostDefendantAccountsSearch_AC2_BusinessUnitFiltering(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_AC2_BusinessUnitFiltering(log);
    }

    public void testPostDefendantAccountsSearch_AC3a_ActiveAccountsOnlyFalse(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_AC3a_ActiveAccountsOnlyFalse(log);
    }

    public void testPostDefendantAccountsSearch_AC5a_ForenamesPartialMatch(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_AC5a_ForenamesPartialMatch(log);
    }

    public void testPostDefendantAccountsSearch_AC9_CompanyNameAndAddress(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_AC9_CompanyNameAndAddress(log);
    }

    public void testPostDefendantAccountsSearch_AC9_CompanyNameAndPostcode(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_AC9_CompanyNameAndPostcode(log);
    }

    public void testPostDefendantAccountsSearch_AC9_CompanyPartialNameAndAddress(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_AC9_CompanyPartialNameAndAddress(log);
    }

    public void testPostDefendantAccountsSearch_AC9_CompanyNameAndWrongAddress(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_AC9_CompanyNameAndWrongAddress(log);
    }

    public void testWrongMediaTypeContainsRetriableField(Logger log) throws Exception {
        search.testWrongMediaTypeContainsRetriableField(log);
    }

    public void testInvalidBodyContainsRetriable(Logger log) throws Exception {
        search.testInvalidBodyContainsRetriable(log);
    }

    public void testPostDefendantAccountsSearch(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch(log);
    }

    public void testPostDefendantAccountsSearch_WhenNoDefendantAccountsFound(Logger log) throws Exception {
        search.testPostDefendantAccountsSearch_WhenNoDefendantAccountsFound(log);
    }


    // Update
    public void opalUpdateDefendantAccount_Happy(Logger log) throws Exception {
        update.opalUpdateDefendantAccount_Happy(log);
    }

    public void patch_conflict_whenIfMatchDoesNotMatch(Logger log) throws Exception {
        update.patch_conflict_whenIfMatchDoesNotMatch(log);
    }

    public void patch_conflict_whenIfMatchMissing(Logger log) throws Exception {
        update.patch_conflict_whenIfMatchMissing(log);
    }

    public void patch_forbidden_whenUserLacksAccountMaintenance(Logger log) throws Exception {
        update.patch_forbidden_whenUserLacksAccountMaintenance(log);
    }

    public void patch_notFound_whenAccountNotInHeaderBU(Logger log) throws Exception {
        update.patch_notFound_whenAccountNotInHeaderBU(log);
    }

    public void patch_badRequest_whenMultipleGroupsProvided(Logger log) throws Exception {
        update.patch_badRequest_whenMultipleGroupsProvided(log);
    }

    public void patch_badRequest_whenTypesInvalid(Logger log) throws Exception {
        update.patch_badRequest_whenTypesInvalid(log);
    }

    public void patch_updatesEnforcementCourt_andValidatesResponseSchema(Logger log) throws Exception {
        update.patch_updatesEnforcementCourt_andValidatesResponseSchema(log);
    }

    public void patch_updatesCollectionOrder(Logger log) throws Exception {
        update.patch_updatesCollectionOrder(log);
    }

    public void patch_updatesEnforcementOverride(Logger log) throws Exception {
        update.patch_updatesEnforcementOverride(log);
    }
}
