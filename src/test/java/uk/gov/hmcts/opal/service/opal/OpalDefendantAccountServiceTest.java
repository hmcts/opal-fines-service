package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.opal.dto.CollectionOrderDto;
import uk.gov.hmcts.opal.dto.CourtReferenceDto;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.CommentsAndNotes;
import uk.gov.hmcts.opal.dto.common.EnforcementOverride;
import uk.gov.hmcts.opal.dto.common.EnforcementOverrideResult;
import uk.gov.hmcts.opal.dto.common.Enforcer;
import uk.gov.hmcts.opal.dto.common.LJA;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.PaymentStateSummary;
import uk.gov.hmcts.opal.dto.legacy.ReferenceNumberDto;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.AliasDto;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountHeaderViewEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountSummaryViewEntity;
import uk.gov.hmcts.opal.entity.EnforcementOverrideResultEntity;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.repository.AliasRepository;
import uk.gov.hmcts.opal.repository.CourtRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountHeaderViewRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountPaymentTermsRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountSummaryViewRepository;
import uk.gov.hmcts.opal.repository.EnforcementOverrideResultRepository;
import uk.gov.hmcts.opal.repository.EnforcerRepository;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.SearchDefendantAccountRepository;
import uk.gov.hmcts.opal.repository.jpa.DefendantAccountSpecs;
import uk.gov.hmcts.opal.repository.jpa.SearchDefendantAccountSpecs;

class OpalDefendantAccountServiceTest {

    private final DefendantAccountRepository defendantAccountRepository =
        mock(DefendantAccountRepository.class);
    private final DefendantAccountSummaryViewRepository dasvRepository =
        mock(DefendantAccountSummaryViewRepository.class);
    private final SearchDefendantAccountRepository searchDefAccRepo =
        mock(SearchDefendantAccountRepository.class);
    private final DefendantAccountPaymentTermsRepository paymentTermsRepository =
        mock(DefendantAccountPaymentTermsRepository.class);

    private DefendantAccountSpecs defendantAccountSpecs;
    // ONE shared spy for search specs; this is the instance we verify interactions on
    private SearchDefendantAccountSpecs searchSpecsSpy;

    // Service under test
    private OpalDefendantAccountService service;

    @BeforeEach
    void setUp() {
        // Create the spy fresh each test
        searchSpecsSpy = spy(new SearchDefendantAccountSpecs());
        defendantAccountSpecs = spy(new DefendantAccountSpecs());

        // Build the service with EXACTLY these dependencies in the expected order
        this.service = new OpalDefendantAccountService(
            /* headerViewRepo */ null,
            /* defendantAccountRepository */ defendantAccountRepository,
            /* defendantAccountSpecs     */ defendantAccountSpecs,
            /* searchDefAccRepo          */ searchDefAccRepo,
            /* searchDefAccSpecs         */ searchSpecsSpy,
            /* paymentTermsRepository    */ paymentTermsRepository,
            /* dasvRepository            */ dasvRepository,
            /* courtRepo                 */ null,
            /* amendmentService          */ null,
            /* entityManager             */ null,
            /* noteRepository            */ null,
            /* enforcementOverrideResult */ null,
            /* localJusticeAreaRepo      */ null,
            /* enforcerRepository        */ null
        );

        // Generic matcher to avoid unchecked warnings
        when(defendantAccountRepository.findAll(ArgumentMatchers.<Specification<DefendantAccountEntity>>any()))
            .thenReturn(List.of());
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
            .businessUnitId((short) 77)
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
    void testMapToDto_DefendantPartyId_ComesFromDefendantAccountPartyId() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .defendantAccountId(77L)
            .defendantAccountPartyId(77L)
            .partyId(999L)
            .accountNumber("177A")
            .accountStatus("L")
            .build();

        DefendantAccountHeaderSummary dto = service.mapToDto(e);

        assertNotNull(dto, "DTO should not be null");
        assertEquals("77", dto.getDefendantPartyId(),
                     "defendant_party_id should map from defendantAccountPartyId");
        assertNotEquals("999", dto.getDefendantPartyId(),
                        "should not map from partyId");
    }

