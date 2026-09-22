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
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;
import uk.gov.hmcts.opal.dto.legacy.CompanyNameLegacy;
import uk.gov.hmcts.opal.dto.legacy.CreditorAccountTypeReferenceLegacy;
import uk.gov.hmcts.opal.dto.legacy.CreditorSummaryLegacy;
import uk.gov.hmcts.opal.dto.legacy.IndividualNameLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyCourtReferenceCommon;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountImpositionCommon;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountImpositionsResponseCommon;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetImpositionsRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyResultReferenceCommon;
import uk.gov.hmcts.opal.dto.legacy.OffenceReferenceLegacy;
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
        void whenGatewayReturnsImposition_mapsUpdatedLegacyCommonObjects_happyPath() {
            LegacyDefendantAccountImpositionsResponseCommon legacyResponse =
                LegacyDefendantAccountImpositionsResponseCommon.builder()
                    .version(BigInteger.valueOf(4L))
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
            DefendantAccountImpositionCommon imposition = response.getPayload().getImpositions().getFirst();

            assertAll(
                () -> verify(gatewayService).postToGateway(
                    eq(LegacyImpositionService.GET_IMPOSITIONS),
                    eq(LegacyDefendantAccountImpositionsResponseCommon.class),
                    eq(requestCaptor.getValue()),
                    isNull()
                ),
                () -> assertEquals("12345", requestCaptor.getValue().getDefendantAccountId()),
                () -> assertEquals(BigInteger.valueOf(4), response.getVersion()),
                () -> assertNotNull(response.getPayload()),
                () -> assertEquals(1, response.getPayload().getImpositions().size()),
                () -> assertEquals(LocalDate.parse("2026-05-06"), imposition.getDateAdded()),
                () -> assertEquals(LocalDate.parse("2026-05-05"), imposition.getDateImposed()),
                () -> assertEquals(new BigDecimal("-600.00"), imposition.getImposedAmount()),
                () -> assertEquals(new BigDecimal("60.00"), imposition.getPaidAmount()),
                () -> assertEquals(new BigDecimal("-540.00"), imposition.getBalance()),
                () -> assertEquals(99000000003006L, imposition.getImpositionId()),
                () -> assertEquals("ABDC", imposition.getImposition().getResultId()),
                () -> assertEquals("Application made for Benefit Deductions",
                    imposition.getImposition().getResultTitle()),
                () -> assertCreditor(imposition),
                () -> assertEquals(5510L, imposition.getOffence().getId()),
                () -> assertEquals("OFF0006", imposition.getOffence().getCode()),
                () -> assertEquals("Test Offence 6", imposition.getOffence().getTitle()),
                () -> assertEquals(101L, imposition.getImposedBy().getCourtId()),
                () -> assertEquals((short) 102, imposition.getImposedBy().getCourtCode()),
                () -> assertEquals("Legacy Court", imposition.getImposedBy().getCourtName())
            );
        }

        @Test
        void whenGatewayReturnsNullEntity_returnsNull_sadPath() {
            when(gatewayService.postToGateway(
                eq(LegacyImpositionService.GET_IMPOSITIONS),
                eq(LegacyDefendantAccountImpositionsResponseCommon.class),
                eq(LegacyGetImpositionsRequest.builder().defendantAccountId("12345").build()),
                isNull()
            )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, null, null, null));

            assertNull(legacyImpositionService.getImpositions(12345L));
        }

        @ParameterizedTest
        @MethodSource("creditorSummaries")
        void whenGatewayReturnsCreditor_mapsCreditorSummary_happyPath(
            CreditorSummaryLegacy creditor, AccountTypeEnum expectedAccountType,
            DisplayNameEnum expectedDisplayName, String expectedName) {

            mock_getImpositionsResponse(legacyImposition(creditor));

            DefendantAccountImpositionCommon imposition = legacyImpositionService.getImpositions(12345L)
                .getPayload().getImpositions().getFirst();

            assertAll(
                () -> assertEquals(expectedAccountType, imposition.getCreditor().getAccountType()),
                () -> assertEquals(expectedDisplayName, imposition.getCreditor().getDisplayName()),
                () -> assertEquals(expectedName, imposition.getCreditor().getName())
            );
        }

        @Test
        void whenGatewayReturnsNullNestedObjects_mapsNullNestedObjects_happyPath() {
            mock_getImpositionsResponse(LegacyDefendantAccountImpositionCommon.builder().build());

            DefendantAccountImpositionCommon imposition = legacyImpositionService.getImpositions(12345L)
                .getPayload().getImpositions().getFirst();

            assertAll(
                () -> assertNull(imposition.getImposition()),
                () -> assertNull(imposition.getCreditor()),
                () -> assertNull(imposition.getOffence()),
                () -> assertNull(imposition.getImposedBy())
            );
        }

        private void assertCreditor(DefendantAccountImpositionCommon imposition) {
            assertAll(
                () -> assertEquals(99000000000806L, imposition.getCreditor().getCreditorAccountId()),
                () -> assertEquals(AccountTypeEnum.MN, imposition.getCreditor().getAccountType()),
                () -> assertEquals(DisplayNameEnum.MINOR_CREDITOR, imposition.getCreditor().getDisplayName()),
                () -> assertNull(imposition.getCreditor().getMajorCreditorId()),
                () -> assertNull(imposition.getCreditor().getMinorCreditorPartyId()),
                () -> assertEquals("Metropolitan Traffic Unit", imposition.getCreditor().getName())
            );
        }

        private static Stream<Arguments> creditorSummaries() {
            return Stream.of(
                Arguments.of(
                    CreditorSummaryLegacy.builder()
                        .creditorAccountTypeReference(
                            CreditorAccountTypeReferenceLegacy.builder().creditorAccountType("MJ").build())
                        .majorCreditorName("  Major Creditor Name  ")
                        .build(),
                    AccountTypeEnum.MJ, DisplayNameEnum.MAJOR_CREDITOR, "Major Creditor Name"),
                Arguments.of(
                    CreditorSummaryLegacy.builder()
                        .creditorAccountTypeReference(
                            CreditorAccountTypeReferenceLegacy.builder().creditorAccountType("MN").build())
                        .individualName(IndividualNameLegacy.builder()
                            .forenames("Jane Mary")
                            .surname("Doe")
                            .build())
                        .build(),
                    AccountTypeEnum.MN, DisplayNameEnum.MINOR_CREDITOR, "Jane Mary Doe"),
                Arguments.of(
                    CreditorSummaryLegacy.builder()
                        .individualName(IndividualNameLegacy.builder().surname("Doe").build())
                        .build(),
                    null, null, "Doe"),
                Arguments.of(CreditorSummaryLegacy.builder().majorCreditorName(" ").build(),
                    null, null, null)
            );
        }
    }

    private LegacyDefendantAccountImpositionCommon legacyImposition() {
        return legacyImposition(minorCompanyCreditor());
    }

    private LegacyDefendantAccountImpositionCommon legacyImposition(CreditorSummaryLegacy creditor) {
        return LegacyDefendantAccountImpositionCommon.builder()
            .dateAdded(LocalDate.parse("2026-05-06"))
            .dateImposed(LocalDate.parse("2026-05-05"))
            .imposition(LegacyResultReferenceCommon.builder()
                .resultId("ABDC")
                .resultTitle("Application made for Benefit Deductions")
                .build())
            .creditor(creditor)
            .imposedAmount(new BigDecimal("-600.00"))
            .paidAmount(new BigDecimal("60.00"))
            .balance(new BigDecimal("-540.00"))
            .offence(OffenceReferenceLegacy.builder()
                .offenceId(5510L)
                .cjsCode("OFF0006")
                .offenceTitle("Test Offence 6")
                .build())
            .imposedBy(LegacyCourtReferenceCommon.builder()
                .courtId(101L)
                .courtCode(102)
                .courtName("Legacy Court")
                .build())
            .impositionId(99000000003006L)
            .build();
    }

    private CreditorSummaryLegacy minorCompanyCreditor() {
        return CreditorSummaryLegacy.builder()
            .creditorAccountTypeReference(
                CreditorAccountTypeReferenceLegacy.builder().creditorAccountType("MN").build())
            .creditorAccountId(99000000000806L)
            .minorCreditorOrganisationFlag(true)
            .companyName(CompanyNameLegacy.builder()
                .organisationName("Metropolitan Traffic Unit")
                .build())
            .build();
    }

    private void mock_getImpositionsResponse(LegacyDefendantAccountImpositionCommon imposition) {
        LegacyDefendantAccountImpositionsResponseCommon legacyResponse =
            LegacyDefendantAccountImpositionsResponseCommon.builder()
                .version(BigInteger.valueOf(4L))
                .impositions(List.of(imposition))
                .build();

        when(gatewayService.postToGateway(
            eq(LegacyImpositionService.GET_IMPOSITIONS),
            eq(LegacyDefendantAccountImpositionsResponseCommon.class),
            eq(LegacyGetImpositionsRequest.builder().defendantAccountId("12345").build()),
            isNull()
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse, null, null));
    }
}
