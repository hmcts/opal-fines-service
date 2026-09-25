package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Creditor;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.CreditorAccountType;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Imposition;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Offence;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.PostedDetails;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Result;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetImpositionsRequest;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionCommon;
import uk.gov.hmcts.opal.generated.model.ImpositionCreditorReferenceCommon.AccountTypeEnum;
import uk.gov.hmcts.opal.generated.model.ImpositionCreditorReferenceCommon.DisplayNameEnum;

@ExtendWith(MockitoExtension.class)
class LegacyImpositionServiceTest {

    @Mock
    private GatewayService gatewayService;

    @InjectMocks
    private LegacyImpositionService legacyImpositionService;

    @Nested
    class GetImpositions {

        @Test
        void whenGatewayReturnsLegacyResponse_mapsImposition_happyPath() {
            ArgumentCaptor<LegacyGetImpositionsRequest> requestCaptor =
                ArgumentCaptor.forClass(LegacyGetImpositionsRequest.class);

            when(gatewayService.postToGateway(
                eq(LegacyImpositionService.GET_IMPOSITIONS),
                eq(GetDefendantAccountImpositionsLegacyResponse.class),
                requestCaptor.capture(),
                isNull()
            )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse(), null, null));

            GetDefendantAccountImpositionsResponse response = legacyImpositionService.getImpositions(12345L);
            DefendantAccountImpositionCommon imposition = response.getPayload().getImpositions().getFirst();

            assertAll(
                () -> verify(gatewayService).postToGateway(
                    eq(LegacyImpositionService.GET_IMPOSITIONS),
                    eq(GetDefendantAccountImpositionsLegacyResponse.class),
                    eq(requestCaptor.getValue()),
                    isNull()
                ),
                () -> assertEquals("12345", requestCaptor.getValue().getDefendantAccountId()),
                () -> assertEquals(new BigInteger("18338687664539878704807506660830801130000030349"),
                                   response.getVersion()),
                () -> assertNotNull(response.getPayload()),
                () -> assertEquals(1, response.getPayload().getImpositions().size()),
                () -> assertEquals(LocalDate.parse("2026-08-19"), imposition.getDateAdded()),
                () -> assertEquals(LocalDate.parse("2025-05-15"), imposition.getDateImposed()),
                () -> assertEquals(new BigDecimal("-250.00"), imposition.getImposedAmount()),
                () -> assertEquals(new BigDecimal("300.00"), imposition.getPaidAmount()),
                () -> assertEquals(new BigDecimal("50.00"), imposition.getBalance()),
                () -> assertEquals(770000027211L, imposition.getImpositionId()),
                () -> assertResult(imposition),
                () -> assertCreditor(imposition),
                () -> assertOffence(imposition),
                () -> assertNull(imposition.getImposedBy())
            );
        }

        @Test
        void whenGatewayReturnsNullEntity_returnsNull_sadPath() {
            mock_getImpositionsResponse(null);

            assertNull(legacyImpositionService.getImpositions(12345L));
        }

        @Test
        void whenGatewayReturnsEmptyImposition_mapsNullFields_happyPath() {
            mock_getImpositionsResponse(GetDefendantAccountImpositionsLegacyResponse.builder()
                .version(BigInteger.ONE)
                .impositions(List.of(Imposition.builder().build()))
                .build());

            DefendantAccountImpositionCommon imposition = legacyImpositionService.getImpositions(12345L)
                .getPayload().getImpositions().getFirst();

            assertAll(
                () -> assertNull(imposition.getDateAdded()),
                () -> assertNull(imposition.getImposition()),
                () -> assertNull(imposition.getCreditor()),
                () -> assertNull(imposition.getOffence()),
                () -> assertNull(imposition.getImposedBy())
            );
        }
    }

    private void assertResult(DefendantAccountImpositionCommon imposition) {
        assertAll(
            () -> assertEquals("FO", imposition.getImposition().getResultId()),
            () -> assertEquals("FINE", imposition.getImposition().getResultTitle())
        );
    }

    private void assertCreditor(DefendantAccountImpositionCommon imposition) {
        assertAll(
            () -> assertEquals(77L, imposition.getCreditor().getCreditorAccountId()),
            () -> assertEquals(AccountTypeEnum.CF, imposition.getCreditor().getAccountType()),
            () -> assertEquals(DisplayNameEnum.CENTRAL_FUND, imposition.getCreditor().getDisplayName()),
            () -> assertNull(imposition.getCreditor().getMajorCreditorId()),
            () -> assertNull(imposition.getCreditor().getMinorCreditorPartyId()),
            () -> assertEquals("HM Courts & Tribunals Service", imposition.getCreditor().getName())
        );
    }

    private void assertOffence(DefendantAccountImpositionCommon imposition) {
        assertAll(
            () -> assertEquals(33369L, imposition.getOffence().getId()),
            () -> assertEquals("HY35014", imposition.getOffence().getCode()),
            () -> assertEquals("Riding a bicycle on a footpath", imposition.getOffence().getTitle())
        );
    }

    private GetDefendantAccountImpositionsLegacyResponse legacyResponse() {
        return GetDefendantAccountImpositionsLegacyResponse.builder()
            .version(new BigInteger("18338687664539878704807506660830801130000030349"))
            .impositions(List.of(Imposition.builder()
                .postedDetails(PostedDetails.builder()
                    .postedDate(LocalDateTime.parse("2026-08-19T00:00:00.00001"))
                    .postedBy("L077AO")
                    .postedByName("L077AO")
                    .build())
                .result(Result.builder().resultId("FO").resultTitle("FINE").build())
                .creditor(Creditor.builder()
                    .creditorAccountType(CreditorAccountType.builder().creditorAccountType("CF").build())
                    .creditorAccountId(77L)
                    .majorCreditorName("HM Courts & Tribunals Service")
                    .build())
                .imposedAmount(new BigDecimal("-250.00"))
                .paidAmount(new BigDecimal("300.00"))
                .balance(new BigDecimal("50.00"))
                .dateImposed(LocalDate.parse("2025-05-15"))
                .offence(Offence.builder()
                    .offenceId(33369L)
                    .cjsCode("HY35014")
                    .offenceTitle("Riding a bicycle on a footpath")
                    .build())
                .impositionId(770000027211L)
                .build()))
            .build();
    }

    private void mock_getImpositionsResponse(GetDefendantAccountImpositionsLegacyResponse response) {
        when(gatewayService.postToGateway(
            eq(LegacyImpositionService.GET_IMPOSITIONS),
            eq(GetDefendantAccountImpositionsLegacyResponse.class),
            eq(LegacyGetImpositionsRequest.builder().defendantAccountId("12345").build()),
            isNull()
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, response, null, null));
    }
}
