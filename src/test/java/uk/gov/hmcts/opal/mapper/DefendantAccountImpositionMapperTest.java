package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountType;
import uk.gov.hmcts.opal.entity.projection.DefendantAccountImpositionData;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionCommon;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionsResponseCommon;
import uk.gov.hmcts.opal.generated.model.ImpositionCreditorReferenceCommon;

class DefendantAccountImpositionMapperTest {

    private final DefendantAccountImpositionMapper mapper = Mappers.getMapper(DefendantAccountImpositionMapper.class);

    @Test
    void toResponse_shouldWrapMappedImpositions() {
        DefendantAccountImpositionData data = impositionData(
            DefendantAccountType.FINES,
            CreditorAccountType.MJ,
            551003L,
            "Graph Major Creditor",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            551001L
        );

        DefendantAccountImpositionsResponseCommon response = mapper.toResponse(List.of(data));
        DefendantAccountImpositionCommon mapped = response.getImpositions().getFirst();

        assertNotNull(response);
        assertEquals(1, response.getImpositions().size());
        assertEquals(LocalDate.of(2026, 4, 17), mapped.getDateAdded());
        assertEquals(551005L, mapped.getImpositionId());
        assertEquals(new BigDecimal("250.00"), mapped.getImposedAmount());
        assertEquals(new BigDecimal("25.00"), mapped.getPaidAmount());
        assertEquals(new BigDecimal("225.00"), mapped.getBalance());
        assertEquals(LocalDate.of(2026, 4, 16), mapped.getDateImposed());
    }

    @Test
    void toImposition_shouldMapMajorCreditorFallbackOffenceAndImposedBy() {
        DefendantAccountImpositionData data = impositionData(
            DefendantAccountType.FINES,
            CreditorAccountType.MJ,
            551003L,
            "Graph Major Creditor",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            551001L
        );

        DefendantAccountImpositionCommon mapped = mapper.toImposition(data);

        assertEquals("IGR001", mapped.getImposition().getResultId());
        assertEquals("Imposition Graph Result", mapped.getImposition().getResultTitle());
        assertEquals(551004L, mapped.getCreditor().getCreditorAccountId());
        assertEquals(ImpositionCreditorReferenceCommon.AccountTypeEnum.MJ, mapped.getCreditor().getAccountType());
        assertEquals(
            ImpositionCreditorReferenceCommon.DisplayNameEnum.MAJOR_CREDITOR,
            mapped.getCreditor().getDisplayName()
        );
        assertEquals(551003L, mapped.getCreditor().getMajorCreditorId());
        assertNull(mapped.getCreditor().getMinorCreditorPartyId());
        assertEquals("Graph Major Creditor", mapped.getCreditor().getName());
        assertEquals(5510L, mapped.getOffence().getId());
        assertEquals("IG5510", mapped.getOffence().getCode());
        assertEquals("Imposition Graph Offence", mapped.getOffence().getTitle());
        assertEquals(551001L, mapped.getImposedBy().getCourtId());
        assertEquals((short) 101, mapped.getImposedBy().getCourtCode());
        assertEquals("Graph Test Court", mapped.getImposedBy().getCourtName());
    }

    @Test
    void toImposition_shouldPreferLocalOffenceAndMapIndividualMinorCreditorName() {
        DefendantAccountImpositionData data = impositionData(
            DefendantAccountType.FINES,
            CreditorAccountType.MN,
            null,
            null,
            551006L,
            false,
            null,
            "Ms",
            "Creditor",
            "Minor",
            "LOCAL1",
            "Local offence title",
            null
        );

        DefendantAccountImpositionCommon mapped = mapper.toImposition(data);

        assertEquals(ImpositionCreditorReferenceCommon.AccountTypeEnum.MN, mapped.getCreditor().getAccountType());
        assertEquals(
            ImpositionCreditorReferenceCommon.DisplayNameEnum.MINOR_CREDITOR,
            mapped.getCreditor().getDisplayName()
        );
        assertNull(mapped.getCreditor().getMajorCreditorId());
        assertEquals(551006L, mapped.getCreditor().getMinorCreditorPartyId());
        assertEquals("Ms Creditor Minor", mapped.getCreditor().getName());
        assertEquals("LOCAL1", mapped.getOffence().getCode());
        assertEquals("Local offence title", mapped.getOffence().getTitle());
        assertNull(mapped.getImposedBy());
    }

    @Test
    void toImposition_shouldMapOrganisationMinorCreditorName() {
        DefendantAccountImpositionData data = impositionData(
            DefendantAccountType.FINES,
            CreditorAccountType.MN,
            null,
            null,
            551006L,
            true,
            "Minor Org Ltd",
            "Ms",
            "Creditor",
            "Minor",
            null,
            null,
            null
        );

        DefendantAccountImpositionCommon mapped = mapper.toImposition(data);

        assertEquals("Minor Org Ltd", mapped.getCreditor().getName());
    }

    @Test
    void toImposition_shouldFallbackToOrganisationNameWhenMinorCreditorIndividualNameIsBlank() {
        DefendantAccountImpositionData data = impositionData(
            DefendantAccountType.FINES,
            CreditorAccountType.MN,
            null,
            null,
            551006L,
            false,
            "Minor Org Ltd",
            " ",
            null,
            "\t",
            null,
            null,
            null
        );

        DefendantAccountImpositionCommon mapped = mapper.toImposition(data);

        assertEquals("Minor Org Ltd", mapped.getCreditor().getName());
    }

