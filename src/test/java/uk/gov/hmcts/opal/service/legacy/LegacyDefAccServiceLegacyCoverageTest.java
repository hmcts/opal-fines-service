package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTerms;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.legacy.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails;

class LegacyDefAccServiceLegacyCoverageTest extends AbstractLegacyDefAccServiceTest {

    @Test
    void legacy_getHeaderSummary_legacyFailureBranch_logsAndContinues() {
        LegacyGetDefendantAccountHeaderSummaryResponse entity =
            LegacyGetDefendantAccountHeaderSummaryResponse.builder()
                .accountNumber("ACC")
                .accountStatusReference(
                    AccountStatusReference.builder().accountStatusCode("L").build())
                .build();

        GatewayService.Response<LegacyGetDefendantAccountHeaderSummaryResponse> resp =
            new GatewayService.Response<>(HttpStatus.INTERNAL_SERVER_ERROR, entity, "<LEGACY_FAIL/>", null);

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.GET_HEADER_SUMMARY),
            eq(LegacyGetDefendantAccountHeaderSummaryResponse.class),
            any(),
            any()
        );

        DefendantAccountHeaderSummary out = legacyDefendantAccountService.getHeaderSummary(123L);

        assertNotNull(out);
        assertEquals("ACC", out.getAccountNumber());
    }

    @Test
    void legacy_getHeaderSummary_successBranch_hitsLoggingAndMaps() {
        LegacyGetDefendantAccountHeaderSummaryResponse entity =
            LegacyGetDefendantAccountHeaderSummaryResponse.builder()
                .accountNumber("OKAY")
                .accountStatusReference(
                    AccountStatusReference.builder().accountStatusCode("L").build())
                .build();

        GatewayService.Response<LegacyGetDefendantAccountHeaderSummaryResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, entity, null, null);

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.GET_HEADER_SUMMARY),
            eq(LegacyGetDefendantAccountHeaderSummaryResponse.class),
            any(),
            any()
        );

        DefendantAccountHeaderSummary out = legacyDefendantAccountService.getHeaderSummary(99L);

        assertEquals("OKAY", out.getAccountNumber());
    }

    @Test
    void legacyAliasFilters_dropNullAliasIdAndNullOrgName() {
        OrganisationDetails.OrganisationAlias alias1 = OrganisationDetails.OrganisationAlias
            .builder().aliasId(null).sequenceNumber((short) 1).organisationName("Name").build();
        OrganisationDetails.OrganisationAlias alias2 = OrganisationDetails.OrganisationAlias
            .builder().aliasId("OK").sequenceNumber((short) 2).organisationName(null).build();
        OrganisationDetails.OrganisationAlias alias3 = OrganisationDetails.OrganisationAlias
            .builder().aliasId("GOOD").sequenceNumber((short) 3).organisationName("X").build();

        OrganisationDetails org = OrganisationDetails.builder()
            .organisationName("Main")
            .organisationAliases(new OrganisationDetails.OrganisationAlias[] {
                alias1, alias2, alias3
            })
            .build();

        LegacyPartyDetails party = LegacyPartyDetails.builder()
            .organisationFlag(true)
            .organisationDetails(org)
            .build();

        LegacyGetDefendantAccountHeaderSummaryResponse respLg = LegacyGetDefendantAccountHeaderSummaryResponse.builder()
            .partyDetails(party)
            .accountStatusReference(
                AccountStatusReference.builder().accountStatusCode("L").build())
            .build();

        doReturn(new GatewayService.Response<>(HttpStatus.OK, respLg, null, null))
            .when(gatewayService).postToGateway(any(), any(), any(), any());

        DefendantAccountHeaderSummary out = legacyDefendantAccountService.getHeaderSummary(5L);

        assertEquals(1, out.getPartyDetails().getOrganisationDetails().getOrganisationAliases().size());
        assertEquals("GOOD", out.getPartyDetails().getOrganisationDetails()
            .getOrganisationAliases().get(0).getAliasId());
    }

    @Test
    void legacyIndividual_dateOfBirthFormattingBranch() {
        IndividualDetails ind =
            IndividualDetails.builder()
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .surname("Smith")
                .build();

        LegacyPartyDetails party = LegacyPartyDetails.builder()
            .individualDetails(ind)
            .organisationFlag(false)
            .build();

        LegacyGetDefendantAccountHeaderSummaryResponse respLg = LegacyGetDefendantAccountHeaderSummaryResponse.builder()
            .partyDetails(party)
            .accountStatusReference(AccountStatusReference.builder().accountStatusCode("L").build())
            .build();

        doReturn(new GatewayService.Response<>(HttpStatus.OK, respLg, null, null))
            .when(gatewayService).postToGateway(any(), any(), any(), any());

        DefendantAccountHeaderSummary out = legacyDefendantAccountService.getHeaderSummary(77L);

        assertEquals("1990-01-01", out.getPartyDetails().getIndividualDetails().getDateOfBirth());
    }

    @Test
    void legacyPaymentTerms_nonNullEnums_areConverted() {
        LegacyGetDefendantAccountPaymentTermsResponse legacy = LegacyGetDefendantAccountPaymentTermsResponse.builder()
            .version(1L)
            .paymentTerms(
                LegacyPaymentTerms.builder()
                    .paymentTermsType(new LegacyPaymentTermsType(LegacyPaymentTermsType.PaymentTermsTypeCode.B))
                    .instalmentPeriod(new LegacyInstalmentPeriod(LegacyInstalmentPeriod.InstalmentPeriodCode.W))
                    .build()
            )
            .build();

        doReturn(new GatewayService.Response<>(HttpStatus.OK, legacy, null, null))
            .when(gatewayService).postToGateway(eq(LegacyDefendantAccountService.GET_PAYMENT_TERMS),
                eq(LegacyGetDefendantAccountPaymentTermsResponse.class), any(), any());

        GetDefendantAccountPaymentTermsResponse out = legacyDefendantAccountService.getPaymentTerms(123L);

        assertNotNull(out.getPaymentTerms().getPaymentTermsType());
        assertNotNull(out.getPaymentTerms().getInstalmentPeriod());
    }
}