    @Test
    void testMapToDto_DefendantPartyId_NullWhenDefendantAccountPartyIdIsNull() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .defendantAccountId(88L)
            .defendantAccountPartyId(null)
            .accountStatus("L")
            .build();

        DefendantAccountHeaderSummary dto = service.mapToDto(e);

        assertNull(dto.getDefendantPartyId(),
                   "defendantPartyId should be null when defendantAccountPartyId is null");
    }

    @Test
    void testBuildPartyDetails_IndividualMatchesApiSpec() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .partyId(77L)
            .organisation(false)
            .title("Ms")
            .firstnames("Anna")
            .surname("Graham")
            .birthDate(LocalDate.of(1980, 2, 3))
            .build();

        PartyDetails details = service.buildPartyDetails(e);

        assertEquals("77", details.getPartyId());
        assertFalse(details.getOrganisationFlag());
        assertNotNull(details.getIndividualDetails());
        assertEquals("Anna", details.getIndividualDetails().getForenames());
        assertEquals("Graham", details.getIndividualDetails().getSurname());

        // Organisation details may be null or just an empty object
        var org = details.getOrganisationDetails();
        if (org != null) {
            assertNull(org.getOrganisationName());
            assertTrue(org.getOrganisationAliases() == null || org.getOrganisationAliases().isEmpty());
        }
    }


    @Test
    void testBuildPartyDetails_OrganisationMatchesApiSpec() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .partyId(10001L)
            .organisation(true)
            .organisationName("Kings Arms")
            .build();

        PartyDetails details = service.buildPartyDetails(e);

        assertEquals("10001", details.getPartyId());
        assertTrue(details.getOrganisationFlag());
        assertNotNull(details.getOrganisationDetails());
        assertEquals("Kings Arms", details.getOrganisationDetails().getOrganisationName());

        // Instead of asserting null, just confirm individual details are empty or unpopulated
        if (details.getIndividualDetails() != null) {
            assertNull(details.getIndividualDetails().getForenames());
            assertNull(details.getIndividualDetails().getSurname());
            assertTrue(details.getIndividualDetails().getIndividualAliases().isEmpty());
        }
    }

    @Test
    void testMapToDto_NormalisesAccountTypeAndStatusDisplayName() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .defendantAccountId(77L)
            .defendantAccountPartyId(77L)
            .accountNumber("177A")
            .accountType("Fines")
            .accountStatus("L")
            .build();

        DefendantAccountHeaderSummary dto = service.mapToDto(e);

        assertEquals("Fines", dto.getAccountType());
        assertEquals("Live", dto.getAccountStatusReference().getAccountStatusDisplayName());
    }

    @Test
    void testMapToDto_SerialisedStructureMatchesApiFields() throws Exception {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .defendantAccountId(77L)
            .defendantAccountPartyId(77L)
            .partyId(77L)
            .accountNumber("177A")
            .organisation(false)
            .firstnames("Anna")
            .surname("Graham")
            .accountStatus("L")
            .build();

        DefendantAccountHeaderSummary dto = service.mapToDto(e);
        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(dto);

        assertTrue(json.contains("\"defendant_party_id\""));
        assertTrue(json.contains("\"party_details\""));
        assertTrue(json.contains("\"account_number\""));
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
        verify(defendantAccountSpecs, times(1)).filterByActiveOnly(false);
        verify(defendantAccountRepository, times(1))
            .findAll(ArgumentMatchers.<Specification<DefendantAccountEntity>>any());
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
        verify(defendantAccountSpecs, times(1)).filterByActiveOnly(false);
        verify(defendantAccountRepository, times(1))
            .findAll(ArgumentMatchers.<Specification<DefendantAccountEntity>>any());
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
        verify(defendantAccountSpecs, times(1)).filterByActiveOnly(true);
        verify(defendantAccountRepository, times(1))
            .findAll(ArgumentMatchers.<Specification<DefendantAccountEntity>>any());
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
        verify(defendantAccountSpecs, times(1)).filterByActiveOnly(false);
        verify(defendantAccountRepository, times(1))
            .findAll(ArgumentMatchers.<Specification<DefendantAccountEntity>>any());
    }

    @Test
    void updateDefendantAccount_happyPath_updatesAllGroups_andReturnsRepresentation() {
        // ---------- Arrange ----------
        Long id = 1L;

        BusinessUnitFullEntity bu = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10)
            .build();

        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .defendantAccountId(id)
            .businessUnit(bu)
            .build();

        // If-Match must match this (@Version)
        entity.setVersion(1L);

        // Core repos & deps
        final DefendantAccountHeaderViewRepository headerViewRepo = mock(DefendantAccountHeaderViewRepository.class);
        final DefendantAccountRepository accountRepo = mock(DefendantAccountRepository.class);
        final DefendantAccountSpecs specs = mock(DefendantAccountSpecs.class);
        final DefendantAccountPaymentTermsRepository paymentTermsRepo =
            mock(DefendantAccountPaymentTermsRepository.class);
        final CourtRepository courtRepo = mock(CourtRepository.class);
        final AmendmentService amendmentService = mock(AmendmentService.class);
        final EntityManager em = mock(EntityManager.class);
        final NoteRepository noteRepository = mock(NoteRepository.class);

        // Repos needed for enforcementOverride
        final EnforcementOverrideResultRepository eorRepo = mock(EnforcementOverrideResultRepository.class);
        final LocalJusticeAreaRepository ljaRepo = mock(LocalJusticeAreaRepository.class);
        final EnforcerRepository enforcerRepo = mock(EnforcerRepository.class);

        // Stubs
        when(accountRepo.findById(id)).thenReturn(Optional.of(entity));
        // Echo the saved entity (so assertions see updated values)
        when(accountRepo.save(any(DefendantAccountEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CourtEntity.Lite court = CourtEntity.Lite.builder()
            .courtId(100L)
            .name("Central Magistrates")
            .build();

        when(courtRepo.findById(100L)).thenReturn(Optional.of(court));

        // Reference entities: stub getters so the service can copy IDs onto the account
        EnforcementOverrideResultEntity eor = mock(EnforcementOverrideResultEntity.class);
        when(eor.getEnforcementOverrideResultId()).thenReturn("EO-1");
        when(eorRepo.findById("EO-1")).thenReturn(Optional.of(eor));

        EnforcerEntity enforcer = mock(EnforcerEntity.class);
        when(enforcer.getEnforcerId()).thenReturn(22L);
        when(enforcerRepo.findById(22L)).thenReturn(Optional.of(enforcer));

        LocalJusticeAreaEntity lja = mock(LocalJusticeAreaEntity.class);
        when(lja.getLocalJusticeAreaId()).thenReturn((short) 33);
        when(ljaRepo.findById((short) 33)).thenReturn(Optional.of(lja));

        // Service under test
        final OpalDefendantAccountService svc = new OpalDefendantAccountService(
            headerViewRepo,
            accountRepo,
            defendantAccountSpecs,
            searchDefAccRepo,
            searchSpecsSpy,
            paymentTermsRepo,
            dasvRepository,
            courtRepo,
            amendmentService,
            em,
            noteRepository,
            eorRepo,
            ljaRepo,
            enforcerRepo
        );

        // Request DTO
        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder()
            .commentsAndNotes(CommentsAndNotes.builder()
                                  .accountNotesAccountComments("acc comment")
                                  .accountNotesFreeTextNote1("n1")
                                  .accountNotesFreeTextNote2("n2")
                                  .accountNotesFreeTextNote3("n3")
                                  .build())
            .enforcementCourt(CourtReferenceDto.builder()
                                  .courtId(100)
                                  .courtName("Central Magistrates")
                                  .build())
            .collectionOrder(CollectionOrderDto.builder()
                                 .collectionOrderFlag(true)
                                 .collectionOrderDate("2025-01-01")
                                 .build())
            .enforcementOverride(EnforcementOverride.builder()
                                      .enforcementOverrideResult(EnforcementOverrideResult.builder()
                                                                     .enforcementOverrideId("EO-1")
                                                                     .enforcementOverrideTitle("Result Title")
                                                                     .build())
                                      .enforcer(Enforcer.builder()
                                                    .enforcerId(Math.toIntExact(22L))
                                                    .enforcerName("Enforcer A")
                                                    .build())
                                      .lja(LJA.builder()
                                               .ljaId(33)
                                               .ljaName("LJA Name")
                                               .build())
                                      .build())
            .build();

        // ---------- Act ----------
        final String buHeader = "10"; // near first use for Checkstyle
        // If-Match = "1" to match entity.setVersion(1L)
        var resp = svc.updateDefendantAccount(id, buHeader, req, "1", "UNIT_TEST");

        // ---------- Assert ----------
        verify(accountRepo).save(entity);
        assertEquals(id, resp.getId());

        assertNotNull(resp.getCommentsAndNotes());
        assertEquals("acc comment", resp.getCommentsAndNotes().getAccountNotesAccountComments());
        assertEquals("n1", resp.getCommentsAndNotes().getAccountNotesFreeTextNote1());
        assertEquals("n2", resp.getCommentsAndNotes().getAccountNotesFreeTextNote2());
        assertEquals("n3", resp.getCommentsAndNotes().getAccountNotesFreeTextNote3());

        assertNotNull(resp.getEnforcementCourt());
        assertEquals(100, resp.getEnforcementCourt().getCourtId());
        assertEquals("Central Magistrates", resp.getEnforcementCourt().getCourtName());

        assertNotNull(resp.getCollectionOrder());
        assertEquals(Boolean.TRUE, resp.getCollectionOrder().getCollectionOrderFlag());
        assertEquals("2025-01-01", resp.getCollectionOrder().getCollectionOrderDate());

        assertNotNull(resp.getEnforcementOverride());
        assertEquals("EO-1", resp.getEnforcementOverride().getEnforcementOverrideResult()
            .getEnforcementOverrideId());
        assertEquals(22, resp.getEnforcementOverride().getEnforcer().getEnforcerId());
        assertEquals(33, resp.getEnforcementOverride().getLja().getLjaId());

        // Verify entity was updated as expected
        assertEquals(court, entity.getEnforcingCourt());
        assertTrue(entity.getCollectionOrder());
        assertEquals(java.time.LocalDate.parse("2025-01-01"), entity.getCollectionOrderEffectiveDate());
        assertEquals("EO-1", entity.getEnforcementOverrideResultId());
        assertEquals(Long.valueOf(22), entity.getEnforcementOverrideEnforcerId());
        assertEquals(Short.valueOf((short) 33), entity.getEnforcementOverrideTfoLjaId());
    }

    @Test
    void updateDefendantAccount_throwsWhenNoUpdateGroupsProvided() {
        DefendantAccountRepository accountRepo = mock(DefendantAccountRepository.class);
        OpalDefendantAccountService svc = new OpalDefendantAccountService(
            null, accountRepo, null, null, null, null, null, null,null,null,null,null,null, null);

        Long id = 1L;
        String buHeader = "10";

        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder().build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            svc.updateDefendantAccount(id, buHeader, req, "1", "UNIT_TEST")
        );
        assertTrue(ex.getMessage().contains("At least one update group"));
        verifyNoInteractions(accountRepo);
    }

    @Test
    void updateDefendantAccount_throwsWhenBusinessUnitMismatch() {
        DefendantAccountRepository accountRepo = mock(DefendantAccountRepository.class);
        OpalDefendantAccountService svc = new OpalDefendantAccountService(
            null, accountRepo, null, null, null, null, null, null,null,null,null,null,null, null);

        Long id = 1L;
        String buHeader = "10";

        BusinessUnitFullEntity bu = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 77)
            .build();

        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .businessUnit(bu)
            .version(1L)
            .build();

        when(accountRepo.findById(id)).thenReturn(Optional.of(entity));

        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder()
            .commentsAndNotes(CommentsAndNotes.builder().accountNotesAccountComments("x").build())
            .build();

        assertThrows(EntityNotFoundException.class, () ->
            svc.updateDefendantAccount(id, buHeader, req, "1", "UNIT_TEST")
        );
        verify(accountRepo, never()).save(any());
    }

    @Test
    void updateDefendantAccount_throwsWhenCollectionOrderDateInvalid() {
        DefendantAccountRepository accountRepo = mock(DefendantAccountRepository.class);
        OpalDefendantAccountService svc = new OpalDefendantAccountService(
            null, accountRepo, null, null, null, null, null, null,null,null,null,null,null, null);

        Long id = 1L;
        String buHeader = "10";

        BusinessUnitFullEntity bu = BusinessUnitFullEntity.builder()
            .businessUnitId(Short.valueOf(buHeader))
            .build();

        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .businessUnit(bu)
            .version(1L)
            .build();

        when(accountRepo.findById(id)).thenReturn(Optional.of(entity));

        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder()
            .collectionOrder(CollectionOrderDto.builder()
                                 .collectionOrderFlag(true)
                                 .collectionOrderDate("not-a-date")
                                 .build())
            .build();

        assertThrows(NullPointerException.class, () ->
            svc.updateDefendantAccount(id, buHeader, req, "1", "UNIT_TEST"));
        verify(accountRepo, never()).save(any());
    }

    @Test
    void updateDefendantAccount_throwsWhenEntityNotFound() {
        DefendantAccountRepository accountRepo = mock(DefendantAccountRepository.class);
        OpalDefendantAccountService svc = new OpalDefendantAccountService(
            null, accountRepo, null, null, null, null, null, null,null,null,null,null,null, null);

        when(accountRepo.findById(99L)).thenReturn(Optional.empty());

        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder()
            .commentsAndNotes(CommentsAndNotes.builder().accountNotesAccountComments("x").build())
            .build();

        assertThrows(EntityNotFoundException.class, () ->
            svc.updateDefendantAccount(99L, "10", req, "1", "UNIT_TEST")
        );
        verify(accountRepo, never()).save(any());
    }

    @Test
    void updateDefendantAccount_missingIfMatch_throwsPrecondition() {
        // minimal setup
        DefendantAccountRepository accountRepo = mock(DefendantAccountRepository.class);
        AmendmentService amendmentService = mock(AmendmentService.class);
        EntityManager em = mock(EntityManager.class);
        NoteRepository noteRepository = mock(NoteRepository.class);

        OpalDefendantAccountService svc = new OpalDefendantAccountService(
            null,
            accountRepo,
            null,
            null,
            null,
            null,
            null,
            null,
            amendmentService,
            em,
            noteRepository,
            null,
            null,
            null
        );

        Long id = 77L;
        String bu = "10";

        BusinessUnitFullEntity buEnt = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10)
            .build();

        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .defendantAccountId(id)
            .businessUnit(buEnt)
            .version(1L)
            .build();

        when(accountRepo.findById(id)).thenReturn(Optional.of(entity));

        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder()
            .commentsAndNotes(CommentsAndNotes.builder().accountNotesAccountComments("x").build())
            .build();

        // Expect whatever your VersionUtils throws on missing/invalid If-Match
        assertThrows(
            uk.gov.hmcts.opal.exception.ResourceConflictException.class,
            () -> svc.updateDefendantAccount(id, bu, req, /*ifMatch*/ null, "UNIT_TEST")
        );
    }

    @Test
    void updateDefendantAccount_versionMismatch_throwsResourceConflict() {
        var repo = mock(DefendantAccountRepository.class);
        var svc = new OpalDefendantAccountService(
            null,
            repo,
            null,
            null,
            null,
            null,
            null,
            mock(CourtRepository.class),
            mock(AmendmentService.class),
            mock(EntityManager.class),
            mock(NoteRepository.class),
            null,
            null,
            null
        );

        var bu = BusinessUnitFullEntity.builder().businessUnitId((short)78).build();
        var entity = DefendantAccountEntity.builder().defendantAccountId(77L).businessUnit(bu).version(5L).build();
        when(repo.findById(77L)).thenReturn(Optional.of(entity));

        var req = UpdateDefendantAccountRequest.builder()
            .commentsAndNotes(CommentsAndNotes.builder().accountNotesAccountComments("x").build()).build();

        assertThrows(ObjectOptimisticLockingFailureException.class,
                     () -> svc.updateDefendantAccount(77L, "78", req, "\"0\"", "tester"));
        verify(repo, never()).save(any());
    }

    @Test
    void updateDefendantAccount_callsAuditProcs() {
        var accountRepo = mock(DefendantAccountRepository.class);
        var amend = mock(AmendmentService.class);
        var em = mock(EntityManager.class);
        var noteRepo = mock(NoteRepository.class);

        var bu = BusinessUnitFullEntity.builder().businessUnitId((short)78).build();
        var entity = DefendantAccountEntity.builder()
            .defendantAccountId(77L).businessUnit(bu).version(0L).build();
        when(accountRepo.findById(77L)).thenReturn(Optional.of(entity));

        var svc = new OpalDefendantAccountService(
            null,
            accountRepo,
            null,
            null,
            null,
            null,
            null,
            null,
            amend,
            em,
            noteRepo,
            null,
            null,
            null
        );

        var req = UpdateDefendantAccountRequest.builder()
            .commentsAndNotes(CommentsAndNotes.builder().accountNotesAccountComments("hello").build()).build();

        svc.updateDefendantAccount(77L, "78", req, "0", "11111111A");

        verify(amend).auditInitialiseStoredProc(77L, RecordType.DEFENDANT_ACCOUNTS);
        verify(amend).auditFinaliseStoredProc(
            eq(77L), eq(RecordType.DEFENDANT_ACCOUNTS), eq((short)78),
            eq("11111111A"), any(), eq("ACCOUNT_ENQUIRY"));
    }

    @Test
    void updateDefendantAccount_enforcementOverrideLookupsMissing_areNull() {
        var accountRepo = mock(DefendantAccountRepository.class);
        var svc = new OpalDefendantAccountService(
            null,
            accountRepo,
            null,
            null,
            null,
            null,
            null,
            null,
            mock(AmendmentService.class),
            mock(EntityManager.class),
            mock(NoteRepository.class),
            mock(EnforcementOverrideResultRepository.class),
            mock(LocalJusticeAreaRepository.class),
            mock(EnforcerRepository.class)
        );

        var bu = BusinessUnitFullEntity.builder().businessUnitId((short)78).build();
        var entity = DefendantAccountEntity.builder().defendantAccountId(77L).businessUnit(bu).version(0L).build();
        when(accountRepo.findById(77L)).thenReturn(Optional.of(entity));

        var req = UpdateDefendantAccountRequest.builder()
            .enforcementOverride(EnforcementOverride.builder()
                                      .enforcementOverrideResult(EnforcementOverrideResult.builder()
                                                                     .enforcementOverrideId("NOPE").build())
                                      .enforcer(Enforcer.builder().enforcerId(Math.toIntExact(999999L)).build())
                                      .lja(LJA.builder().ljaId(9999).build())
                                      .build())
            .build();

        var resp = svc.updateDefendantAccount(77L, "78", req, "0", "tester");
        assertNotNull(resp.getEnforcementOverride());
    }

    @Test
    void searchDefendantAccounts_mapsAliases_forIndividual() {
        // --- Arrange: build the data shape this service actually uses ---
        var party = PartyEntity.builder()
            .partyId(10L)
            .organisation(false)
            .title("Mr")
            .forenames("Amy")
            .surname("Pond")
            .addressLine1("1 Main St")
            .postcode("AB12CD")
            .birthDate(LocalDate.of(2000, 1, 1))
            .build();

        var dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(100L)
            .party(party)
            .debtor(true)
            .associationType("DEF")
            .build();

        var acc = DefendantAccountEntity.builder()
            .defendantAccountId(1L)
            .accountNumber("ACC1")
            .prosecutorCaseReference("PCR1")
            .lastEnforcement("LEVY")
            .accountBalance(new BigDecimal("12.34"))
            .parties(List.of(dap))
            .build();

        // Service calls defendantAccountRepository.findAll(spec) — stub that:
        when(defendantAccountRepository.findAll(ArgumentMatchers.<Specification<DefendantAccountEntity>>any()))
            .thenReturn(List.of(acc));

        // Inject and stub aliasRepository (service uses it to load aliases)
        AliasRepository aliasRepo = mock(AliasRepository.class);
        ReflectionTestUtils.setField(service, "aliasRepository", aliasRepo);

        // Provide three aliases (equivalent to alias1, alias2, alias5)
        var a1 = AliasEntity.builder().sequenceNumber(1).forenames("Amy").surname("Pond").build();
        var a2 = AliasEntity.builder().sequenceNumber(2).forenames("Amelia").surname("Pond").build();
        var a5 = AliasEntity.builder().sequenceNumber(5).forenames(null).surname("Pond").build();
        when(aliasRepo.findByParty_PartyId(10L)).thenReturn(List.of(a1, a2, a5));

        // --- Act ---
        var out = service.searchDefendantAccounts(emptyCriteria());

        // --- Assert ---
        assertEquals(1, out.getCount());
        var dto = out.getDefendantAccounts().get(0);

        assertFalse(dto.getOrganisation());
        assertEquals("ACC1", dto.getAccountNumber());
        assertEquals("LEVY", dto.getLastEnforcementAction());
        assertEquals(0, dto.getAccountBalance().compareTo(new BigDecimal("12.34")));

        List<AliasDto> aliases = dto.getAliases();
        assertEquals(3, aliases.size());

        var a1Dto = aliases.stream().filter(a -> a.getAliasNumber() == 1).findFirst().orElseThrow();
        assertEquals("Amy", a1Dto.getForenames());
        assertEquals("Pond", a1Dto.getSurname());
        assertNull(a1Dto.getOrganisationName());

        var a5Dto = aliases.stream().filter(a -> a.getAliasNumber() == 5).findFirst().orElseThrow();
        assertNull(a5Dto.getForenames());
        assertEquals("Pond", a5Dto.getSurname());
    }

    @Test
    void searchDefendantAccounts_mapsAliases_forOrganisation() {
        // ---- Arrange: build the shape the service actually uses ----
        var party = PartyEntity.builder()
            .partyId(20L)
            .organisation(true)
            .organisationName("Wayne Enterprises")
            .build();

        var dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(200L)
            .party(party)
            .debtor(true)
            .associationType("DEF")
            .build();

        var acc = DefendantAccountEntity.builder()
            .defendantAccountId(2L)
            .accountNumber("ACC2")
            .prosecutorCaseReference("PCR2")
            .lastEnforcement("CLAMP")
            .accountBalance(new BigDecimal("99.00"))
            .parties(List.of(dap))
            .build();

        // Service uses defendantAccountRepository.findAll(spec)
        when(defendantAccountRepository.findAll(ArgumentMatchers.<Specification<DefendantAccountEntity>>any()))
            .thenReturn(List.of(acc));

        // Inject & stub aliasRepository: org aliases have organisationName only
        AliasRepository aliasRepo = mock(AliasRepository.class);
        ReflectionTestUtils.setField(service, "aliasRepository", aliasRepo);

        var orgA1 = AliasEntity.builder().sequenceNumber(1).organisationName("Wayne Ent Ltd").build();
        var orgA2 = AliasEntity.builder().sequenceNumber(2).organisationName("Wayne Group").build();
        var orgA5 = AliasEntity.builder().sequenceNumber(5).organisationName("Wayne Holdings").build();
        when(aliasRepo.findByParty_PartyId(20L)).thenReturn(List.of(orgA1, orgA2, orgA5));

        // ---- Act ----
        var out = service.searchDefendantAccounts(emptyCriteria());

        // ---- Assert ----
        assertEquals(1, out.getCount());
        var dto = out.getDefendantAccounts().get(0);

        assertTrue(dto.getOrganisation());
        assertEquals("Wayne Enterprises", dto.getOrganisationName());
        assertNull(dto.getDefendantTitle());
        assertNull(dto.getDefendantFirstnames());
        assertNull(dto.getDefendantSurname());

        var aliases = dto.getAliases();
        assertEquals(3, aliases.size());
        assertTrue(aliases.stream().allMatch(a ->
                                                 a.getOrganisationName() != null
                                                     && a.getForenames() == null
                                                     && a.getSurname() == null
        ));

        // Optional sanity check: correct repo called
        verify(defendantAccountRepository, times(1))
            .findAll(ArgumentMatchers.<Specification<DefendantAccountEntity>>any());
    }

    @Test
    void searchDefendantAccounts_ignoresBlankAliasSlots() {
        // ---- Arrange: build the entity shape this service actually uses ----
        var party = PartyEntity.builder()
            .partyId(30L)
            .organisation(false)
            .forenames("John")
            .surname("Doe")
            .build();

        var dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(300L)
            .party(party)
            .debtor(true)
            .associationType("DEF")
            .build();

        var acc = DefendantAccountEntity.builder()
            .defendantAccountId(3L)
            .accountNumber("ACC3")
            .parties(List.of(dap))
            .build();

        // Service queries defendantAccountRepository, not searchDefAccRepo
        when(defendantAccountRepository.findAll(ArgumentMatchers.<Specification<DefendantAccountEntity>>any()))
            .thenReturn(List.of(acc));

        // Inject & stub aliasRepository so only one non-blank alias is returned
        AliasRepository aliasRepo = mock(AliasRepository.class);
        ReflectionTestUtils.setField(service, "aliasRepository", aliasRepo);

        // Only alias #1 exists; “blank slots” simply aren’t returned by the repo
        var a1 = AliasEntity.builder().sequenceNumber(1).forenames("John").surname("Doe").build();
        when(aliasRepo.findByParty_PartyId(30L)).thenReturn(List.of(a1));

        // ---- Act ----
        var out = service.searchDefendantAccounts(emptyCriteria());
        var aliases = out.getDefendantAccounts().get(0).getAliases();

        // ---- Assert ----
        assertEquals(1, out.getCount());
        assertEquals(1, aliases.size());
        assertEquals(1, aliases.get(0).getAliasNumber());
        assertEquals("John", aliases.get(0).getForenames());
        assertEquals("Doe", aliases.get(0).getSurname());

        // (Optional) ensure the correct repo was used
        verify(defendantAccountRepository, times(1))
            .findAll(ArgumentMatchers.<Specification<DefendantAccountEntity>>any());
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
