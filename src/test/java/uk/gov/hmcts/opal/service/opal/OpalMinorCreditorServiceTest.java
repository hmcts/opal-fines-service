package uk.gov.hmcts.opal.service.opal;

import java.math.BigInteger;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.EntityNotFoundException;
import uk.gov.hmcts.opal.dto.CreditorAccountDto;
import uk.gov.hmcts.opal.dto.DefendantDto;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.CreditorAccountTypeReference;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountHeaderEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;
import uk.gov.hmcts.opal.repository.MinorCreditorAccountHeaderRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorRepository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpalMinorCreditorServiceTest {

    @Mock
    private MinorCreditorRepository minorCreditorRepository;

    @Mock
    private MinorCreditorAccountHeaderRepository minorCreditorAccountHeaderRepository;


    @InjectMocks
    private OpalMinorCreditorService service;

    @Captor
    private ArgumentCaptor<Specification<MinorCreditorEntity>> specCaptor;

    private MinorCreditorEntity entityOrgFalseWithDefendant;
    private MinorCreditorEntity entityOrgTrueNoDefendant;

    @BeforeEach
    void setUp() {
        entityOrgFalseWithDefendant = MinorCreditorEntity.builder()
            .creditorId(104L)
            .accountNumber("12345678A")
            .businessUnitId((short) 10)
            .businessUnitName("Derbyshire")
            .partyId(9000L)
            .organisation(false)
            .addressLine1("Acme House")
            .postCode("MA4 1AL")
            .forenames(null)
            .surname(null)
            .defendantAccountId(0L)
            .defendantOrganisationName(null)
            .defendantFornames("Anna")
            .defendantSurname("Graham")
            .creditorAccountBalance(150)
            .build();

        entityOrgTrueNoDefendant = MinorCreditorEntity.builder()
            .creditorId(105L)
            .accountNumber("12345678")
            .businessUnitId((short) 10)
            .businessUnitName("Derbyshire")
            .partyId(9000L)
            .organisation(true)
            .addressLine1("Acme House")
            .postCode("MA4 1AL")
            .forenames("")
            .surname("")
            .defendantAccountId(0L)
            .defendantOrganisationName(null)
            .defendantFornames(null)
            .defendantSurname(null)
            .creditorAccountBalance(0)
            .build();
    }

    @Test
    void searchMinorCreditors_twoResults_happyPath_mapsAllFields() {
        MinorCreditorSearch criteria = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .accountNumber("12345678A")
            .activeAccountsOnly(false)
            .build();

        when(minorCreditorRepository.findAll(Mockito.<Specification<MinorCreditorEntity>>any()))
            .thenReturn(List.of(entityOrgFalseWithDefendant, entityOrgTrueNoDefendant));

        PostMinorCreditorAccountsSearchResponse response = service.searchMinorCreditors(criteria);

        verify(minorCreditorRepository, times(1)).findAll(specCaptor.capture());
        Specification<MinorCreditorEntity> passedSpec = specCaptor.getValue();
        assertNotNull(passedSpec);

        assertNotNull(response);
        assertEquals(2, response.getCount());
        assertEquals(2, response.getCreditorAccounts().size());

        CreditorAccountDto a0 = response.getCreditorAccounts().getFirst();
        assertEquals("104", a0.getCreditorAccountId());
        assertEquals("12345678A", a0.getAccountNumber());
        assertFalse(a0.isOrganisation());
        assertNull(a0.getOrganisationName());
        assertNull(a0.getFirstnames());
        assertNull(a0.getSurname());
        assertEquals("Acme House", a0.getAddressLine1());
        assertEquals("MA4 1AL", a0.getPostcode());
        assertEquals("Derbyshire", a0.getBusinessUnitName());
        assertEquals("10", a0.getBusinessUnitId());
        assertEquals(new java.math.BigDecimal("150"), a0.getAccountBalance());

        DefendantDto d0 = a0.getDefendant();
        assertNotNull(d0);
        assertEquals("0", d0.getDefendantAccountId());
        assertFalse(d0.isOrganisation());
        assertNull(d0.getOrganisationName());
        assertEquals("Anna", d0.getFirstnames());
        assertEquals("Graham", d0.getSurname());

        CreditorAccountDto a1 = response.getCreditorAccounts().get(1);
        assertEquals("105", a1.getCreditorAccountId());
        assertEquals("12345678", a1.getAccountNumber());
        assertTrue(a1.isOrganisation());
        assertNull(a1.getOrganisationName());
        assertEquals("", a1.getFirstnames());
        assertEquals("", a1.getSurname());
        assertEquals("Acme House", a1.getAddressLine1());
        assertEquals("MA4 1AL", a1.getPostcode());
        assertEquals("Derbyshire", a1.getBusinessUnitName());
        assertEquals("10", a1.getBusinessUnitId());
        assertEquals(BigDecimal.ZERO, a1.getAccountBalance());

        DefendantDto d1 = a1.getDefendant();
        assertNotNull(d1);
        assertEquals("0", d1.getDefendantAccountId());
        assertTrue(d1.isOrganisation());
        assertNull(d1.getOrganisationName());
        assertNull(d1.getFirstnames());
        assertNull(d1.getSurname());
    }

    @Test
    void searchMinorCreditors_emptyResult_returnsCountZeroAndEmptyList() {
        MinorCreditorSearch criteria = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .build();

        when(minorCreditorRepository.findAll(Mockito.<Specification<MinorCreditorEntity>>any()))
            .thenReturn(Collections.emptyList());

        PostMinorCreditorAccountsSearchResponse response = service.searchMinorCreditors(criteria);

        verify(minorCreditorRepository, times(1))
            .findAll(Mockito.<Specification<MinorCreditorEntity>>any());
        assertEquals(0, response.getCount());
        assertNull(response.getCreditorAccounts());
    }

    @Test
    void searchMinorCreditors_repositoryThrows_exceptionPropagates() {
        MinorCreditorSearch criteria = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .accountNumber("boom")
            .build();

        RuntimeException failure = new RuntimeException("DB failure");
        when(minorCreditorRepository.findAll(Mockito.<Specification<MinorCreditorEntity>>any()))
            .thenThrow(failure);

        RuntimeException ex = assertThrows(RuntimeException.class,
                                           () -> service.searchMinorCreditors(criteria));
        assertSame(failure, ex);
    }

    @Test
    void searchMinorCreditors_minimalCriteria_buildsSpecAndMapsSingle() {
        MinorCreditorSearch criteria = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .build();

        when(minorCreditorRepository.findAll(Mockito.<Specification<MinorCreditorEntity>>any()))
            .thenReturn(List.of(entityOrgFalseWithDefendant));

        PostMinorCreditorAccountsSearchResponse response = service.searchMinorCreditors(criteria);

        verify(minorCreditorRepository).findAll(specCaptor.capture());
        Specification<MinorCreditorEntity> spec = specCaptor.getValue();
        assertNotNull(spec);

        assertEquals(1, response.getCount());
        assertEquals("104", response.getCreditorAccounts().getFirst().getCreditorAccountId());
    }

    @Test
    void getHeaderSummary_foundOrganisation_mapsCorrectly_andHasAssociatedDefendantFalse() {
        // Arrange
        long id = 99000000000800L;

        MinorCreditorAccountHeaderEntity entity = MinorCreditorAccountHeaderEntity.builder()
            .creditorAccountId(id)
            .creditorAccountNumber("87654321")
            .creditorAccountType("MN")
            .versionNumber(5L)
            .partyId(99000000000900L)
            .title(null)
            .forenames(null)
            .surname(null)
            .organisation(true)
            .organisationName("Minor Creditor Test Ltd")
            .businessUnitId((short) 77)
            .businessUnitName("Camberwell Green")
            .welshLanguage(false)
            .awarded(BigDecimal.ZERO)
            .paidOut(BigDecimal.ZERO)
            .awaitingPayment(BigDecimal.ZERO)
            .outstanding(BigDecimal.ZERO)
            .build();

        when(minorCreditorAccountHeaderRepository.findById(id)).thenReturn(Optional.of(entity));

        // Act
        GetMinorCreditorAccountHeaderSummaryResponse res = service.getHeaderSummary(id);

        // Assert
        assertNotNull(res);
        assertEquals(String.valueOf(id), res.getCreditorAccountId());
        assertEquals("87654321", res.getAccountNumber());
        assertCreditorAccountType(res.getCreditorAccountType(), "MN", "Minor Creditor");
        assertEquals(BigInteger.valueOf(5L), res.getVersion());

        BusinessUnitSummary bu = res.getBusinessUnitSummary();
        assertNotNull(bu);
        assertEquals("77", bu.getBusinessUnitId());
        assertEquals("Camberwell Green", bu.getBusinessUnitName());
        assertEquals("N", bu.getWelshSpeaking()); // welshLanguage=false -> "N"

        PartyDetails party = res.getPartyDetails();
        assertNotNull(party);
        assertEquals("99000000000900", party.getPartyId());
        assertTrue(party.getOrganisationFlag());

        OrganisationDetails org = party.getOrganisationDetails();
        assertNotNull(org);
        assertEquals("Minor Creditor Test Ltd", org.getOrganisationName());
        assertNull(party.getIndividualDetails());

        assertEquals(BigDecimal.ZERO, res.getAwardedAmount());
        assertEquals(BigDecimal.ZERO, res.getPaidOutAmount());
        assertEquals(BigDecimal.ZERO, res.getAwaitingPayoutAmount());
        assertEquals(BigDecimal.ZERO, res.getOutstandingAmount());

        // hasAssociatedDefendant() is based on awarded/outstanding > 0
        assertFalse(res.getHasAssociatedDefendant());

        verify(minorCreditorAccountHeaderRepository, times(1)).findById(id);
    }

    @Test
    void getHeaderSummary_foundIndividual_mapsCorrectly_andHasAssociatedDefendantTrue() {
        // Arrange
        long id = 99000000000801L;

        MinorCreditorAccountHeaderEntity entity = MinorCreditorAccountHeaderEntity.builder()
            .creditorAccountId(id)
            .creditorAccountNumber("87654322")
            .creditorAccountType("MN")
            .versionNumber(null) // verify null -> null
            .partyId(99000000000901L)
            .title("Mr")
            .forenames("John")
            .surname("Smith")
            .organisation(false)
            .organisationName(null)
            .businessUnitId((short) 77)
            .businessUnitName("Camberwell Green")
            .welshLanguage(true)
            .awarded(new BigDecimal("10.00")) // makes hasAssociatedDefendant true
            .paidOut(BigDecimal.ZERO)
            .awaitingPayment(BigDecimal.ZERO)
            .outstanding(BigDecimal.ZERO)
            .build();

        when(minorCreditorAccountHeaderRepository.findById(id)).thenReturn(Optional.of(entity));

        // Act
        GetMinorCreditorAccountHeaderSummaryResponse res = service.getHeaderSummary(id);

        // Assert
        assertNotNull(res);
        assertEquals(String.valueOf(id), res.getCreditorAccountId());
        assertEquals("87654322", res.getAccountNumber());
        assertCreditorAccountType(res.getCreditorAccountType(), "MN", "Minor Creditor");
        assertNull(res.getVersion());

        BusinessUnitSummary bu = res.getBusinessUnitSummary();
        assertNotNull(bu);
        assertEquals("77", bu.getBusinessUnitId());
        assertEquals("Camberwell Green", bu.getBusinessUnitName());
        assertEquals("Y", bu.getWelshSpeaking());

        PartyDetails party = res.getPartyDetails();
        assertNotNull(party);
        assertEquals("99000000000901", party.getPartyId());
        assertFalse(party.getOrganisationFlag());

        IndividualDetails ind = party.getIndividualDetails();
        assertNotNull(ind);
        assertEquals("Mr", ind.getTitle());
        assertEquals("John", ind.getForenames());
        assertEquals("Smith", ind.getSurname());
        assertNull(party.getOrganisationDetails());

        assertEquals(new BigDecimal("10.00"), res.getAwardedAmount());
        assertTrue(res.getHasAssociatedDefendant());

        verify(minorCreditorAccountHeaderRepository, times(1)).findById(id);
    }

    @Test
    void getHeaderSummary_notFound_throwsEntityNotFoundException_withExpectedMessage() {
        // Arrange
        long missingId = 123456789L;
        when(minorCreditorAccountHeaderRepository.findById(missingId)).thenReturn(Optional.empty());

        // Act
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
            () -> service.getHeaderSummary(missingId));

        // Assert
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("Minor creditor account not found: " + missingId));

        verify(minorCreditorAccountHeaderRepository, times(1)).findById(missingId);
    }

    @Test
    void getHeaderSummary_hasAssociatedDefendant_trueWhenOutstandingPositive_evenIfAwardedNull() {
        // Arrange
        long id = 222L;

        MinorCreditorAccountHeaderEntity entity = MinorCreditorAccountHeaderEntity.builder()
            .creditorAccountId(id)
            .creditorAccountNumber("X")
            .creditorAccountType("MN")
            .versionNumber(null)
            .partyId(333L)
            .organisation(true)
            .organisationName("Org")
            .businessUnitId((short) 77)
            .businessUnitName("BU")
            .welshLanguage(false)
            .awarded(null)
            .paidOut(BigDecimal.ZERO)
            .awaitingPayment(BigDecimal.ZERO)
            .outstanding(new BigDecimal("0.01"))
            .build();

        when(minorCreditorAccountHeaderRepository.findById(id)).thenReturn(Optional.of(entity));

        // Act
        GetMinorCreditorAccountHeaderSummaryResponse res = service.getHeaderSummary(id);

        // Assert
        assertTrue(res.getHasAssociatedDefendant());
    }

    @Test
    void getHeaderSummary_orgAccount_mapsOrganisationDetails_andSetsHasAssociatedDefendantFalse_whenZeroes() {
        // Arrange
        long id = 99000000000800L;

        MinorCreditorAccountHeaderEntity entity = MinorCreditorAccountHeaderEntity.builder()
            .creditorAccountId(id)
            .creditorAccountNumber("87654321")
            .creditorAccountType("MN")
            .versionNumber(null) // covers version null branch
            .partyId(99000000000900L)
            .organisation(true)
            .organisationName("Minor Creditor Test Ltd")
            .title(null)
            .forenames(null)
            .surname(null)
            .businessUnitId((short) 77)
            .businessUnitName("Camberwell Green")
            .welshLanguage(false) // -> "N"
            .awarded(BigDecimal.ZERO)
            .paidOut(BigDecimal.ZERO)
            .awaitingPayment(BigDecimal.ZERO)
            .outstanding(BigDecimal.ZERO)
            .build();

        when(minorCreditorAccountHeaderRepository.findById(id)).thenReturn(Optional.of(entity));

        // Act
        GetMinorCreditorAccountHeaderSummaryResponse resp = service.getHeaderSummary(id);

        // Assert (top level)
        assertNotNull(resp);
        assertEquals(String.valueOf(id), resp.getCreditorAccountId());
        assertEquals("87654321", resp.getAccountNumber());
        assertCreditorAccountType(resp.getCreditorAccountType(), "MN", "Minor Creditor");
        assertNull(resp.getVersion());

        // business unit mapping
        assertNotNull(resp.getBusinessUnitSummary());
        assertEquals("77", resp.getBusinessUnitSummary().getBusinessUnitId());
        assertEquals("Camberwell Green", resp.getBusinessUnitSummary().getBusinessUnitName());
        assertEquals("N", resp.getBusinessUnitSummary().getWelshSpeaking());

        // party mapping - organisation branch
        assertNotNull(resp.getPartyDetails());
        assertEquals("99000000000900", resp.getPartyDetails().getPartyId());
        assertEquals(Boolean.TRUE, resp.getPartyDetails().getOrganisationFlag());

        assertNotNull(resp.getPartyDetails().getOrganisationDetails());
        assertEquals("Minor Creditor Test Ltd", resp.getPartyDetails().getOrganisationDetails().getOrganisationName());
        assertNull(resp.getPartyDetails().getIndividualDetails());

        // amounts + defendant flag
        assertEquals(BigDecimal.ZERO, resp.getAwardedAmount());
        assertEquals(BigDecimal.ZERO, resp.getPaidOutAmount());
        assertEquals(BigDecimal.ZERO, resp.getAwaitingPayoutAmount());
        assertEquals(BigDecimal.ZERO, resp.getOutstandingAmount());
        assertEquals(Boolean.FALSE, resp.getHasAssociatedDefendant());

        verify(minorCreditorAccountHeaderRepository).findById(id);
    }

    @Test
    void getHeaderSummary_individualAccount_mapsIndividualDetails_andWelshSpeakingY() {
        // Arrange
        long id = 123L;

        MinorCreditorAccountHeaderEntity entity = MinorCreditorAccountHeaderEntity.builder()
            .creditorAccountId(id)
            .creditorAccountNumber("ACC123")
            .creditorAccountType("MN")
            .versionNumber(5L) // covers version non-null branch
            .partyId(456L)
            .organisation(false)
            .organisationName(null)
            .title("Mr")
            .forenames("John")
            .surname("Smith")
            .businessUnitId((short) 10)
            .businessUnitName("Derbyshire")
            .welshLanguage(true) // -> "Y"
            .awarded(BigDecimal.ZERO)
            .paidOut(BigDecimal.ZERO)
            .awaitingPayment(BigDecimal.ZERO)
            .outstanding(BigDecimal.ZERO)
            .build();

        when(minorCreditorAccountHeaderRepository.findById(id)).thenReturn(Optional.of(entity));

        // Act
        GetMinorCreditorAccountHeaderSummaryResponse resp = service.getHeaderSummary(id);

        // Assert
        assertNotNull(resp);
        assertEquals("123", resp.getCreditorAccountId());
        assertEquals("ACC123", resp.getAccountNumber());
        assertCreditorAccountType(resp.getCreditorAccountType(), "MN", "Minor Creditor");
        assertNotNull(resp.getVersion());
        assertEquals(new java.math.BigInteger("5"), resp.getVersion());

        assertNotNull(resp.getBusinessUnitSummary());
        assertEquals("10", resp.getBusinessUnitSummary().getBusinessUnitId());
        assertEquals("Derbyshire", resp.getBusinessUnitSummary().getBusinessUnitName());
        assertEquals("Y", resp.getBusinessUnitSummary().getWelshSpeaking());

        assertNotNull(resp.getPartyDetails());
        assertEquals("456", resp.getPartyDetails().getPartyId());
        assertEquals(Boolean.FALSE, resp.getPartyDetails().getOrganisationFlag());

        assertNull(resp.getPartyDetails().getOrganisationDetails());
        assertNotNull(resp.getPartyDetails().getIndividualDetails());
        assertEquals("Mr", resp.getPartyDetails().getIndividualDetails().getTitle());
        assertEquals("John", resp.getPartyDetails().getIndividualDetails().getForenames());
        assertEquals("Smith", resp.getPartyDetails().getIndividualDetails().getSurname());
    }

    @Test
    void getHeaderSummary_hasAssociatedDefendant_true_whenAwardedPositive() {
        // Arrange
        long id = 200L;

        MinorCreditorAccountHeaderEntity entity = MinorCreditorAccountHeaderEntity.builder()
            .creditorAccountId(id)
            .creditorAccountNumber("ACC200")
            .creditorAccountType("MN")
            .versionNumber(null)
            .partyId(300L)
            .organisation(true)
            .organisationName("Org")
            .businessUnitId((short) 1)
            .businessUnitName("BU")
            .welshLanguage(false)
            .awarded(new BigDecimal("0.01")) // awarded > 0 => true branch
            .paidOut(BigDecimal.ZERO)
            .awaitingPayment(BigDecimal.ZERO)
            .outstanding(BigDecimal.ZERO)
            .build();

        when(minorCreditorAccountHeaderRepository.findById(id)).thenReturn(Optional.of(entity));

        // Act
        GetMinorCreditorAccountHeaderSummaryResponse resp = service.getHeaderSummary(id);

        // Assert
        assertEquals(Boolean.TRUE, resp.getHasAssociatedDefendant());
    }

    @Test
    void getHeaderSummary_hasAssociatedDefendant_true_whenOutstandingPositive_evenIfAwardedNull() {
        // Arrange
        long id = 201L;

        MinorCreditorAccountHeaderEntity entity = MinorCreditorAccountHeaderEntity.builder()
            .creditorAccountId(id)
            .creditorAccountNumber("ACC201")
            .creditorAccountType("MN")
            .versionNumber(null)
            .partyId(301L)
            .organisation(false)
            .title("Ms")
            .forenames("A")
            .surname("B")
            .businessUnitId((short) 2)
            .businessUnitName("BU2")
            .welshLanguage(false)
            .awarded(null) // covers null check on awarded
            .paidOut(BigDecimal.ZERO)
            .awaitingPayment(BigDecimal.ZERO)
            .outstanding(new BigDecimal("1.00")) // outstanding > 0 => true
            .build();

        when(minorCreditorAccountHeaderRepository.findById(id)).thenReturn(Optional.of(entity));

        // Act
        GetMinorCreditorAccountHeaderSummaryResponse resp = service.getHeaderSummary(id);

        // Assert
        assertEquals(Boolean.TRUE, resp.getHasAssociatedDefendant());
    }

    @Test
    void getHeaderSummary_notFound_throwsEntityNotFoundException_withReason() {
        // Arrange
        long id = 999L;
        when(minorCreditorAccountHeaderRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> service.getHeaderSummary(id));

        // Assert
        assertTrue(ex.getMessage().contains("Minor creditor account not found: " + id));

        verify(minorCreditorAccountHeaderRepository).findById(id);
    }

    private static void assertCreditorAccountType(CreditorAccountTypeReference reference,
                                                  String expectedType,
                                                  String expectedDisplayName) {
        assertNotNull(reference);
        assertEquals(expectedType, reference.getType());
        assertEquals(expectedDisplayName, reference.getDisplayName());
    }

}
