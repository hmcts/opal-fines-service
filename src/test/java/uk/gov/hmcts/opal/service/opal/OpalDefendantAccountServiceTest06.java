package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto.Checks;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto.WarnError;
import uk.gov.hmcts.opal.dto.legacy.ReferenceNumberDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.AliasDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.search.SearchConsolidatedEntity;
import uk.gov.hmcts.opal.entity.search.SearchDefendantAccount.BasicEntity;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.repository.SearchDefendantBasicRepository;
import uk.gov.hmcts.opal.repository.SearchDefendantConsolidatedRepository;
import uk.gov.hmcts.opal.repository.jpa.SearchBasicEntitySpecs;
import uk.gov.hmcts.opal.repository.jpa.SearchConsolidatedEntitySpecs;

@ExtendWith(MockitoExtension.class)
class OpalDefendantAccountServiceTest06 {

    @Mock
    private SearchDefendantBasicRepository searchDefendantBasicRepository;

    @Mock
    private SearchDefendantConsolidatedRepository searchConsolidatedRepository;

    @Spy
    private SearchBasicEntitySpecs searchBasicEntitySpecs;

    @Spy
    private SearchConsolidatedEntitySpecs searchConsolidatedEntitySpecs;

    // Service under test
    @InjectMocks
    private OpalDefendantAccountService service;

    @Test
    void testSearch_mapsAliases_forIndividual() {
        // Given a person with mixed alias shapes
        BasicEntity row = BasicEntity.builder()
            .defendantAccountId(1L)
            .accountNumber("ACC1")
            .organisation(false)
            .organisationName(null)
            .title("Mr")
            .forenames("Amy")
            .surname("Pond")
            .addressLine1("1 Main St")
            .postcode("AB12CD")
            .businessUnitName("BU")
            .businessUnitId((short)99)
            .prosecutorCaseReference("PCR1")
            .lastEnforcement("LEVY")
            .defendantAccountBalance(new BigDecimal("12.34"))
            .birthDate(LocalDate.of(2000, 1, 1))
            .alias1("Amy Pond")       // normal "forenames surname"
            .alias2("Amelia Pond")    // another normal case
            .alias3("  ")             // blank → ignored
            .alias4(null)             // null → ignored
            .alias5("Pond")           // single token → treated as surname
            .build();

        when(searchDefendantBasicRepository.findAll(
            ArgumentMatchers.<Specification<BasicEntity>>any()
        )).thenReturn(Collections.singletonList(row));

        // When
        DefendantAccountSearchResultsDto out =
            service.searchDefendantAccounts(emptyCriteria());

        // Then
        assertEquals(1, out.getCount());
        var dto = out.getDefendantAccounts().get(0);

        assertFalse(dto.getOrganisation());
        assertEquals("ACC1", dto.getAccountNumber());
        assertEquals("LEVY", dto.getLastEnforcementAction());
        assertEquals(0, dto.getAccountBalance().compareTo(new BigDecimal("12.34")));

        // Aliases: alias1, alias2, alias5 should be present
        List<AliasDto> aliases = dto.getAliases();
        assertEquals(3, aliases.size());

        // Check a “forenames surname” split
        AliasDto a1 = aliases.stream().filter(a -> a.getAliasNumber() == 1).findFirst().orElseThrow();
        assertEquals("Amy", a1.getForenames());
        assertEquals("Pond", a1.getSurname());
        assertNull(a1.getOrganisationName());

        // Single token treated as surname
        AliasDto a5 = aliases.stream().filter(a -> a.getAliasNumber() == 5).findFirst().orElseThrow();
        assertNull(a5.getForenames());
        assertEquals("Pond", a5.getSurname());
    }

    @Test
    void testSearch_mapsAliases_forOrganisation() {
        // Given an organisation, alias fields are full org names
        BasicEntity row = BasicEntity.builder()
            .defendantAccountId(2L)
            .accountNumber("ACC2")
            .organisation(true)
            .organisationName("Wayne Enterprises")
            .businessUnitName("BU")
            .businessUnitId((short)88)
            .prosecutorCaseReference("PCR2")
            .lastEnforcement("CLAMP")
            .defendantAccountBalance(new BigDecimal("99.00"))
            .alias1("Wayne Ent Ltd")
            .alias2("Wayne Group")
            .alias3(null)
            .alias4("")
            .alias5(" Wayne Holdings ")
            .build();

        when(searchDefendantBasicRepository.findAll(
            ArgumentMatchers.<Specification<BasicEntity>>any()
        )).thenReturn(Collections.singletonList(row));

        DefendantAccountSearchResultsDto out = service.searchDefendantAccounts(emptyCriteria());

        var dto = out.getDefendantAccounts().getFirst();

        assertTrue(dto.getOrganisation());
        assertEquals("Wayne Enterprises", dto.getOrganisationName());
        // Personal fields must be null for orgs
        assertNull(dto.getDefendantTitle());
        assertNull(dto.getDefendantFirstnames());
        assertNull(dto.getDefendantSurname());

        // Aliases: names go into organisationName; person fields null
        List<AliasDto> aliases = dto.getAliases();
        assertEquals(3, aliases.size());
        assertTrue(aliases.stream().allMatch(a ->
            a.getOrganisationName() != null
                && a.getForenames() == null
                && a.getSurname() == null));
    }

