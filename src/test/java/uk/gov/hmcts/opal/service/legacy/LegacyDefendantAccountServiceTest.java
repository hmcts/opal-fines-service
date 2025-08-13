package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.disco.legacy.LegacyTestsBase;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.legacy.LegacyCreateDefendantAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.legacy.common.DefendantDetails;
import uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.legacy.common.PaymentStateSummary;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LegacyDefendantAccountServiceTest extends LegacyTestsBase {

    @Spy
    private MockRestClient restClient = spy(MockRestClient.class);

    @Mock
    private LegacyGatewayProperties gatewayProperties;

    private GatewayService gatewayService;

    @InjectMocks
    private LegacyDefendantAccountService legacyDefendantAccountService;

    @BeforeEach
    void openMocks() throws Exception {
        gatewayService = Mockito.spy(new LegacyGatewayService(gatewayProperties, restClient));
        injectGatewayService(legacyDefendantAccountService, gatewayService);
    }

    private void injectGatewayService(
        LegacyDefendantAccountService legacyDefendantAccountService, GatewayService gatewayService)
        throws NoSuchFieldException, IllegalAccessException {

        Field field = LegacyDefendantAccountService.class.getDeclaredField("gatewayService");
        field.setAccessible(true);
        field.set(legacyDefendantAccountService, gatewayService);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetHeaderSummary_success() {

        DefendantAccountHeaderSummary headerSummary = createHeaderSummaryDto();
        LegacyGetDefendantAccountHeaderSummaryResponse responseBody = createHeaderSummaryResponse();

        ParameterizedTypeReference typeRef = new ParameterizedTypeReference<LegacyCreateDefendantAccountResponse>(){};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(responseBody);

        ResponseEntity<String> serverSuccessResponse =
            new ResponseEntity<>(responseBody.toXml(), HttpStatus.valueOf(200));
        when(restClient.responseSpec.toEntity(String.class)).thenReturn(serverSuccessResponse);

        DefendantAccountHeaderSummary published = legacyDefendantAccountService.getHeaderSummary(1L);

        assertEquals(headerSummary, published);
    }

    private DefendantAccountHeaderSummary createHeaderSummaryDto() {
        return DefendantAccountHeaderSummary.builder()
            .hasParentGuardian(false)
            .imposed(BigDecimal.ZERO)
            .arrears(BigDecimal.ZERO)
            .paid(BigDecimal.ZERO)
            .writtenOff(BigDecimal.ZERO)
            .accountBalance(BigDecimal.ZERO)
            .build();
    }

    private LegacyGetDefendantAccountHeaderSummaryResponse createHeaderSummaryResponse() {
        return LegacyGetDefendantAccountHeaderSummaryResponse.builder()
            .defendantDetails(
                DefendantDetails.builder()
                    .organisationDetails(OrganisationDetails.builder().build())
                    .individualDetails(IndividualDetails.builder().build())
                    .build())
            .accountStatusReference(AccountStatusReference.builder().build())
            .businessUnitSummary(BusinessUnitSummary.builder().build())
            .paymentStateSummary(PaymentStateSummary.builder().build())
            .build();
    }
}
