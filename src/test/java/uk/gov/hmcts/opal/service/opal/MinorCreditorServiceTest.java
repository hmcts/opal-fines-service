package uk.gov.hmcts.opal.service.opal;

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
import uk.gov.hmcts.opal.dto.CreditorAccountDto;
import uk.gov.hmcts.opal.dto.DefendantDto;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;
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
class MinorCreditorServiceTest {

    @Mock
    private MinorCreditorRepository minorCreditorRepository;

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
}
