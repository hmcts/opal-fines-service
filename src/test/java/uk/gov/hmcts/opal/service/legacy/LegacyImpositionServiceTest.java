package uk.gov.hmcts.opal.service.legacy;

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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyCourtReferenceCommon;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountImpositionCommon;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountImpositionsResponseCommon;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetImpositionsRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyImpositionCreditorReferenceCommon;
import uk.gov.hmcts.opal.dto.legacy.LegacyOffenceReferenceCommon;
import uk.gov.hmcts.opal.dto.legacy.LegacyResultReferenceCommon;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionCommon;
import uk.gov.hmcts.opal.generated.model.ImpositionCreditorReferenceCommon.AccountTypeEnum;
import uk.gov.hmcts.opal.generated.model.ImpositionCreditorReferenceCommon.DisplayNameEnum;

@ExtendWith(MockitoExtension.class)
class LegacyImpositionServiceTest {

    @Mock
    private GatewayService gatewayService;

    @InjectMocks
    private LegacyImpositionService legacyImpositionService;

    @Test
    void getImpositions_postsLegacyRequestAndMapsResponse() {
        LegacyDefendantAccountImpositionsResponseCommon legacyResponse =
            LegacyDefendantAccountImpositionsResponseCommon.builder()
                .version(4L)
                .impositions(List.of(legacyImposition()))
                .build();

        ArgumentCaptor<LegacyGetImpositionsRequest> requestCaptor =
            ArgumentCaptor.forClass(LegacyGetImpositionsRequest.class);

        when(gatewayService.postToGateway(
            eq(LegacyImpositionService.GET_IMPOSITIONS),
            eq(LegacyDefendantAccountImpositionsResponseCommon.class),
            requestCaptor.capture(),
            isNull()
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse, null, null));

        GetDefendantAccountImpositionsResponse response = legacyImpositionService.getImpositions(12345L);

        verify(gatewayService).postToGateway(
            eq(LegacyImpositionService.GET_IMPOSITIONS),
            eq(LegacyDefendantAccountImpositionsResponseCommon.class),
            eq(requestCaptor.getValue()),
            isNull()
        );

        assertEquals("12345", requestCaptor.getValue().getDefendantAccountId());
        assertEquals(BigInteger.valueOf(4), response.getVersion());
        assertNotNull(response.getPayload());
        assertEquals(1, response.getPayload().getImpositions().size());

        DefendantAccountImpositionCommon imposition = response.getPayload().getImpositions().getFirst();
        assertEquals(LocalDate.parse("2026-05-06"), imposition.getDateAdded());
        assertEquals(LocalDate.parse("2026-05-05"), imposition.getDateImposed());
        assertEquals(new BigDecimal("600.00"), imposition.getImposedAmount());
        assertEquals(new BigDecimal("60.00"), imposition.getPaidAmount());
        assertEquals(new BigDecimal("540.00"), imposition.getBalance());
        assertEquals(99000000003006L, imposition.getImpositionId());

        assertEquals("ABDC", imposition.getImposition().getResultId());
        assertEquals("Application made for Benefit Deductions", imposition.getImposition().getResultTitle());

        assertEquals(99000000000806L, imposition.getCreditor().getCreditorAccountId());
        assertEquals(AccountTypeEnum.MN, imposition.getCreditor().getAccountType());
        assertEquals(DisplayNameEnum.MINOR_CREDITOR, imposition.getCreditor().getDisplayName());
        assertNull(imposition.getCreditor().getMajorCreditorId());
        assertEquals(99000000000906L, imposition.getCreditor().getMinorCreditorPartyId());
        assertEquals("Metropolitan Traffic Unit", imposition.getCreditor().getName());

        assertEquals(5510L, imposition.getOffence().getId());
        assertEquals("OFF0006", imposition.getOffence().getCode());
        assertEquals("Test Offence 6", imposition.getOffence().getTitle());

        assertEquals(101L, imposition.getImposedBy().getCourtId());
        assertEquals((short) 102, imposition.getImposedBy().getCourtCode());
        assertEquals("Legacy Court", imposition.getImposedBy().getCourtName());
    }

    @Test
    void getImpositions_whenGatewayReturnsNullEntity_returnsNull() {
        when(gatewayService.postToGateway(
            eq(LegacyImpositionService.GET_IMPOSITIONS),
            eq(LegacyDefendantAccountImpositionsResponseCommon.class),
            eq(LegacyGetImpositionsRequest.builder().defendantAccountId("12345").build()),
            isNull()
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, null, null, null));

        assertNull(legacyImpositionService.getImpositions(12345L));
    }

    private LegacyDefendantAccountImpositionCommon legacyImposition() {
        return LegacyDefendantAccountImpositionCommon.builder()
            .dateAdded(LocalDate.parse("2026-05-06"))
            .dateImposed(LocalDate.parse("2026-05-05"))
            .imposition(LegacyResultReferenceCommon.builder()
                .resultId("ABDC")
                .resultTitle("Application made for Benefit Deductions")
                .build())
            .creditor(LegacyImpositionCreditorReferenceCommon.builder()
                .creditorAccountId(99000000000806L)
                .accountType(AccountTypeEnum.MN)
                .displayName(DisplayNameEnum.MINOR_CREDITOR)
                .minorCreditorPartyId(99000000000906L)
                .name("Metropolitan Traffic Unit")
                .build())
            .imposedAmount(new BigDecimal("600.00"))
            .paidAmount(new BigDecimal("60.00"))
            .balance(new BigDecimal("540.00"))
            .offence(LegacyOffenceReferenceCommon.builder()
                .id(5510L)
                .code("OFF0006")
                .title("Test Offence 6")
                .build())
            .imposedBy(LegacyCourtReferenceCommon.builder()
                .courtId(101L)
                .courtCode(102)
                .courtName("Legacy Court")
                .build())
            .impositionId(99000000003006L)
            .build();
    }
}
