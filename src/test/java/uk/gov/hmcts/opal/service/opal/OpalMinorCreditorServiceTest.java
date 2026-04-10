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
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse.AtAGlanceDefendant;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse.CreditorHeader;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse.Financials;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.Payment;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.CreditorAccountTypeReference;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountHeaderEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;
import uk.gov.hmcts.opal.mapper.MinorCreditorAccountHeaderSummaryMapper;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountAtAGlanceEntity;
import uk.gov.hmcts.opal.mapper.response.GetMinorCreditorAccountAtAGlanceResponseMapper;
import uk.gov.hmcts.opal.repository.MinorCreditorAccountAtAGlanceRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorAccountHeaderRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorRepository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import uk.gov.hmcts.opal.repository.PartyRepository;

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

    @Mock
    private MinorCreditorAccountHeaderSummaryMapper headerSummaryMapper;

    @Mock
    private MinorCreditorAccountAtAGlanceRepository minorCreditorAccountAtAGlanceRepository;

    @Mock
    private PartyRepository partyRepository;

    @Mock
    private GetMinorCreditorAccountAtAGlanceResponseMapper atAGlanceResponseMapper;

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
    void getHeaderSummary_happyPath_callsCorrectRepositoriesAndMappers() {
        // Arrange
        long id = 99000000000800L;
        long partyId = 99000000000900L;

        MinorCreditorAccountHeaderEntity entity = MinorCreditorAccountHeaderEntity.builder()
            .creditorAccountId(id)
            .creditorAccountNumber("87654321")
            .creditorAccountType(CreditorAccountType.MN)
            .versionNumber(5L)
            .partyId(partyId)
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

        PartyEntity party = PartyEntity.builder().partyId(partyId).build();

        when(minorCreditorAccountHeaderRepository.findById(id)).thenReturn(Optional.of(entity));
        when(partyRepository.findById(partyId)).thenReturn(Optional.of(party));
        GetMinorCreditorAccountHeaderSummaryResponse mapped = buildHeaderSummaryResponse(String.valueOf(id));
        when(headerSummaryMapper.toResponse(entity, party)).thenReturn(mapped);

        // Act
        GetMinorCreditorAccountHeaderSummaryResponse res = service.getHeaderSummary(id);

        // Assert
        assertSame(mapped, res);
        verify(minorCreditorAccountHeaderRepository, times(1)).findById(id);
        verify(partyRepository, times(1)).findById(partyId);
        verify(headerSummaryMapper).toResponse(entity, party);
    }

    @Test
    void getHeaderSummary_creditorNotFound_throwsEntityNotFoundException_withExpectedMessage() {
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
        verify(headerSummaryMapper, Mockito.never()).toResponse(Mockito.any(), Mockito.any());
    }

    @Test
    void getHeaderSummary_partyNotFound_throwsEntityNotFoundException_withExpectedMessage() {
        // Arrange
        long id = 99000000000801L;
        long missingId = 123456789L;


        MinorCreditorAccountHeaderEntity entity = MinorCreditorAccountHeaderEntity.builder()
            .creditorAccountId(id)
            .creditorAccountNumber("87654322")
            .creditorAccountType(CreditorAccountType.MN)
            .hasAssociatedDefendant(true)
            .versionNumber(null) // verify null -> null
            .partyId(missingId)
            .title("Mr")
            .forenames("John")
            .surname("Smith")
            .organisation(false)
            .organisationName(null)
            .businessUnitId((short) 77)
            .businessUnitName("Camberwell Green")
            .welshLanguage(true)
            .awarded(new BigDecimal("10.00"))
            .paidOut(BigDecimal.ZERO)
            .awaitingPayment(BigDecimal.ZERO)
            .outstanding(BigDecimal.ZERO)
            .build();

        when(minorCreditorAccountHeaderRepository.findById(id)).thenReturn(Optional.of(entity));
        when(partyRepository.findById(missingId)).thenReturn(Optional.empty());

        // Act
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                                                  () -> service.getHeaderSummary(id));

        // Assert
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("Minor creditor party not found: " + missingId));

        verify(minorCreditorAccountHeaderRepository, times(1)).findById(id);
        verify(partyRepository, times(1)).findById(missingId);
        verify(headerSummaryMapper, Mockito.never()).toResponse(Mockito.any(), Mockito.any());
    }

    @Test
    void getMinorCreditorAtAGlance_happyPath_allFields() {
        // Arrange
        long id = 200L;

        MinorCreditorAccountAtAGlanceEntity creditor = getFullAtAGlanceEntity();
        PartyEntity party = PartyEntity.builder()
            .partyId(100L)
            .organisation(true)
            .organisationName("The Republic")
            .addressLine1("Coruscant")
            .postcode("SP4CE")
            .build();

        when(minorCreditorAccountAtAGlanceRepository.findById(id)).thenReturn(Optional.of(creditor));
        when(partyRepository.findById(id)).thenReturn(Optional.of(party));

        GetMinorCreditorAccountAtAGlanceResponse mapped = GetMinorCreditorAccountAtAGlanceResponse.builder()
            .party(PartyDetails.builder().partyId("100").organisationFlag(true).organisationDetails(
                OrganisationDetails.builder().organisationName("The Republic").build()).build())
            .address(AddressDetails.builder().addressLine1("Coruscant").postcode("SP4CE").build())
            .creditorAccountId(1977L)
            .defendant(AtAGlanceDefendant.builder()
                .accountNumber("066")
                .accountId(66L)
                .title("Mr")
                .forenames("Obi Wan")
                .surname("Kenobi")
                .build())
            .payment(Payment.builder().holdPayment(false).bacs(true).build())
            .build();

        when(atAGlanceResponseMapper.toDto(creditor, party)).thenReturn(mapped);

        // Act
        GetMinorCreditorAccountAtAGlanceResponse response = service.getMinorCreditorAtAGlance(id);

        // Assert
        assertNotNull(response);

        // Party
        assertNotNull(response.getParty());
        assertEquals("100", response.getParty().getPartyId());
        assertTrue(response.getParty().getOrganisationFlag());

        assertNotNull(response.getParty().getOrganisationDetails());
        assertEquals("The Republic",
            response.getParty().getOrganisationDetails().getOrganisationName());
        assertNull(response.getParty().getOrganisationDetails().getOrganisationAliases());

        assertNull(response.getParty().getIndividualDetails());

        // Address
        assertNotNull(response.getAddress());
        assertEquals("Coruscant", response.getAddress().getAddressLine1());
        assertNull(response.getAddress().getAddressLine2());
        assertNull(response.getAddress().getAddressLine3());
        assertNull(response.getAddress().getAddressLine4());
        assertNull(response.getAddress().getAddressLine5());
        assertEquals("SP4CE", response.getAddress().getPostcode());

        // Creditor Account
        assertEquals(1977, response.getCreditorAccountId());

        // Defendant
        assertNotNull(response.getDefendant());
        assertEquals("066", response.getDefendant().getAccountNumber());
        assertEquals(66, response.getDefendant().getAccountId());
        assertEquals("Mr", response.getDefendant().getTitle());
        assertEquals("Obi Wan", response.getDefendant().getForenames());
        assertEquals("Kenobi", response.getDefendant().getSurname());

        // Payment
        assertNotNull(response.getPayment());
        assertTrue(response.getPayment().getBacs());
        assertFalse(response.getPayment().getHoldPayment());
    }

    @Test
    void getMinorCreditorAtAGlance_creditorNotFound() {
        // Arrange
        long id = 200L;
        when(minorCreditorAccountAtAGlanceRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
            service.getMinorCreditorAtAGlance(id));

        // Assert
        assertEquals("Minor creditor account not found: " + id, ex.getMessage());

        verify(minorCreditorAccountAtAGlanceRepository).findById(id);
    }

    @Test
    void getMinorCreditorAtAGlance_partyNotFound() {
        // Arrange
        long id = 200L;
        when(minorCreditorAccountAtAGlanceRepository.findById(id)).thenReturn(
            Optional.of(getFullAtAGlanceEntity()));
        when(partyRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
            service.getMinorCreditorAtAGlance(id));

        // Assert
        assertEquals("Party not found: " + id, ex.getMessage());

        verify(partyRepository).findById(id);
    }

    private MinorCreditorAccountAtAGlanceEntity getFullAtAGlanceEntity() {
        return MinorCreditorAccountAtAGlanceEntity.builder()
           .creditorId(1977L)
           .accountNumber("66")
           .payByBacs(true)
           .versionNumber(9L)
           .holdPayout(false)
           .partyId(200L)
           .creditorTitle("Mr")
           .creditorForenames("Obi Wan")
           .creditorSurname("Kenobi")
           .creditorOrganisation(false)
           .addressLine1("Tatooine")
           .postcode("T S4ND")
           .defendantAccountId(66L)
           .defendantAccountNumber("O66")
           .defendantTitle("Senator")
           .defendantForenames("Sheev")
           .defendantSurname("Palpatine")
           .build();
    }

    private GetMinorCreditorAccountHeaderSummaryResponse buildHeaderSummaryResponse(String creditorAccountId) {
        return GetMinorCreditorAccountHeaderSummaryResponse.builder()
            .version(BigInteger.valueOf(5L))
            .party(PartyDetails.builder()
                .partyId("99000000000900")
                .organisationFlag(true)
                .organisationDetails(OrganisationDetails.builder()
                    .organisationName("Minor Creditor Test Ltd")
                    .build())
                .individualDetails(IndividualDetails.builder()
                    .title("Mr")
                    .forenames("John")
                    .surname("Smith")
                    .build())
                .build())
            .businessUnit(BusinessUnitSummary.builder()
                .businessUnitId("77")
                .businessUnitName("Camberwell Green")
                .welshSpeaking("N")
                .build())
            .creditor(CreditorHeader.builder()
                .accountId(creditorAccountId)
                .accountNumber("404")
                .accountType(CreditorAccountTypeReference.builder()
                    .type("MN")
                    .displayName("Minor Creditor")
                    .build())
                .hasAssociatedDefendant(false)
                .build())
            .financials(Financials.builder()
                .awarded(BigDecimal.ZERO)
                .paidOut(BigDecimal.ZERO)
                .awaitingPayout(BigDecimal.ZERO)
                .outstanding(BigDecimal.ZERO)
                .build())
            .build();
    }
}
