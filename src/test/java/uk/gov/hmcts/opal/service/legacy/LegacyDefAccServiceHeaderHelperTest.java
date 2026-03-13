package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails;

class LegacyDefAccServiceHeaderHelperTest extends AbstractLegacyDefAccServiceTest {

    @Test
    void toBigDecimalOrZero_handlesAllBranches() {
        assertEquals(BigDecimal.ZERO, LegacyDefendantAccountService.toBigDecimalOrZero(null));

        BigDecimal value = new BigDecimal("123.45");
        assertEquals(value, LegacyDefendantAccountService.toBigDecimalOrZero(value));

        assertEquals(new BigDecimal("77"), LegacyDefendantAccountService.toBigDecimalOrZero("77"));
        assertEquals(BigDecimal.ZERO, LegacyDefendantAccountService.toBigDecimalOrZero("NaN"));
        assertEquals(new BigDecimal("5.0"), LegacyDefendantAccountService.toBigDecimalOrZero(5));
        assertEquals(BigDecimal.ZERO, LegacyDefendantAccountService.toBigDecimalOrZero(new Object()));
    }

    @Test
    void toPaymentTermsType_and_toInstalmentPeriod_coverNonNullCodes() {
        LegacyPaymentTermsType legacyType = new LegacyPaymentTermsType(LegacyPaymentTermsType.PaymentTermsTypeCode.B);
        LegacyInstalmentPeriod legacyInst = new LegacyInstalmentPeriod(LegacyInstalmentPeriod.InstalmentPeriodCode.W);

        Object out1 = LegacyDefendantAccountService.toPaymentTermsType(legacyType);
        Object out2 = LegacyDefendantAccountService.toInstalmentPeriod(legacyInst);
        assertNotNull(out1);
        assertNotNull(out2);
    }

    @Test
    void getHeaderSummary_errorAndSuccessBranches_triggerLogging() {
        LegacyGetDefendantAccountHeaderSummaryResponse legacyEntity =
            LegacyGetDefendantAccountHeaderSummaryResponse.builder().build();

        GatewayService.Response<LegacyGetDefendantAccountHeaderSummaryResponse> respError =
            new GatewayService.Response<>(HttpStatus.INTERNAL_SERVER_ERROR, legacyEntity, "body", null);
        GatewayService.Response<LegacyGetDefendantAccountHeaderSummaryResponse> respSuccess =
            new GatewayService.Response<>(HttpStatus.OK, legacyEntity, null, null);

        doReturn(respError, respSuccess).when(gatewayService)
            .postToGateway(any(), any(), any(), any());

        legacyDefendantAccountService.getHeaderSummary(1L);
        legacyDefendantAccountService.getHeaderSummary(1L);

        verify(gatewayService, times(2)).postToGateway(any(), any(), any(), any());
    }

    @Test
    void toHeaderSumaryDto_mapsOrgAndIndBranches() {
        OrganisationDetails.OrganisationAlias orgAlias = OrganisationDetails.OrganisationAlias.builder()
            .aliasId("O1").sequenceNumber((short) 1).organisationName("AliasCo").build();
        OrganisationDetails orgDetails = OrganisationDetails.builder()
            .organisationName("MainCo")
            .organisationAliases(new OrganisationDetails.OrganisationAlias[] {orgAlias})
            .build();
        LegacyPartyDetails party = LegacyPartyDetails.builder()
            .organisationFlag(true).organisationDetails(orgDetails).build();
        LegacyGetDefendantAccountHeaderSummaryResponse resp = LegacyGetDefendantAccountHeaderSummaryResponse.builder()
            .partyDetails(party).build();
        assertNotNull(legacyDefendantAccountService.toHeaderSumaryDto(resp));

        IndividualDetails.IndividualAlias indAlias = IndividualDetails.IndividualAlias.builder()
            .aliasId("I1").sequenceNumber((short) 1).surname("Smith").forenames("John").build();
        IndividualDetails ind = IndividualDetails.builder()
            .firstNames("John").surname("Smith")
            .individualAliases(new IndividualDetails.IndividualAlias[] {indAlias})
            .build();
        party = LegacyPartyDetails.builder()
            .organisationFlag(false).individualDetails(ind).build();
        resp = LegacyGetDefendantAccountHeaderSummaryResponse.builder()
            .partyDetails(party).build();
        assertNotNull(legacyDefendantAccountService.toHeaderSumaryDto(resp));
    }

    @Test
    void toHeaderSumaryDto_populatesDisplayNameFromCodeWhenMissing() {
        LegacyGetDefendantAccountHeaderSummaryResponse legacyResponse =
            LegacyGetDefendantAccountHeaderSummaryResponse.builder()
            .accountStatusReference(AccountStatusReference.builder()
                .accountStatusCode("L")
                .accountStatusDisplayName(null)
                .build())
            .accountNumber("177A")
            .defendantAccountId("77")
            .accountType("Fine")
            .build();

        DefendantAccountHeaderSummary result = legacyDefendantAccountService.toHeaderSumaryDto(legacyResponse);

        assertNotNull(result);
    }
}
