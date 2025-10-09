package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.PaymentStateSummary;
import uk.gov.hmcts.opal.dto.legacy.ReferenceNumberDto;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.AliasDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountHeaderViewEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountSummaryViewEntity;
import uk.gov.hmcts.opal.entity.SearchDefendantAccountEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountHeaderViewRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountPaymentTermsRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountSummaryViewRepository;
import uk.gov.hmcts.opal.repository.SearchDefendantAccountRepository;
import uk.gov.hmcts.opal.repository.jpa.SearchDefendantAccountSpecs;

class OpalDefendantAccountServiceTest {

    private final DefendantAccountRepository defendantAccountRepository =
        mock(DefendantAccountRepository.class);
    private final DefendantAccountSummaryViewRepository dasvRepository =
        mock(DefendantAccountSummaryViewRepository.class);

    private final DefendantAccountHeaderViewRepository dahvRepository =
        mock(DefendantAccountHeaderViewRepository.class);

    private final SearchDefendantAccountRepository searchDefAccRepo =
        mock(SearchDefendantAccountRepository.class);

    private final SearchDefendantAccountSpecs searchDefAccSpecs = new SearchDefendantAccountSpecs();

    private final DefendantAccountPaymentTermsRepository paymentTermsRepository =
        mock(DefendantAccountPaymentTermsRepository.class);

    // make the specs a SPY so we can verify its method call
    private final SearchDefendantAccountSpecs specs =
        spy(new SearchDefendantAccountSpecs());


    // Service under test (NOT final; we construct it in @BeforeEach)
    private OpalDefendantAccountService service;

    @BeforeEach
    void setUp() {
        // IMPORTANT: inject the SAME 'specs' spy into the service 6-arg ctor
        service = new OpalDefendantAccountService(
            dahvRepository,
            defendantAccountRepository,
            searchDefAccRepo,
            specs, // <- spy injected here
            paymentTermsRepository,
            dasvRepository
        );

        // Generic matcher to avoid unchecked warnings
        when(searchDefAccRepo.findAll(
            ArgumentMatchers.<Specification<SearchDefendantAccountEntity>>any()
        )).thenReturn(List.of());
    }

    @Test
    void testDefendantAccountById() {
        long testId = 1L;

        DefendantAccountEntity entity = DefendantAccountEntity.builder().build();
        when(defendantAccountRepository.findById(testId)).thenReturn(java.util.Optional.of(entity));

        DefendantAccountEntity result = service.getDefendantAccountById(testId);
        assertNotNull(result);
    }

    @Test
    void testNzHelper() {
        assertEquals(BigDecimal.valueOf(10), OpalDefendantAccountService.nz(BigDecimal.valueOf(10)));
        assertEquals(BigDecimal.ZERO, OpalDefendantAccountService.nz(null));
    }

    @Test
    void testCalculateAge() {
        int age = service.calculateAge(LocalDate.now().minusYears(22));
        assertTrue(age == 22 || age == 21); // depending on birthday
        assertEquals(0, service.calculateAge(null));
    }

    @Test
    void testResolveStatusDisplayName() {
        assertEquals("Live", service.resolveStatusDisplayName("L"));
        assertEquals("Completed", service.resolveStatusDisplayName("C"));
        assertEquals("TFO to be acknowledged", service.resolveStatusDisplayName("TO"));
        assertEquals("TFO to NI/Scotland to be acknowledged", service.resolveStatusDisplayName("TS"));
        assertEquals("TFO acknowledged", service.resolveStatusDisplayName("TA"));
        assertEquals("Account consolidated", service.resolveStatusDisplayName("CS"));
        assertEquals("Account written off", service.resolveStatusDisplayName("WO"));
        assertEquals("Unknown", service.resolveStatusDisplayName("nonsense"));
    }

    @Test
    void testBuildPaymentStateSummary() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .imposed(BigDecimal.valueOf(5))
            .arrears(BigDecimal.valueOf(2))
            .paid(BigDecimal.valueOf(3))
            .accountBalance(BigDecimal.valueOf(7))
            .build();

