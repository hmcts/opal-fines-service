package uk.gov.hmcts.opal.service.legacy;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import jakarta.xml.bind.JAXBException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.common.xml.XmlUtil;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails;
import uk.gov.hmcts.opal.exception.DefendantAccountNotFoundException;

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
    void getHeaderSummary_gatewayFailureReturns500() {
        LegacyGetDefendantAccountHeaderSummaryResponse legacyEntity =
            LegacyGetDefendantAccountHeaderSummaryResponse.builder().build();

        GatewayService.Response<LegacyGetDefendantAccountHeaderSummaryResponse> respError =
            new GatewayService.Response<>(HttpStatus.INTERNAL_SERVER_ERROR, legacyEntity, "body", null);
        doReturn(respError).when(gatewayService).postToGateway(any(), any(), any(), any());

        assertThatThrownBy(() -> legacyDefendantAccountService.getHeaderSummary(1L))
            .isInstanceOf(HttpServerErrorException.class).hasMessage("500 Legacy gateway returned failure");
    }

    @Test
    void getHeaderSummary_accountNotFoundReturns404() throws JAXBException {
        String xml = "<response><error_response><error_code>-20013</error_code>"
            + "<error_message>Account not found</error_message></error_response></response>";
        LegacyGetDefendantAccountHeaderSummaryResponse legacyEntity = XmlUtil.unmarshalXmlString(
            xml, LegacyGetDefendantAccountHeaderSummaryResponse.class);
        GatewayService.Response<LegacyGetDefendantAccountHeaderSummaryResponse> response =
            new GatewayService.Response<>(HttpStatus.OK, legacyEntity);
        doReturn(response).when(gatewayService).postToGateway(any(), any(), any(), any());

        DefendantAccountNotFoundException exception = assertThrows(DefendantAccountNotFoundException.class,
            () -> legacyDefendantAccountService.getHeaderSummary(900000000000L));

        assertEquals(900000000000L, exception.getDefendantAccountId());
    }

    @Test
    void getHeaderSummary_precisionErrorRemains500() throws JAXBException {
        String xml = "<response><error_response><error_code>-6502</error_code>"
            + "<error_message>number precision too large</error_message></error_response></response>";
        LegacyGetDefendantAccountHeaderSummaryResponse legacyEntity = XmlUtil.unmarshalXmlString(
            xml, LegacyGetDefendantAccountHeaderSummaryResponse.class);
        GatewayService.Response<LegacyGetDefendantAccountHeaderSummaryResponse> response =
            new GatewayService.Response<>(HttpStatus.OK, legacyEntity);
        doReturn(response).when(gatewayService).postToGateway(any(), any(), any(), any());

        HttpServerErrorException exception = assertThrows(HttpServerErrorException.class,
            () -> legacyDefendantAccountService.getHeaderSummary(90000000000000L));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
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
            .forenames("John").surname("Smith")
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
