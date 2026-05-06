package uk.gov.hmcts.opal.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissionUser;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.CreditorAccountPaymentDetailsCommon;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;

@ActiveProfiles({"integration", "legacy"})
@Slf4j(topic = "opal.LegacyDefendantsIntegrationTest01")
@Sql(scripts = "classpath:db/insertData/insert_into_minor_creditors.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_minor_creditors.sql", executionPhase = AFTER_TEST_METHOD)
public class LegacyMinorCreditorIntegrationTest extends MinorCreditorControllerIntegrationTest {

    private static final String URL_BASE = "/minor-creditor-accounts";
    private static final String AUTH_HEADER = "Bearer some_value";
    private static final short PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID = 10;

    @Test
    void testPostSearchMinorCreditorSuccess() throws Exception {
        super.postSearchMinorCreditorImpl_Success(log);
    }

    @Test
    void testPostSearchMinorCreditor_500Error() throws Exception {
        super.legacyPostSearchMinorCreditorImpl_500Error(log);
    }

    @Test
    void testGetMinorCreditorAtAGlanceSuccess() throws Exception {
        super.getMinorCreditorAtAGlanceImpl_Success(log);
    }

    @Test
    void testGetMinorCreditorAtAGlance_500Error() throws Exception {
        super.legacyGetMinorCreditorAtAGlanceImpl_500Error(log);
    }

    @Test
    void testGetMinorCreditorHeaderSummarySuccess() throws Exception {
        super.getHeaderSummaryImpl_Success(log);
    }

    @Test
    void testGetMinorCreditorHeaderSummary_500Error() throws Exception {
        super.legacyGetMinorCreditorHeaderSummaryImpl_500Error(log);
    }

    @Test
    void testGetMinorCreditorAccountSuccess() throws Exception {
        super.getMinorCreditorAccountImpl_Success(log);
    }

    @Test
    void testGetMinorCreditorAccountFiltersBacsWithoutPermission() throws Exception {
        super.getMinorCreditorAccountImpl_filtersBacsDetailsWithoutPermission(log);
    }

    @Test
    void testGetMinorCreditorAccountMissingAuthHeaderReturns401() throws Exception {
        super.getMinorCreditorAccount_missingAuthHeader_returns401();
    }

    @Test
    void testGetMinorCreditorAccountAuthenticatedWithoutPermissionReturns403() throws Exception {
        super.getMinorCreditorAccount_authenticatedWithoutPermission_returns403();
    }

    @Test
    void testGetMinorCreditorAccount_500Error() throws Exception {
        super.legacyGetMinorCreditorAccountImpl_500Error(log);
    }

    @Test
    void patchMinorCreditor_withoutPermission_returns403() throws Exception {
        super.patchMinorCreditor_withoutPermission_returns403();
    }

    @Test
    void patchMinorCreditor_withoutHoldPermission_returns403() throws Exception {
        super.patchMinorCreditor_withoutHoldPermission_returns403();
    }

    @Test
    void patchMinorCreditor_withoutAccountMaintenancePermission_returns403() throws Exception {
        super.patchMinorCreditor_withoutAccountMaintenancePermission_returns403();
    }

    @Test
    void patchMinorCreditor_missingAuthHeader_returns401() throws Exception {
        super.patchMinorCreditor_missingAuthHeader_returns401();
    }

    @Test
    void patchMinorCreditor_timeout_returns408() throws Exception {
        super.patchMinorCreditor_timeout_returns408(log);
    }

    @Test
    void patchMinorCreditor_missingPayload_returns400() throws Exception {
        super.patchMinorCreditor_missingPayload_returns400();
    }

    private PatchMinorCreditorAccountRequest patchMinorCreditorLegacyRequest() {
        return new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon()
                .partyId("99008")
                .organisationFlag(true)
                .organisationDetails(new OrganisationDetailsCommon().organisationName("Updated Ltd")))
            .address(new AddressDetailsCommon()
                .addressLine1("99 Updated Road")
                .addressLine2("Updated Area")
                .addressLine3("Updated Town")
                .postcode("NW1 1AA"))
            .payment(new CreditorAccountPaymentDetailsCommon()
                .accountName("Updated Account")
                .sortCode("112233")
                .accountNumber("12345678")
                .accountReference("Ref-01")
                .payByBacs(true)
                .holdPayment(true));
    }
}