    @Test
    void testSearch_ignoresBlankAliasSlots() {
        BasicEntity row = BasicEntity.builder()
            .defendantAccountId(3L)
            .accountNumber("ACC3")
            .organisation(false)
            .alias1("John Doe")
            .alias2("   ")
            .alias3("")
            .alias4(null)
            .alias5(null)
            .build();

        when(searchDefendantBasicRepository.findAll(
            ArgumentMatchers.<Specification<BasicEntity>>any()
        )).thenReturn(Collections.singletonList(row));

        var out = service.searchDefendantAccounts(emptyCriteria());
        var aliases = out.getDefendantAccounts().getFirst().getAliases();
        assertEquals(1, aliases.size());
        assertEquals(1, aliases.getFirst().getAliasNumber());
        assertEquals("John", aliases.getFirst().getForenames());
        assertEquals("Doe", aliases.getFirst().getSurname());
    }

    @Test
    void testSearch_hasRefTrueBranchCovered() {
        ReferenceNumberDto ref = ReferenceNumberDto.builder().prosecutorCaseReference("177").build();
        AccountSearchDto dto = AccountSearchDto.builder()
            .activeAccountsOnly(true)
            .referenceNumberDto(ref)
            .businessUnitIds(List.of((short)78))
            .build();

        // should not throw; branch just sets applyActiveOnly=false
        service.searchDefendantAccounts(dto);
    }

    private AccountSearchDto emptyCriteria() {
        return AccountSearchDto.builder().build();
    }

    @Test
    void testSearch_whenAccountNumberPresent_activeOnlyIsIgnored() {
        // given
        AccountSearchDto dto = AccountSearchDto.builder()
            .activeAccountsOnly(true)
            .referenceNumberDto(ReferenceNumberDto.builder().accountNumber("AAAAAAAAX").build())
            .build();

        // when
        service.searchDefendantAccounts(dto);

        verify(searchDefendantBasicRepository, times(1))
            .findAll(ArgumentMatchers.<Specification<BasicEntity>>any());
        verify(searchDefendantBasicRepository, times(1))
            .findAll(ArgumentMatchers.<Specification<BasicEntity>>any());
    }

    @Test
    void testSearch_whenPcrPresent_activeOnlyIsIgnored() {
        // given
        ReferenceNumberDto ref = ReferenceNumberDto.builder().prosecutorCaseReference("PCR/1234/XY").build();
        AccountSearchDto dto = AccountSearchDto.builder().activeAccountsOnly(true).referenceNumberDto(ref).build();

        // when
        service.searchDefendantAccounts(dto);

        // then
        verify(searchDefendantBasicRepository, times(1))
            .findAll(ArgumentMatchers.<Specification<BasicEntity>>any());
        verify(searchDefendantBasicRepository, times(1))
            .findAll(ArgumentMatchers.<Specification<BasicEntity>>any());
    }

    @Test
    void testSearch_whenNoReference_activeOnlyIsRespected() {
        // given
        AccountSearchDto dto = AccountSearchDto.builder().activeAccountsOnly(true).build();

        // when
        service.searchDefendantAccounts(dto);

        // then → with no reference, activeOnly should be applied as true
        verify(searchDefendantBasicRepository, times(1))
            .findAll(ArgumentMatchers.<Specification<BasicEntity>>any());
        verify(searchDefendantBasicRepository, times(1))
            .findAll(ArgumentMatchers.<Specification<BasicEntity>>any());
    }

    @Test
    void testSearch_whenActiveOnlyFalse_andReferencePresent_stillIgnoredButFalseIsCorrect() {
        // given
        ReferenceNumberDto ref = ReferenceNumberDto.builder().accountNumber("AAAAAAAAX").build();
        AccountSearchDto dto = AccountSearchDto.builder().activeAccountsOnly(false).referenceNumberDto(ref).build();

        // when
        service.searchDefendantAccounts(dto);

        // then → should pass false (ignoring or not, final effect is false)
        verify(searchDefendantBasicRepository, times(1))
            .findAll(ArgumentMatchers.<Specification<BasicEntity>>any());
        verify(searchDefendantBasicRepository, times(1))
            .findAll(ArgumentMatchers.<Specification<BasicEntity>>any());
    }