    @Test
    void toImposition_shouldMapCentralFundCreditorWithConfiguredName() {
        DefendantAccountImpositionData data = impositionData(
            DefendantAccountType.FINES,
            CreditorAccountType.CF,
            null,
            "  Configured Central Fund  ",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );

        DefendantAccountImpositionCommon mapped = mapper.toImposition(data);

        assertEquals(ImpositionCreditorReferenceCommon.AccountTypeEnum.CF, mapped.getCreditor().getAccountType());
        assertEquals(
            ImpositionCreditorReferenceCommon.DisplayNameEnum.CENTRAL_FUND,
            mapped.getCreditor().getDisplayName()
        );
        assertEquals("Configured Central Fund", mapped.getCreditor().getName());
    }

    @Test
    void toImposition_shouldUseCentralFundLabelWhenConfiguredNameIsBlank() {
        DefendantAccountImpositionData data = impositionData(
            DefendantAccountType.FINES,
            CreditorAccountType.CF,
            null,
            " ",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );

        DefendantAccountImpositionCommon mapped = mapper.toImposition(data);

        assertEquals("Central Fund", mapped.getCreditor().getName());
    }

    @Test
    void toImposition_shouldNotMapImposedByForNonFinesAccount() {
        DefendantAccountImpositionData data = impositionData(
            DefendantAccountType.FIXED_PENALTY,
            CreditorAccountType.MJ,
            551003L,
            "Graph Major Creditor",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            551001L
        );

        DefendantAccountImpositionCommon mapped = mapper.toImposition(data);

        assertNull(mapped.getImposedBy());
    }

    @Test
    void toImposition_shouldCalculateBalanceFromZeroWhenPaidAmountIsNull() {
        DefendantAccountImpositionData data = new DefendantAccountImpositionData(
            551002L,
            7L,
            DefendantAccountType.FINES,
            551005L,
            LocalDateTime.of(2026, 4, 17, 10, 0),
            "IGR001",
            "Imposition Graph Result",
            551004L,
            CreditorAccountType.MJ,
            551003L,
            "Graph Major Creditor",
            null,
            null,
            null,
            null,
            null,
            null,
            LocalDateTime.of(2026, 4, 16, 9, 30),
            new BigDecimal("250.00"),
            null,
            5510L,
            null,
            null,
            "IG5510",
            "Imposition Graph Offence",
            551001L,
            null,
            "Graph Test Court"
        );

        DefendantAccountImpositionCommon mapped = mapper.toImposition(data);

        assertEquals(new BigDecimal("250.00"), mapped.getBalance());
        assertNull(mapped.getImposedBy().getCourtCode());
    }

    @Test
    void toImposition_shouldMapNullsWhenOptionalSourceValuesAreMissing() {
        DefendantAccountImpositionData data = new DefendantAccountImpositionData(
            551002L,
            7L,
            DefendantAccountType.FINES,
            551005L,
            null,
            null,
            null,
            551004L,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            new BigDecimal("25.00"),
            5510L,
            " ",
            null,
            "\t",
            null,
            null,
            null,
            null
        );

        DefendantAccountImpositionCommon mapped = mapper.toImposition(data);

        assertNull(mapped.getDateAdded());
        assertNull(mapped.getDateImposed());
        assertNull(mapped.getCreditor().getAccountType());
        assertNull(mapped.getCreditor().getDisplayName());
        assertNull(mapped.getCreditor().getName());
        assertNull(mapped.getBalance());
        assertNull(mapped.getOffence().getCode());
        assertNull(mapped.getOffence().getTitle());
        assertNull(mapped.getImposedBy());
    }

    @Test
    void toImposition_shouldReturnNullForNullSource() {
        assertNull(mapper.toImposition(null));
    }

    @Test
    void toResponse_shouldReturnEmptyImpositionListForNullSource() {
        DefendantAccountImpositionsResponseCommon response = mapper.toResponse(null);

        assertNotNull(response);
        assertTrue(response.getImpositions().isEmpty());
    }

    @Test
    void derivedReferenceMethods_shouldReturnNullForNullSource() {
        assertNull(mapper.toResultReference(null));
        assertNull(mapper.toCreditorReference(null));
        assertNull(mapper.toBalance(null));
        assertNull(mapper.toOffenceReference(null));
        assertNull(mapper.toImposedByReference(null));
    }

    private DefendantAccountImpositionData impositionData(
        DefendantAccountType defendantAccountType,
        CreditorAccountType creditorAccountType,
        Long majorCreditorId,
        String majorCreditorName,
        Long minorCreditorPartyId,
        Boolean minorCreditorOrganisation,
        String minorCreditorOrganisationName,
        String minorCreditorTitle,
        String minorCreditorForenames,
        String minorCreditorSurname,
        String impositionOffenceCode,
        String impositionOffenceTitle,
        Long imposingCourtId
    ) {
        return new DefendantAccountImpositionData(
            551002L,
            7L,
            defendantAccountType,
            551005L,
            LocalDateTime.of(2026, 4, 17, 10, 0),
            "IGR001",
            "Imposition Graph Result",
            551004L,
            creditorAccountType,
            majorCreditorId,
            majorCreditorName,
            minorCreditorPartyId,
            minorCreditorOrganisation,
            minorCreditorOrganisationName,
            minorCreditorTitle,
            minorCreditorForenames,
            minorCreditorSurname,
            LocalDateTime.of(2026, 4, 16, 9, 30),
            new BigDecimal("250.00"),
            new BigDecimal("25.00"),
            5510L,
            impositionOffenceCode,
            impositionOffenceTitle,
            "IG5510",
            "Imposition Graph Offence",
            imposingCourtId,
            imposingCourtId == null ? null : (short) 101,
            imposingCourtId == null ? null : "Graph Test Court"
        );
    }
}