        PaymentStateSummary summary = service.buildPaymentStateSummary(e);
        assertEquals(BigDecimal.valueOf(5), summary.getImposedAmount());
        assertEquals(BigDecimal.valueOf(2), summary.getArrearsAmount());
        assertEquals(BigDecimal.valueOf(3), summary.getPaidAmount());
        assertEquals(BigDecimal.valueOf(7), summary.getAccountBalance());
    }

    @Test
    void testBuildPartyDetails_allFieldsNullSafe() {
        DefendantAccountHeaderViewEntity e = new DefendantAccountHeaderViewEntity();
        PartyDetails details = service.buildPartyDetails(e);
        assertNotNull(details);
        // If needed, add more asserts based on expected default/null behaviour
    }

    @Test
    void testBuildAccountStatusReference() {
        AccountStatusReference ref = service.buildAccountStatusReference("L");
        assertEquals("L", ref.getAccountStatusCode());
        assertEquals("Live", ref.getAccountStatusDisplayName());
    }

    @Test
    void testBuildBusinessUnitSummary() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .businessUnitId((short) 55)
            .businessUnitName("NorthEast")
            .build();

        BusinessUnitSummary summary = service.buildBusinessUnitSummary(e);
        assertEquals("55", summary.getBusinessUnitId());
        assertEquals("NorthEast", summary.getBusinessUnitName());
        assertEquals("N", summary.getWelshSpeaking());
    }

    @Test
    void testMapToDtoCoversFields() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .partyId(123L)
            .parentGuardianAccountPartyId(456L)
            .accountNumber("ACCT100")
            .accountType("Fine")
            .prosecutorCaseReference("PCR1")
            .fixedPenaltyTicketNumber("FPT1")
            .accountStatus("L")
            .businessUnitId((short)77)
            .businessUnitName("BUName")
            .imposed(BigDecimal.valueOf(11))
            .arrears(BigDecimal.valueOf(22))
            .paid(BigDecimal.valueOf(33))
            .accountBalance(BigDecimal.valueOf(44))
            .organisation(false)
            .organisationName("MyOrg")
            .title("Sir")
            .firstnames("Robo")
            .surname("Cop")
            .birthDate(LocalDate.now().minusYears(10))
            .build();

        DefendantAccountHeaderSummary dto = service.mapToDto(e);
        assertEquals("ACCT100", dto.getAccountNumber());
        assertNotNull(dto.getPartyDetails());
    }

    @Test
    void testGetDefendantAccountSummaryViewById() {
        long testId = 1L;

        DefendantAccountSummaryViewEntity viewEntity = DefendantAccountSummaryViewEntity.builder().build();
        when(dasvRepository.findById(testId)).thenReturn(java.util.Optional.of(viewEntity));

        DefendantAccountSummaryViewEntity result = service.getDefendantAccountSummaryViewById(testId);
        assertNotNull(result);
    }


    @Test
    void convertEntityToAtAGlanceResponse_mapsAllFields_Individual() {
        DefendantAccountSummaryViewEntity entity = DefendantAccountSummaryViewEntity.builder()
            .defendantAccountId(1L)
            .accountNumber("ACC123")
            .debtorType("Defendant")
            .birthDate(LocalDateTime.now().minusYears(17))
            .organisation(false)
            .forenames("John")
            .surname("Doe")
            .addressLine1("123 Main St")
            .addressLine2("Apt 4B")
            .addressLine3("City Center")
            .addressLine4("Region")
            .addressLine5("Country")
            .postcode("12345")
            .collectionOrder(true)
            .jailDays(10)
            .lastMovementDate(LocalDateTime.now().minusDays(5))
            .accountComments("Comment")
            .accountNote1("Note1")
            .accountNote2("Note2")
            .accountNote3("Note3")
            .build();

        DefendantAccountAtAGlanceResponse response = service.convertEntityToAtAGlanceResponse(entity);

        assertNotNull(response);
        assertEquals("1", response.getDefendantAccountId());
        assertEquals("ACC123", response.getAccountNumber());
        assertEquals("Defendant", response.getDebtorType());
        assertTrue(response.getIsYouth());
        assertNotNull(response.getPartyDetails());
    }

    @Test
    void convertEntityToAtAGlanceResponse_mapsAllFields_Organisation() {
        DefendantAccountSummaryViewEntity entity = DefendantAccountSummaryViewEntity.builder()
            .defendantAccountId(1L)
            .accountNumber("ACC123")
            .debtorType("Defendant")
            .birthDate(LocalDateTime.now().minusYears(17))
            .organisation(true)
            .forenames("John")
            .surname("Doe")
            .addressLine1("123 Main St")
            .addressLine2("Apt 4B")
            .addressLine3("City Center")
            .addressLine4("Region")
            .addressLine5("Country")
            .postcode("12345")
            .collectionOrder(true)
            .jailDays(10)
            .lastMovementDate(LocalDateTime.now().minusDays(5))
            .accountComments("Comment")
            .accountNote1("Note1")
            .accountNote2("Note2")
            .accountNote3("Note3")
            .build();

        DefendantAccountAtAGlanceResponse response = service.convertEntityToAtAGlanceResponse(entity);

        assertNotNull(response);
        assertEquals("1", response.getDefendantAccountId());
        assertEquals("ACC123", response.getAccountNumber());
        assertEquals("Defendant", response.getDebtorType());
        assertTrue(response.getIsYouth());
        assertNotNull(response.getPartyDetails());
    }

    @Test
    void searchDefendantAccounts_mapsAliases_forIndividual() {
        // Given a person with mixed alias shapes
        SearchDefendantAccountEntity row = SearchDefendantAccountEntity.builder()
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
            .businessUnitId(99L)
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

        when(searchDefAccRepo.findAll(
            ArgumentMatchers.<Specification<SearchDefendantAccountEntity>>any()
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
    void searchDefendantAccounts_mapsAliases_forOrganisation() {
        // Given an organisation, alias fields are full org names
        SearchDefendantAccountEntity row = SearchDefendantAccountEntity.builder()
            .defendantAccountId(2L)
            .accountNumber("ACC2")
            .organisation(true)
            .organisationName("Wayne Enterprises")
            .businessUnitName("BU")
            .businessUnitId(88L)
            .prosecutorCaseReference("PCR2")
            .lastEnforcement("CLAMP")
            .defendantAccountBalance(new BigDecimal("99.00"))
            .alias1("Wayne Ent Ltd")
            .alias2("Wayne Group")
            .alias3(null)
            .alias4("")
            .alias5(" Wayne Holdings ")
            .build();

        when(searchDefAccRepo.findAll(
            ArgumentMatchers.<Specification<SearchDefendantAccountEntity>>any()
        )).thenReturn(Collections.singletonList(row));

        DefendantAccountSearchResultsDto out = service.searchDefendantAccounts(emptyCriteria());

        var dto = out.getDefendantAccounts().get(0);

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
    void searchDefendantAccounts_ignoresBlankAliasSlots() {
        SearchDefendantAccountEntity row = SearchDefendantAccountEntity.builder()
            .defendantAccountId(3L)
            .accountNumber("ACC3")
            .organisation(false)
            .alias1("John Doe")
            .alias2("   ")
            .alias3("")
            .alias4(null)
            .alias5(null)
            .build();

        when(searchDefAccRepo.findAll(
            ArgumentMatchers.<Specification<SearchDefendantAccountEntity>>any()
        )).thenReturn(Collections.singletonList(row));

        var out = service.searchDefendantAccounts(emptyCriteria());
        var aliases = out.getDefendantAccounts().get(0).getAliases();
        assertEquals(1, aliases.size());
        assertEquals(1, aliases.get(0).getAliasNumber());
        assertEquals("John", aliases.get(0).getForenames());
        assertEquals("Doe", aliases.get(0).getSurname());
    }

    @Test
    void whenAccountNumberPresent_activeOnlyIsIgnored() {
        // given
        AccountSearchDto dto = mock(AccountSearchDto.class, RETURNS_DEEP_STUBS);
        ReferenceNumberDto ref = mock(ReferenceNumberDto.class);

        when(dto.getActiveAccountsOnly()).thenReturn(true); // the user asked for active-only
        when(dto.getReferenceNumberDto()).thenReturn(ref);
        when(ref.getAccountNumber()).thenReturn("AAAAAAAAX"); // reference present
        when(ref.getProsecutorCaseReference()).thenReturn(null);

        // when
        service.searchDefendantAccounts(dto);

        // then → AC1b requires the service to IGNORE activeOnly (i.e., pass false to the spec)
        verify(specs, times(1)).filterByActiveOnly(false);
        verify(searchDefAccRepo, times(1)).findAll(
            ArgumentMatchers.<Specification<SearchDefendantAccountEntity>>any());
    }

    @Test
    void whenPcrPresent_activeOnlyIsIgnored() {
        // given
        AccountSearchDto dto = mock(AccountSearchDto.class, RETURNS_DEEP_STUBS);
        ReferenceNumberDto ref = mock(ReferenceNumberDto.class);

        when(dto.getActiveAccountsOnly()).thenReturn(true);
        when(dto.getReferenceNumberDto()).thenReturn(ref);
        when(ref.getAccountNumber()).thenReturn(null);
        when(ref.getProsecutorCaseReference()).thenReturn("PCR/1234/XY"); // PCR present

        // when
        service.searchDefendantAccounts(dto);

        // then
        verify(specs, times(1)).filterByActiveOnly(false);
        verify(searchDefAccRepo, times(1)).findAll(
            ArgumentMatchers.<Specification<SearchDefendantAccountEntity>>any());
    }

    @Test
    void whenNoReference_activeOnlyIsRespected() {
        // given
        AccountSearchDto dto = mock(AccountSearchDto.class, RETURNS_DEEP_STUBS);
        when(dto.getActiveAccountsOnly()).thenReturn(true);
        when(dto.getReferenceNumberDto()).thenReturn(null); // no account number, no PCR

        // when
        service.searchDefendantAccounts(dto);

        // then → with no reference, activeOnly should be applied as true
        verify(specs, times(1)).filterByActiveOnly(true);
        verify(searchDefAccRepo, times(1)).findAll(
            ArgumentMatchers.<Specification<SearchDefendantAccountEntity>>any());
    }

    @Test
    void whenActiveOnlyFalse_andReferencePresent_stillIgnoredButFalseIsCorrect() {
        // given
        AccountSearchDto dto = mock(AccountSearchDto.class, RETURNS_DEEP_STUBS);
        ReferenceNumberDto ref = mock(ReferenceNumberDto.class);

        when(dto.getActiveAccountsOnly()).thenReturn(false); // already false
        when(dto.getReferenceNumberDto()).thenReturn(ref);
        when(ref.getAccountNumber()).thenReturn("AAAAAAAAX");

        // when
        service.searchDefendantAccounts(dto);

        // then → should pass false (ignoring or not, final effect is false)
        verify(specs, times(1)).filterByActiveOnly(false);
        verify(searchDefAccRepo, times(1)).findAll(
            ArgumentMatchers.<Specification<SearchDefendantAccountEntity>>any());
    }


    private AccountSearchDto emptyCriteria() {
        AccountSearchDto c = mock(AccountSearchDto.class);
        when(c.getBusinessUnitIds()).thenReturn(null);
        when(c.getActiveAccountsOnly()).thenReturn(null);
        when(c.getReferenceNumberDto()).thenReturn(null);
        when(c.getDefendant()).thenReturn(null);
        return c;
    }

}