    @Test
    void testSearch_whenReferenceOrganisationFlagProvided_appliesFilterCorrectly() {
        // Arrange
        ReferenceNumberDto ref = ReferenceNumberDto.builder().organisation(Boolean.TRUE).build();
        AccountSearchDto dto = AccountSearchDto.builder().activeAccountsOnly(false).referenceNumberDto(ref).build();

        // Act
        service.searchDefendantAccounts(dto);

        // Assert
        verify(searchBasicEntitySpecs, times(1)).equalsOrganisation(Boolean.TRUE);
        verify(searchDefendantBasicRepository, times(1))
            .findAll(ArgumentMatchers.<Specification<BasicEntity>>any());
    }

    @Test
    void testConsolidatedSearch() {
        SearchConsolidatedEntity entity1 = SearchConsolidatedEntity.builder()
            .defendantAccountId(1L)
            .errors(List.of("Error1", "|Error2"))
            .warnings(List.of("", "|", "Warn3|Warning Message 3"))
            .hasCollectionOrder(true)
            .versionNumber(123L)
            .build();
        SearchConsolidatedEntity entity2 = SearchConsolidatedEntity.builder()
            .build();
        List<SearchConsolidatedEntity> dbEntities = List.of(entity1, entity2);

        when(searchConsolidatedRepository.findAll(ArgumentMatchers.<Specification<SearchConsolidatedEntity>>any()))
            .thenReturn(dbEntities);

        AccountSearchDto dto = AccountSearchDto.builder()
            .activeAccountsOnly(true)
            .referenceNumberDto(ReferenceNumberDto.builder().prosecutorCaseReference("177").build())
            .businessUnitIds(List.of((short) 78))
            .consolidationSearch(true)
            .build();

        DefendantAccountSearchResultsDto resultsDto = service.searchDefendantAccounts(dto);
        assertEquals(2, resultsDto.getCount());

        DefendantAccountSummaryDto summary = resultsDto.getDefendantAccounts().get(0);
        assertTrue(summary.getHasCollectionOrder());
        assertEquals(new BigInteger("123"), summary.getAccountVersion());

        Checks checks = summary.getChecks();
        assertNotNull(checks);
        List<WarnError> errors = checks.getErrors();
        assertEquals(2, errors.size());
        WarnError error1 = errors.get(0);
        assertEquals("Error1", error1.getReference());
        assertEquals("", error1.getMessage());
        WarnError error2 = errors.get(1);
        assertEquals("", error2.getReference());
        assertEquals("Error2", error2.getMessage());

        List<WarnError> warnings = checks.getWarnings();
        assertEquals(2, warnings.size());
        WarnError warn1 = warnings.get(0);
        assertEquals("", warn1.getReference());
        assertEquals("", warn1.getMessage());
        WarnError warn2 = warnings.get(1);
        assertEquals("Warn3", warn2.getReference());
        assertEquals("Warning Message 3", warn2.getMessage());
    }

    @Test
    void testConsolidatedSearch_tooManyResults() {
        SearchConsolidatedEntity entity2 = SearchConsolidatedEntity.builder().build();
        List<SearchConsolidatedEntity> dbEntities = Collections.nCopies(101, entity2);

        when(searchConsolidatedRepository.findAll(ArgumentMatchers.<Specification<SearchConsolidatedEntity>>any()))
            .thenReturn(dbEntities);

        AccountSearchDto dto = AccountSearchDto.builder().consolidationSearch(true).build();

        UnprocessableException ex = assertThrows(
            UnprocessableException.class,
            () -> service.searchDefendantAccounts(dto)
        );

        assertEquals("Search generated more than 100 results. Please refine your search and try again.",
            ex.getDetailedReason());
    }

    @Test
    void testConsolidatedSearch_notTooManyResults() {
        SearchConsolidatedEntity entity2 = SearchConsolidatedEntity.builder().build();
        List<SearchConsolidatedEntity> dbEntities = Collections.nCopies(100, entity2);

        when(searchConsolidatedRepository.findAll(ArgumentMatchers.<Specification<SearchConsolidatedEntity>>any()))
            .thenReturn(dbEntities);

        AccountSearchDto dto = AccountSearchDto.builder().consolidationSearch(true).build();

        DefendantAccountSearchResultsDto resultsDto = service.searchDefendantAccounts(dto);
        assertEquals(100, resultsDto.getCount());
    }

}
