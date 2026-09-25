package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Creditor;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.CreditorAccountType;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Imposition;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Offence;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.PostedDetails;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Result;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionCommon;
import uk.gov.hmcts.opal.generated.model.ImpositionCreditorReferenceCommon.AccountTypeEnum;
import uk.gov.hmcts.opal.generated.model.ImpositionCreditorReferenceCommon.DisplayNameEnum;
import uk.gov.hmcts.opal.mapper.AbstractMapperTest;

class DefendantAccountImpositionsLegacyResponseMapperTest extends AbstractMapperTest {

    private static final BigInteger VERSION =
        new BigInteger("18338687664539878704807506660830801130000030349");

    @Autowired
    private DefendantAccountImpositionsLegacyResponseMapper mapper;

    @Nested
    class ToOpal {

        @Test
        void whenLegacyResponseContainsImposition_mapsResponse_happyPath() {
            GetDefendantAccountImpositionsResponse response = mapper.toOpal(legacyResponse());
            DefendantAccountImpositionCommon imposition = response.getPayload().getImpositions().getFirst();

            assertAll(
                () -> assertEquals(VERSION, response.getVersion()),
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
        void whenLegacyResponseIsNull_returnsNull_sadPath() {
            assertNull(mapper.toOpal((GetDefendantAccountImpositionsLegacyResponse) null));
        }

        @Test
        void whenLegacyImpositionsAreNull_mapsNullPayload_happyPath() {
            GetDefendantAccountImpositionsResponse response = mapper.toOpal(
                GetDefendantAccountImpositionsLegacyResponse.builder().version(BigInteger.ONE).build());

            assertAll(
                () -> assertEquals(BigInteger.ONE, response.getVersion()),
                () -> assertNull(response.getPayload())
            );
        }

        @Test
        void whenLegacyImpositionsAreEmpty_mapsEmptyList_happyPath() {
            GetDefendantAccountImpositionsResponse response = mapper.toOpal(
                GetDefendantAccountImpositionsLegacyResponse.builder()
                    .version(BigInteger.ONE)
                    .impositions(List.of())
                    .build());

            assertAll(
                () -> assertNotNull(response.getPayload()),
                () -> assertTrue(response.getPayload().getImpositions().isEmpty())
            );
        }

        @Test
        void whenLegacyImpositionIsEmpty_mapsNullFields_happyPath() {
            GetDefendantAccountImpositionsLegacyResponse legacyResponse =
                GetDefendantAccountImpositionsLegacyResponse.builder()
                    .version(BigInteger.ONE)
                    .impositions(List.of(Imposition.builder().build()))
                    .build();

            DefendantAccountImpositionCommon imposition = mapper.toOpal(legacyResponse)
                .getPayload().getImpositions().getFirst();

            assertAll(
                () -> assertNull(imposition.getDateAdded()),
                () -> assertNull(imposition.getImposition()),
                () -> assertNull(imposition.getCreditor()),
                () -> assertNull(imposition.getOffence()),
                () -> assertNull(imposition.getImposedBy())
            );
        }

        @Test
        void whenCreditorAccountTypeIsUnknown_throwsIllegalArgumentException_sadPath() {
            GetDefendantAccountImpositionsLegacyResponse legacyResponse =
                GetDefendantAccountImpositionsLegacyResponse.builder()
                    .impositions(List.of(Imposition.builder()
                        .creditor(Creditor.builder()
                            .creditorAccountType(CreditorAccountType.builder()
                                .creditorAccountType("UNKNOWN")
                                .build())
                            .build())
                        .build()))
                    .build();

            assertThrows(IllegalArgumentException.class, () -> mapper.toOpal(legacyResponse));
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
            .version(VERSION)
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
}
