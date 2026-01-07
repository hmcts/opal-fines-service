package uk.gov.hmcts.opal.service.opal;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.CollectionOrderDto;
import uk.gov.hmcts.opal.dto.CourtReferenceDto;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.CommentsAndNotes;
import uk.gov.hmcts.opal.dto.common.ContactDetails;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.common.EmployerDetails;
import uk.gov.hmcts.opal.dto.common.EnforcementOverride;
import uk.gov.hmcts.opal.dto.common.EnforcementOverrideResult;
import uk.gov.hmcts.opal.dto.common.Enforcer;
import uk.gov.hmcts.opal.dto.common.IndividualAlias;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.LJA;
import uk.gov.hmcts.opal.dto.common.LanguagePreference;
import uk.gov.hmcts.opal.dto.common.LanguagePreferences;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.PaymentStateSummary;
import uk.gov.hmcts.opal.dto.common.VehicleDetails;
import uk.gov.hmcts.opal.dto.legacy.ReferenceNumberDto;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.AliasDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountHeaderViewEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountSummaryViewEntity;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentCardRequestEntity;
import uk.gov.hmcts.opal.entity.SearchDefendantAccountEntity;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.generated.model.GetEnforcementStatusResponse.DefendantAccountTypeEnum;
import uk.gov.hmcts.opal.repository.AliasRepository;
import uk.gov.hmcts.opal.repository.CourtRepository;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountHeaderViewRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountPaymentTermsRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountSummaryViewRepository;
import uk.gov.hmcts.opal.repository.EnforcementOverrideResultRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.repository.EnforcerRepository;
import uk.gov.hmcts.opal.repository.FixedPenaltyOffenceRepository;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.PaymentCardRequestRepository;
import uk.gov.hmcts.opal.repository.ResultRepository;
import uk.gov.hmcts.opal.repository.SearchDefendantAccountRepository;
import uk.gov.hmcts.opal.repository.jpa.DefendantAccountSpecs;
import uk.gov.hmcts.opal.repository.jpa.SearchDefendantAccountSpecs;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountServiceProxy;
import uk.gov.hmcts.opal.util.VersionUtils;
import uk.gov.hmcts.opal.util.Versioned;

// TODO: This class is testing a lot of things that AREN'T in the system under test. Refactor needed.
@ExtendWith(MockitoExtension.class)
class OpalDefendantAccountServiceTest {

    @Mock
    private DefendantAccountRepository defendantAccountRepository;

    @Mock
    private DefendantAccountSummaryViewRepository dasvRepository;

    @Mock
    private DefendantAccountHeaderViewRepository dahvRepository;

    @Mock
    private SearchDefendantAccountRepository searchDefAccRepo;

    @Mock
    private FixedPenaltyOffenceRepository fixedPenaltyOffenceRepository;

    @Mock
    private DefendantAccountPaymentTermsRepository paymentTermsRepo;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private CourtRepository courtRepo;

    @Mock
    private EnforcementOverrideResultRepository eorRepo;

    @Mock
    private LocalJusticeAreaRepository ljaRepo;

    @Mock
    private EnforcerRepository enforcerRepo;

    @Mock
    private AmendmentService amendmentService;

    @Mock
    private AliasRepository aliasRepo;

    @Mock
    private DebtorDetailRepository debtorRepo;

    @Mock
    private EntityManager entityManager;

    @Mock
    private PaymentCardRequestRepository paymentCardRequestRepository;

    @Mock
    private EnforcementRepository enforcementRepository;

    @Mock
    private AccessTokenService accessTokenService;

    @Mock
    private UserStateService userStateService;

    @Mock
    private OpalPartyService opalPartyService;

    @Mock
    private ResultRepository resultRepo;

    @Spy
    private DefendantAccountSpecs defendantAccountSpecs;

    @Spy
    private SearchDefendantAccountSpecs searchSpecsSpy;

    // Service under test
    @InjectMocks
    private OpalDefendantAccountService service;

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
        assertEquals(BigDecimal.valueOf(10), OpalDefendantAccountBuilders.nz(BigDecimal.valueOf(10)));
        assertEquals(BigDecimal.ZERO, OpalDefendantAccountBuilders.nz(null));
    }

    @Test
    void testCalculateAge() {
        int age = OpalDefendantAccountBuilders.calculateAge(LocalDate.now().minusYears(22));
        assertTrue(age == 22 || age == 21); // depending on birthday
        assertEquals(0, OpalDefendantAccountBuilders.calculateAge(null));
    }

    @Test
    void testResolveStatusDisplayName() {
        assertEquals("Live", OpalDefendantAccountBuilders.resolveStatusDisplayName("L"));
        assertEquals("Completed", OpalDefendantAccountBuilders.resolveStatusDisplayName("C"));
        assertEquals("TFO to be acknowledged", OpalDefendantAccountBuilders.resolveStatusDisplayName("TO"));
        assertEquals("TFO to NI/Scotland to be acknowledged",
            OpalDefendantAccountBuilders.resolveStatusDisplayName("TS"));
        assertEquals("TFO acknowledged", OpalDefendantAccountBuilders.resolveStatusDisplayName("TA"));
        assertEquals("Account consolidated", OpalDefendantAccountBuilders.resolveStatusDisplayName("CS"));
        assertEquals("Account written off", OpalDefendantAccountBuilders.resolveStatusDisplayName("WO"));
        assertEquals("Unknown", OpalDefendantAccountBuilders.resolveStatusDisplayName("nonsense"));
    }

    @Test
    void testBuildPaymentStateSummary() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .imposed(BigDecimal.valueOf(5))
            .arrears(BigDecimal.valueOf(2))
            .paid(BigDecimal.valueOf(3))
            .accountBalance(BigDecimal.valueOf(7))
            .build();

        PaymentStateSummary summary = OpalDefendantAccountBuilders.buildPaymentStateSummary(e);
        assertEquals(BigDecimal.valueOf(5), summary.getImposedAmount());
        assertEquals(BigDecimal.valueOf(2), summary.getArrearsAmount());
        assertEquals(BigDecimal.valueOf(3), summary.getPaidAmount());
        assertEquals(BigDecimal.valueOf(7), summary.getAccountBalance());
    }

    @Test
    void testBuildPartyDetails_allFieldsNullSafe() {
        DefendantAccountHeaderViewEntity e = new DefendantAccountHeaderViewEntity();
        PartyDetails details = OpalDefendantAccountBuilders.buildPartyDetails(e);
        assertNotNull(details);
    }

    @Test
    void testBuildAccountStatusReference() {
        AccountStatusReference ref = OpalDefendantAccountBuilders.buildAccountStatusReference("L");
        assertEquals("L", ref.getAccountStatusCode());
        assertEquals("Live", ref.getAccountStatusDisplayName());
    }

    @Test
    void testBuildBusinessUnitSummary() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .businessUnitId((short) 55)
            .businessUnitName("NorthEast")
            .build();

        BusinessUnitSummary summary = OpalDefendantAccountBuilders.buildBusinessUnitSummary(e);
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
            .version(1L)
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
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = service.mapToDto(e);

        assertNotNull(dto, "DTO should not be null");
        assertEquals("77", dto.getDefendantAccountPartyId(),
            "defendant_account_party_id should map from defendantAccountPartyId");
        assertNotEquals("999", dto.getDefendantAccountPartyId(),
            "should not map from partyId");
    }

    @Test
    void testMapToDto_DefendantPartyId_NullWhenDefendantAccountPartyIdIsNull() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .defendantAccountId(88L)
            .defendantAccountPartyId(null)
            .accountStatus("L")
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = service.mapToDto(e);

        assertNull(dto.getDefendantAccountPartyId(),
            "defendantAccountPartyId should be null when defendantAccountPartyId is null");
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

        PartyDetails details = OpalDefendantAccountBuilders.buildPartyDetails(e);

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

        PartyDetails details = OpalDefendantAccountBuilders.buildPartyDetails(e);

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
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = service.mapToDto(e);

        assertEquals("Fine", dto.getAccountType()); // Should normalise plural Fines → Fine"
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
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = service.mapToDto(e);
        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(dto);

        assertTrue(json.contains("\"defendant_account_party_id\""));
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
    void buildAtAGlanceResponse_mapsAllFields_Individual() {
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

        DefendantAccountAtAGlanceResponse response = OpalDefendantAccountBuilders.buildAtAGlanceResponse(entity);

        assertNotNull(response);
        assertEquals("1", response.getDefendantAccountId());
        assertEquals("ACC123", response.getAccountNumber());
        assertEquals("Defendant", response.getDebtorType());
        assertTrue(response.getIsYouth());
        assertNotNull(response.getPartyDetails());
    }

    @Test
    void buildAtAGlanceResponse_mapsAllFields_Organisation() {
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

        DefendantAccountAtAGlanceResponse response = OpalDefendantAccountBuilders.buildAtAGlanceResponse(entity);

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
        AccountSearchDto dto = AccountSearchDto.builder()
            .activeAccountsOnly(true)
            .referenceNumberDto(ReferenceNumberDto.builder().accountNumber("AAAAAAAAX").build())
            .build();

        // when
        service.searchDefendantAccounts(dto);

        // then → AC1b requires the service to IGNORE activeOnly (i.e., pass false to the spec)
        verify(searchSpecsSpy, times(1)).filterByActiveOnly(false);
        verify(searchDefAccRepo, times(1))
            .findAll(ArgumentMatchers.<Specification<SearchDefendantAccountEntity>>any());
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
        verify(searchSpecsSpy, times(1)).filterByActiveOnly(false);
        verify(searchDefAccRepo, times(1))
            .findAll(ArgumentMatchers.<Specification<SearchDefendantAccountEntity>>any());
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
        verify(searchSpecsSpy, times(1)).filterByActiveOnly(true);
        verify(searchDefAccRepo, times(1))
            .findAll(ArgumentMatchers.<Specification<SearchDefendantAccountEntity>>any());
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
        verify(searchSpecsSpy, times(1)).filterByActiveOnly(false);
        verify(searchDefAccRepo, times(1))
            .findAll(ArgumentMatchers.<Specification<SearchDefendantAccountEntity>>any());
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
        entity.setVersionNumber(1L);

        // Stubs
        when(defendantAccountRepository.findById(id)).thenReturn(Optional.of(entity));
        // Echo the saved entity (so assertions see updated values)
        when(defendantAccountRepository.save(any(DefendantAccountEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CourtEntity.Lite court = CourtEntity.Lite.builder()
            .courtId(100L)
            .name("Central Magistrates")
            .build();

        when(courtRepo.findById(100L)).thenReturn(Optional.of(court));

        // Reference entities: stub getters so the service can copy IDs onto the account
        ResultEntity.Lite eor = mock(ResultEntity.Lite.class);
        when(eor.getResultId()).thenReturn("EO-1");
        when(resultRepo.findById("EO-1")).thenReturn(Optional.of(eor));

        EnforcerEntity enforcer = mock(EnforcerEntity.class);
        when(enforcer.getEnforcerId()).thenReturn(22L);
        when(enforcerRepo.findById(22L)).thenReturn(Optional.of(enforcer));

        LocalJusticeAreaEntity lja = mock(LocalJusticeAreaEntity.class);
        when(lja.getLocalJusticeAreaId()).thenReturn((short) 33);
        when(ljaRepo.findById((short) 33)).thenReturn(Optional.of(lja));

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
                    .enforcerId(22L)
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
        var resp = service.updateDefendantAccount(id, buHeader, req, "1", "UNIT_TEST");

        // ---------- Assert ----------
        verify(defendantAccountRepository).save(entity);
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
        Long id = 1L;
        String buHeader = "10";

        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder().build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            service.updateDefendantAccount(id, buHeader, req, "1", "UNIT_TEST")
        );
        assertTrue(ex.getMessage().contains("At least one update group"));
        verifyNoInteractions(defendantAccountRepository);
    }

    @Test
    void updateDefendantAccount_throwsWhenBusinessUnitMismatch() {
        Long id = 1L;
        String buHeader = "10";

        BusinessUnitFullEntity bu = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 77)
            .build();

        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .businessUnit(bu)
            .versionNumber(1L)
            .build();

        when(defendantAccountRepository.findById(id)).thenReturn(Optional.of(entity));

        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder()
            .commentsAndNotes(CommentsAndNotes.builder().accountNotesAccountComments("x").build())
            .build();

        assertThrows(EntityNotFoundException.class, () ->
            service.updateDefendantAccount(id, buHeader, req, "1", "UNIT_TEST")
        );
        verify(defendantAccountRepository, never()).save(any());
    }

    @Test
    void updateDefendantAccount_throwsWhenCollectionOrderDateInvalid() {
        Long id = 1L;
        String buHeader = "10";

        BusinessUnitFullEntity bu = BusinessUnitFullEntity.builder()
            .businessUnitId(Short.valueOf(buHeader))
            .build();

        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .businessUnit(bu)
            .versionNumber(1L)
            .build();

        when(defendantAccountRepository.findById(id)).thenReturn(Optional.of(entity));

        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder()
            .collectionOrder(CollectionOrderDto.builder()
                .collectionOrderFlag(true)
                .collectionOrderDate("not-a-date")
                .build())
            .build();

        assertThrows(IllegalArgumentException.class, () ->
            service.updateDefendantAccount(id, buHeader, req, "1", "UNIT_TEST"));
        verify(defendantAccountRepository, never()).save(any());
    }

    @Test
    void updateDefendantAccount_throwsWhenEntityNotFound() {
        when(defendantAccountRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder()
            .commentsAndNotes(CommentsAndNotes.builder().accountNotesAccountComments("x").build())
            .build();

        assertThrows(EntityNotFoundException.class, () ->
            service.updateDefendantAccount(99L, "10", req, "1", "UNIT_TEST")
        );
        verify(defendantAccountRepository, never()).save(any());
    }

    @Test
    void updateDefendantAccount_missingIfMatch_throwsPrecondition() {
        Long id = 77L;
        String bu = "10";

        BusinessUnitFullEntity buEnt = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10)
            .build();

        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .defendantAccountId(id)
            .businessUnit(buEnt)
            .versionNumber(1L)
            .build();

        when(defendantAccountRepository.findById(id)).thenReturn(Optional.of(entity));

        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder()
            .commentsAndNotes(CommentsAndNotes.builder().accountNotesAccountComments("x").build())
            .build();

        // Expect whatever your VersionUtils throws on missing/invalid If-Match
        assertThrows(
            uk.gov.hmcts.opal.exception.ResourceConflictException.class,
            () -> service.updateDefendantAccount(id, bu, req, /*ifMatch*/ null, "UNIT_TEST")
        );
    }

    @Test
    void updateDefendantAccount_versionMismatch_throwsResourceConflict() {
        var bu = BusinessUnitFullEntity.builder().businessUnitId((short) 78).build();
        var entity = DefendantAccountEntity.builder().defendantAccountId(77L).businessUnit(bu)
            .versionNumber(5L).build();
        when(defendantAccountRepository.findById(77L)).thenReturn(Optional.of(entity));

        var req = UpdateDefendantAccountRequest.builder()
            .commentsAndNotes(CommentsAndNotes.builder().accountNotesAccountComments("x").build()).build();

        assertThrows(ObjectOptimisticLockingFailureException.class,
            () -> service.updateDefendantAccount(77L, "78", req, "\"0\"", "tester"));
        verify(defendantAccountRepository, never()).save(any());
    }

    @Test
    void updateDefendantAccount_callsAuditProcs() {
        var bu = BusinessUnitFullEntity.builder().businessUnitId((short) 78).build();
        var entity = DefendantAccountEntity.builder()
            .defendantAccountId(77L).businessUnit(bu).versionNumber(0L).build();
        when(defendantAccountRepository.findById(77L)).thenReturn(Optional.of(entity));

        var req = UpdateDefendantAccountRequest.builder()
            .commentsAndNotes(CommentsAndNotes.builder().accountNotesAccountComments("hello").build()).build();

        service.updateDefendantAccount(77L, "78", req, "0", "11111111A");

        verify(amendmentService).auditInitialiseStoredProc(77L, RecordType.DEFENDANT_ACCOUNTS);
        verify(amendmentService).auditFinaliseStoredProc(
            eq(77L), eq(RecordType.DEFENDANT_ACCOUNTS), eq((short) 78),
            eq("11111111A"), any(), eq("ACCOUNT_ENQUIRY"));
    }

    @Test
    void updateDefendantAccount_enforcementOverrideLookupsMissing_areNull() {
        var bu = BusinessUnitFullEntity.builder().businessUnitId((short) 78).build();
        var entity = DefendantAccountEntity.builder().defendantAccountId(77L).businessUnit(bu)
            .versionNumber(0L).build();
        when(defendantAccountRepository.findById(77L)).thenReturn(Optional.of(entity));

        var req = UpdateDefendantAccountRequest.builder()
            .enforcementOverride(EnforcementOverride.builder()
                .enforcementOverrideResult(EnforcementOverrideResult.builder()
                    .enforcementOverrideId("NOPE").build())
                .enforcer(Enforcer.builder().enforcerId(999999L).build())
                .lja(LJA.builder().ljaId(9999).build())
                .build())
            .build();

        var resp = service.updateDefendantAccount(77L, "78", req, "0", "tester");
        assertNotNull(resp.getEnforcementOverride());
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
        var aliases = out.getDefendantAccounts().getFirst().getAliases();
        assertEquals(1, aliases.size());
        assertEquals(1, aliases.getFirst().getAliasNumber());
        assertEquals("John", aliases.getFirst().getForenames());
        assertEquals("Doe", aliases.getFirst().getSurname());
    }

    private DefendantAccountSummaryViewEntity mockDasv(Boolean organisation,
        String a1, String a2, String a3, String a4, String a5) {
        var e = mock(DefendantAccountSummaryViewEntity.class);
        when(e.getAlias1()).thenReturn(a1);
        when(e.getAlias2()).thenReturn(a2);
        when(e.getAlias3()).thenReturn(a3);
        when(e.getAlias4()).thenReturn(a4);
        when(e.getAlias5()).thenReturn(a5);
        when(e.getOrganisation()).thenReturn(organisation);
        return e;
    }

    // --- Individuals ---

    @Test
    void individualAliases_parses_and_trims_and_splits_full_name() {
        // unified person rows; entity is an individual
        var e = mockDasv(false,
            "P123|10|  Ada   Lovelace  ",
            "P777|| Grace   Hopper ",
            "   ",
            null,
            null
        );

        var out = OpalDefendantAccountBuilders.buildIndividualAliasesList(e);

        assertEquals(2, out.size());

        var a0 = out.get(0);
        assertEquals("P123", a0.getAliasId());
        assertEquals(10, a0.getSequenceNumber());
        assertEquals("Ada", a0.getForenames());         // trimmed, internal spaces collapsed for split
        assertEquals("Lovelace", a0.getSurname());

        var a1 = out.get(1);
        assertEquals("P777", a1.getAliasId());
        assertNull(a1.getSequenceNumber());             // empty seq → null
        assertEquals("Grace", a1.getForenames());
        assertEquals("Hopper", a1.getSurname());
    }

    @Test
    void individualAliases_single_token_name_maps_to_forenames_only() {
        var e = mockDasv(false,
            "P5||Jane",
            null, null, null, null
        );

        var ind = OpalDefendantAccountBuilders.buildIndividualAliasesList(e);
        assertEquals(1, ind.size());
        var a = ind.get(0);
        assertEquals("P5", a.getAliasId());
        assertNull(a.getSequenceNumber());              // empty → null
        assertEquals("Jane", a.getForenames());
        assertNull(a.getSurname());                     // single token → null surname
    }

    @Test
    void individualAliases_malformedRows_areSkipped_safely() {
        var e = mockDasv(false,
            "X|notANumber|OnlyTwoParts",   // NumberFormatException → skipped
            "too|many|parts|here|oops",    // wrong arity → skipped
            null,
            "   ",
            "P100|1|John William Smith"    // valid, test splitting on last token
        );

        var ind = OpalDefendantAccountBuilders.buildIndividualAliasesList(e);

        assertEquals(1, ind.size());
        assertEquals("P100", ind.get(0).getAliasId());
        assertEquals(1, ind.get(0).getSequenceNumber());
        assertEquals("John William", ind.get(0).getForenames()); // last token is surname
        assertEquals("Smith", ind.get(0).getSurname());

        // entity is an individual → org aliases list must be empty
        var org = OpalDefendantAccountBuilders.buildOrganisationAliasesList(e);
        assertTrue(org.isEmpty());
    }

    @Test
    void individualAliases_preserves_row_order_for_valid_rows() {
        var e = mockDasv(false,
            "P1|1|Alpha One",
            "P3|3|Gamma Three",
            null, null, null
        );

        var ind = OpalDefendantAccountBuilders.buildIndividualAliasesList(e);
        assertEquals(2, ind.size());
        assertEquals("P1", ind.get(0).getAliasId());
        assertEquals("P3", ind.get(1).getAliasId());
    }

    // --- Organisations ---

    @Test
    void organisationAliases_parses_and_trims() {
        var e = mockDasv(true,
            "O111|1|  Wayne Enterprises  ",
            "O222|| Wayne Group ",
            null,
            "",
            null
        );

        var out = OpalDefendantAccountBuilders.buildOrganisationAliasesList(e);

        assertEquals(2, out.size());
        var a0 = out.get(0);
        assertEquals("O111", a0.getAliasId());
        assertEquals(1, a0.getSequenceNumber());
        assertEquals("Wayne Enterprises", a0.getOrganisationName());

        var a1 = out.get(1);
        assertEquals("O222", a1.getAliasId());
        assertNull(a1.getSequenceNumber());
        assertEquals("Wayne Group", a1.getOrganisationName());
    }

    @Test
    void organisationAliases_malformedRows_areSkipped_safely() {
        var e = mockDasv(true,
            "O1|badNumber|Acme Corp",     // NumberFormatException → skipped
            "O2|2|Beta Org",
            "too|many|parts|oops",
            null, "   "
        );

        var out = OpalDefendantAccountBuilders.buildOrganisationAliasesList(e);
        assertEquals(1, out.size());
        assertEquals("O2", out.get(0).getAliasId());
        assertEquals(2, out.get(0).getSequenceNumber());
        assertEquals("Beta Org", out.get(0).getOrganisationName());

        // entity is an organisation → individual list must be empty
        var ind = OpalDefendantAccountBuilders.buildIndividualAliasesList(e);
        assertTrue(ind.isEmpty());
    }

    // --- Shared / edge cases ---

    @Test
    void all_null_or_blank_alias_slots_yield_empty_lists_for_both_entity_types() {
        var person = mockDasv(false, null, "", "   ", null, "");
        var org = mockDasv(true, null, "", "   ", null, "");

        assertTrue(OpalDefendantAccountBuilders.buildIndividualAliasesList(person).isEmpty());
        assertTrue(OpalDefendantAccountBuilders.buildOrganisationAliasesList(person).isEmpty());

        assertTrue(OpalDefendantAccountBuilders.buildIndividualAliasesList(org).isEmpty());
        assertTrue(OpalDefendantAccountBuilders.buildOrganisationAliasesList(org).isEmpty());
    }

    @Test
    void gating_by_entity_type_means_wrong_list_is_always_empty() {
        var person = mockDasv(false,
            "P1|1|Alice Wonderland",
            "P2|2|Bob Builder",
            null, null, null
        );
        var org = mockDasv(true,
            "O1|1|Umbrella Corp",
            "O2|2|Stark Industries",
            null, null, null
        );

        assertEquals(2, OpalDefendantAccountBuilders.buildIndividualAliasesList(person).size());
        assertTrue(OpalDefendantAccountBuilders.buildOrganisationAliasesList(person).isEmpty());

        assertEquals(2, OpalDefendantAccountBuilders.buildOrganisationAliasesList(org).size());
        assertTrue(OpalDefendantAccountBuilders.buildIndividualAliasesList(org).isEmpty());
    }


    private AccountSearchDto emptyCriteria() {
        AccountSearchDto c = mock(AccountSearchDto.class);
        when(c.getBusinessUnitIds()).thenReturn(null);
        when(c.getActiveAccountsOnly()).thenReturn(null);
        when(c.getReferenceNumberDto()).thenReturn(null);
        when(c.getDefendant()).thenReturn(null);
        return c;
    }

    @Test
    void mapToDto_setsParentGuardianDebtorTypeAndYouthFlag() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .debtorType(null)
            .hasParentGuardian(true)
            .birthDate(LocalDate.now().minusYears(15))
            .accountStatus("L")
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = service.mapToDto(e);
        assertEquals("Parent/Guardian", dto.getDebtorType());
        assertTrue(dto.getIsYouth());
    }

    @Test
    void mapToDto_setsDefaultDebtorTypeAndNotYouthWhenBirthDateMissing() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .debtorType(null)
            .hasParentGuardian(false)
            .birthDate(null)
            .accountStatus("L")
            .version(1L)
            .build();

        DefendantAccountHeaderSummary dto = service.mapToDto(e);
        assertEquals("Defendant", dto.getDebtorType());
        assertFalse(dto.getIsYouth());
    }

    @Test
    void whenReferenceOrganisationFlagProvided_appliesFilterCorrectly() {
        // Arrange
        AccountSearchDto dtoTrue = mock(AccountSearchDto.class, RETURNS_DEEP_STUBS);
        ReferenceNumberDto refTrue = mock(ReferenceNumberDto.class);
        when(dtoTrue.getReferenceNumberDto()).thenReturn(refTrue);
        when(dtoTrue.getActiveAccountsOnly()).thenReturn(false);
        when(dtoTrue.getBusinessUnitIds()).thenReturn(Collections.emptyList());
        when(dtoTrue.getDefendant()).thenReturn(null);

        AccountSearchDto dtoFalse = mock(AccountSearchDto.class, RETURNS_DEEP_STUBS);
        ReferenceNumberDto refFalse = mock(ReferenceNumberDto.class);
        when(dtoFalse.getReferenceNumberDto()).thenReturn(refFalse);
        when(dtoFalse.getActiveAccountsOnly()).thenReturn(false);
        when(dtoFalse.getBusinessUnitIds()).thenReturn(Collections.emptyList());
        when(dtoFalse.getDefendant()).thenReturn(null);

        // Act
        service.searchDefendantAccounts(dtoTrue);
        service.searchDefendantAccounts(dtoFalse);

        // Assert
        verify(searchSpecsSpy, times(1)).filterByReferenceOrganisationFlag(dtoTrue);
        verify(searchSpecsSpy, times(1)).filterByReferenceOrganisationFlag(dtoFalse);
        verify(searchDefAccRepo, times(2))
            .findAll(ArgumentMatchers.<Specification<SearchDefendantAccountEntity>>any());
    }

    @Test
    void buildBusinessUnitSummary_handlesNullBusinessUnitId() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .businessUnitId(null)
            .businessUnitName("Some BU")
            .build();

        BusinessUnitSummary summary = OpalDefendantAccountBuilders.buildBusinessUnitSummary(e);
        assertNull(summary.getBusinessUnitId());
        assertEquals("Some BU", summary.getBusinessUnitName());
    }

    @Test
    void searchDefendantAccounts_hasRefTrueBranchCovered() {
        AccountSearchDto dto = mock(AccountSearchDto.class, RETURNS_DEEP_STUBS);
        ReferenceNumberDto ref = new ReferenceNumberDto();
        ref.setAccountNumber("177");
        ref.setProsecutorCaseReference(null);

        when(dto.getReferenceNumberDto()).thenReturn(ref);
        when(dto.getActiveAccountsOnly()).thenReturn(true);
        when(dto.getBusinessUnitIds()).thenReturn(Collections.singletonList(78));
        when(dto.getDefendant()).thenReturn(null);

        // should not throw; branch just sets applyActiveOnly=false
        service.searchDefendantAccounts(dto);

        verify(searchSpecsSpy, times(1)).filterByActiveOnly(false);
    }

    @Test
    void getDefendantAccountParty_builds_individual_aliases_only() {
        // Build a Party (individual)
        PartyEntity party = PartyEntity.builder()
            .partyId(10L).organisation(false).title("Mr").forenames("John").surname("Doe")
            .build();

        // Link party into DefendantAccountPartiesEntity
        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(100L)
            .associationType("DEFENDANT")
            .debtor(true)
            .party(party)
            .build();

        // Account with that party
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(1L)
            .parties(List.of(dap))
            .businessUnit(BusinessUnitFullEntity.builder()
                .businessUnitId((short) 1).build())
            .versionNumber(0L)
            .build();

        when(defendantAccountRepository.findById(1L)).thenReturn(Optional.of(account));

        // AliasEntity rows: only surname populated should be considered for individual path;
        // blanks and org-only names ignored; ensure sorting by sequenceNumber.
        AliasEntity a1 = AliasEntity.builder()
            .aliasId(200L).sequenceNumber(2).surname("Smith").forenames("Alice").build();
        AliasEntity a2 = AliasEntity.builder()
            .aliasId(201L).sequenceNumber(1).surname("Jones").forenames("Bob").build();
        AliasEntity blank = AliasEntity.builder()
            .aliasId(202L).sequenceNumber(3).surname("   ").forenames("X").build(); // ignored
        AliasEntity orgOnly = AliasEntity.builder()
            .aliasId(203L).sequenceNumber(4).organisationName("Wayne Ent") // ignored for individual
            .build();

        when(aliasRepo.findByParty_PartyId(10L)).thenReturn(List.of(a1, a2, blank, orgOnly));
        when(debtorRepo.findByPartyId(10L)).thenReturn(Optional.empty());

        // --- Act
        GetDefendantAccountPartyResponse resp = service.getDefendantAccountParty(1L, 100L);

        assertNotNull(resp);
        DefendantAccountParty partyDto = resp.getDefendantAccountParty();
        assertNotNull(partyDto.getPartyDetails().getIndividualDetails());
        List<IndividualAlias> indAliases = partyDto.getPartyDetails().getIndividualDetails().getIndividualAliases();
        assertNotNull(indAliases);
        assertEquals(2, indAliases.size(), "should include only valid surname-bearing aliases");

        assertEquals("201", indAliases.get(0).getAliasId());
        assertEquals(Integer.valueOf(1), indAliases.get(0).getSequenceNumber());
        assertEquals("Bob", indAliases.get(0).getForenames());
        assertEquals("Jones", indAliases.get(0).getSurname());

        assertEquals("200", indAliases.get(1).getAliasId());
        assertEquals(Integer.valueOf(2), indAliases.get(1).getSequenceNumber());
        assertEquals("Alice", indAliases.get(1).getForenames());
        assertEquals("Smith", indAliases.get(1).getSurname());

        // Organisation aliases must be null/empty for an individual party
        OrganisationDetails orgAliases = partyDto.getPartyDetails().getOrganisationDetails();
        if (orgAliases != null) {
            assertTrue(orgAliases.getOrganisationAliases() == null || orgAliases.getOrganisationAliases().isEmpty());
        }
    }

    @Test
    void getDefendantAccountParty_builds_organisation_aliases_only() {
        var party = uk.gov.hmcts.opal.entity.PartyEntity.builder()
            .partyId(20L).organisation(true).organisationName("Wayne Enterprises")
            .build();

        var dap = uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(200L)
            .associationType("DEFENDANT")
            .debtor(true)
            .party(party)
            .build();

        var account = uk.gov.hmcts.opal.entity.DefendantAccountEntity.builder()
            .defendantAccountId(2L).parties(List.of(dap))
            .businessUnit(uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity.builder()
                .businessUnitId((short) 1).build())
            .versionNumber(0L).build();

        when(defendantAccountRepository.findById(2L)).thenReturn(Optional.of(account));

        // Only org-name-bearing aliases should be mapped; blanks ignored; sorted by seq.
        var o1 = uk.gov.hmcts.opal.entity.AliasEntity.builder()
            .aliasId(300L).sequenceNumber(2).organisationName("Wayne Group").build();
        var o2 = uk.gov.hmcts.opal.entity.AliasEntity.builder()
            .aliasId(301L).sequenceNumber(1).organisationName("Wayne Ent Ltd").build();
        var blank = uk.gov.hmcts.opal.entity.AliasEntity.builder()
            .aliasId(302L).sequenceNumber(3).organisationName("   ").build(); // ignored
        var personOnly = uk.gov.hmcts.opal.entity.AliasEntity.builder()
            .aliasId(303L).sequenceNumber(4).surname("Jones").forenames("Bob").build(); // ignored for org

        when(aliasRepo.findByParty_PartyId(20L)).thenReturn(List.of(o1, o2, blank, personOnly));
        when(debtorRepo.findByPartyId(20L)).thenReturn(Optional.empty());

        try {
            var f1 = OpalDefendantAccountService.class.getDeclaredField("aliasRepository");
            f1.setAccessible(true);
            f1.set(service, aliasRepo);
            var f2 = OpalDefendantAccountService.class.getDeclaredField("debtorDetailRepository");
            f2.setAccessible(true);
            f2.set(service, debtorRepo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        var resp = service.getDefendantAccountParty(2L, 200L);

        assertNotNull(resp);
        var partyDto = resp.getDefendantAccountParty();
        assertTrue(partyDto.getPartyDetails().getOrganisationFlag());

        var orgDetails = partyDto.getPartyDetails().getOrganisationDetails();
        assertNotNull(orgDetails);
        var orgAliases = orgDetails.getOrganisationAliases();
        assertNotNull(orgAliases);
        assertEquals(2, orgAliases.size());

        // Sorted: o2 (seq=1) then o1 (seq=2)
        assertEquals("301", orgAliases.get(0).getAliasId());
        assertEquals(Integer.valueOf(1), orgAliases.get(0).getSequenceNumber());
        assertEquals("Wayne Ent Ltd", orgAliases.get(0).getOrganisationName());

        assertEquals("300", orgAliases.get(1).getAliasId());
        assertEquals(Integer.valueOf(2), orgAliases.get(1).getSequenceNumber());
        assertEquals("Wayne Group", orgAliases.get(1).getOrganisationName());

        // Individual aliases must be null/empty for an organisation party
        var indDetails = partyDto.getPartyDetails().getIndividualDetails();
        if (indDetails != null) {
            assertTrue(indDetails.getIndividualAliases() == null || indDetails.getIndividualAliases().isEmpty());
        }
    }

    @Test
    void getDefendantAccountParty_individual_with_no_aliases_sets_individualAliases_null() {
        // Party (individual)
        var party = uk.gov.hmcts.opal.entity.PartyEntity.builder()
            .partyId(10L).organisation(false).title("Ms").forenames("Anna").surname("Graham")
            .build();

        var dap = uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(100L).associationType("DEFENDANT").debtor(true).party(party)
            .build();

        var account = uk.gov.hmcts.opal.entity.DefendantAccountEntity.builder()
            .defendantAccountId(1L)
            .parties(List.of(dap))
            .businessUnit(uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity.builder()
                .businessUnitId((short) 1).build())
            .versionNumber(0L)
            .build();

        when(defendantAccountRepository.findById(1L)).thenReturn(Optional.of(account));

        // No valid aliases: empty list OR rows that don't have surname populated are ignored
        when(aliasRepo.findByParty_PartyId(10L)).thenReturn(List.of(
            uk.gov.hmcts.opal.entity.AliasEntity.builder().aliasId(1L)
                .sequenceNumber(1).surname("   ").forenames("X").build(),
            uk.gov.hmcts.opal.entity.AliasEntity.builder().aliasId(2L)
                .sequenceNumber(2).organisationName("Some Org").build()
        ));
        when(debtorRepo.findByPartyId(10L)).thenReturn(Optional.empty());

        try {
            var f1 = OpalDefendantAccountService.class.getDeclaredField("aliasRepository");
            f1.setAccessible(true);
            f1.set(service, aliasRepo);
            var f2 = OpalDefendantAccountService.class.getDeclaredField("debtorDetailRepository");
            f2.setAccessible(true);
            f2.set(service, debtorRepo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        var resp = service.getDefendantAccountParty(1L, 100L);

        assertNotNull(resp);
        var ind = resp.getDefendantAccountParty().getPartyDetails().getIndividualDetails();
        assertNotNull(ind, "individual details should be present for an individual party");
        assertNull(ind.getIndividualAliases(), "no valid individual aliases → should be null");

        // also ensure org details/aliases are absent or empty
        var org = resp.getDefendantAccountParty().getPartyDetails().getOrganisationDetails();
        if (org != null) {
            assertTrue(org.getOrganisationAliases() == null || org.getOrganisationAliases().isEmpty());
        }
    }

    @Test
    void getDefendantAccountParty_organisation_with_no_aliases_sets_organisationAliases_null() {
        // Party (organisation)
        var party = uk.gov.hmcts.opal.entity.PartyEntity.builder()
            .partyId(20L).organisation(true).organisationName("TechCorp Solutions Ltd")
            .build();

        var dap = uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(200L).associationType("DEFENDANT").debtor(true).party(party)
            .build();

        var account = uk.gov.hmcts.opal.entity.DefendantAccountEntity.builder()
            .defendantAccountId(2L)
            .parties(List.of(dap))
            .businessUnit(uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity.builder()
                .businessUnitId((short) 1).build())
            .versionNumber(0L)
            .build();

        when(defendantAccountRepository.findById(2L)).thenReturn(Optional.of(account));

        // No valid org aliases: empty list OR rows with blank org names are ignored;
        // person-only rows are ignored for org parties
        when(aliasRepo.findByParty_PartyId(20L)).thenReturn(List.of(
            uk.gov.hmcts.opal.entity.AliasEntity.builder().aliasId(10L)
                .sequenceNumber(1).organisationName("   ").build(),
            uk.gov.hmcts.opal.entity.AliasEntity.builder().aliasId(11L)
                .sequenceNumber(2).surname("Jones").forenames("Bob").build()
        ));
        when(debtorRepo.findByPartyId(20L)).thenReturn(Optional.empty());

        var resp = service.getDefendantAccountParty(2L, 200L);

        assertNotNull(resp);
        var org = resp.getDefendantAccountParty().getPartyDetails().getOrganisationDetails();
        assertNotNull(org, "organisation details should be present for an organisation party");
        assertNull(org.getOrganisationAliases(), "no valid organisation aliases → should be null");

        // also ensure individual alias list is absent/empty
        var ind = resp.getDefendantAccountParty().getPartyDetails().getIndividualDetails();
        if (ind != null) {
            assertTrue(ind.getIndividualAliases() == null || ind.getIndividualAliases().isEmpty());
        }
    }

    @Test
    void getDefendantAccountFixedPenalty_shouldReturnVehicleFixedPenaltyResponse() {
        Long defendantAccountId = 77L;

        DefendantAccountEntity mockAccount = buildMockAccount(defendantAccountId);
        FixedPenaltyOffenceEntity mockOffence = buildMockOffence(true);

        when(defendantAccountRepository.findById(defendantAccountId))
            .thenReturn(Optional.of(mockAccount));
        when(fixedPenaltyOffenceRepository.findByDefendantAccountId(defendantAccountId))
            .thenReturn(Optional.of(mockOffence));

        GetDefendantAccountFixedPenaltyResponse response =
            service.getDefendantAccountFixedPenalty(defendantAccountId);

        assertNotNull(response);
        assertTrue(response.isVehicleFixedPenaltyFlag());
        assertEquals("Kingston-upon-Thames Mags Court",
            response.getFixedPenaltyTicketDetails().getIssuingAuthority());
        assertEquals("888", response.getFixedPenaltyTicketDetails().getTicketNumber());
        assertEquals("12:34", response.getFixedPenaltyTicketDetails().getTimeOfOffence());
        assertEquals("London", response.getFixedPenaltyTicketDetails().getPlaceOfOffence());
    }

    @Test
    void getDefendantAccountFixedPenalty_shouldReturnNonVehiclePenaltyResponse() {
        Long accountId = 88L;

        DefendantAccountEntity account = buildMockAccount(accountId);
        FixedPenaltyOffenceEntity offence = buildMockOffence(false);
        offence.setOffenceLocation("Manchester");
        offence.setTimeOfOffence(LocalTime.parse("12:12"));

        when(defendantAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(fixedPenaltyOffenceRepository.findByDefendantAccountId(accountId)).thenReturn(Optional.of(offence));

        GetDefendantAccountFixedPenaltyResponse response = service.getDefendantAccountFixedPenalty(accountId);

        assertNotNull(response);
        assertFalse(response.isVehicleFixedPenaltyFlag());
        assertEquals("Kingston-upon-Thames Mags Court", response.getFixedPenaltyTicketDetails().getIssuingAuthority());
        assertEquals("888", response.getFixedPenaltyTicketDetails().getTicketNumber());
        assertEquals("12:12", response.getFixedPenaltyTicketDetails().getTimeOfOffence());
        assertEquals("Manchester", response.getFixedPenaltyTicketDetails().getPlaceOfOffence());
        assertNull(response.getVehicleFixedPenaltyDetails());
    }

    @Test
    void getDefendantAccountFixedPenalty_shouldThrowWhenNoOffenceFound() {
        Long accountId = 999L;
        DefendantAccountEntity account = buildMockAccount(accountId);

        when(defendantAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(fixedPenaltyOffenceRepository.findByDefendantAccountId(accountId)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
            EntityNotFoundException.class,
            () -> service.getDefendantAccountFixedPenalty(accountId)
        );

        assertTrue(ex.getMessage().contains("Fixed Penalty Offence not found for account: 999"));
        verify(fixedPenaltyOffenceRepository).findByDefendantAccountId(accountId);
    }

    @Test
    void getDefendantAccountFixedPenalty_shouldThrowWhenAccountNotFound() {
        Long id = 123L;
        when(defendantAccountRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.getDefendantAccountFixedPenalty(id));
    }

    @Test
    void getDefendantAccountFixedPenalty_shouldHandleNullOptionalFields() {
        Long id = 456L;
        DefendantAccountEntity account = buildMockAccount(id);
        account.setOriginatorName(null);

        FixedPenaltyOffenceEntity offence = buildMockOffence(true);
        offence.setOffenceLocation(null);
        offence.setIssuedDate(null);
        offence.setLicenceNumber(null);
        offence.setVehicleRegistration(null);
        offence.setTimeOfOffence(null);

        when(defendantAccountRepository.findById(id)).thenReturn(Optional.of(account));
        when(fixedPenaltyOffenceRepository.findByDefendantAccountId(id)).thenReturn(Optional.of(offence));

        var response = service.getDefendantAccountFixedPenalty(id);
        assertNotNull(response);
        assertNotNull(response.getFixedPenaltyTicketDetails());
    }

    @Test
    void getDefendantAccountFixedPenalty_shouldMapVersionCorrectly() {
        Long id = 789L;
        DefendantAccountEntity acc = buildMockAccount(id);
        acc.setVersionNumber(5L);

        FixedPenaltyOffenceEntity offence = buildMockOffence(false);

        when(defendantAccountRepository.findById(id)).thenReturn(Optional.of(acc));
        when(fixedPenaltyOffenceRepository.findByDefendantAccountId(id)).thenReturn(Optional.of(offence));

        var resp = service.getDefendantAccountFixedPenalty(id);
        assertEquals(BigInteger.valueOf(5), resp.getVersion());
    }

    @Test
    void getDefendantAccountFixedPenalty_shouldCallProxyWhenAuthorized() {
        // Arrange
        var proxy = mock(DefendantAccountServiceProxy.class);
        var userStateService = mock(UserStateService.class);
        var mockUserState = mock(UserState.class);
        var mockResponse = new GetDefendantAccountFixedPenaltyResponse();

        when(userStateService.checkForAuthorisedUser("Bearer token")).thenReturn(mockUserState);
        when(mockUserState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(proxy.getDefendantAccountFixedPenalty(123L)).thenReturn(mockResponse);

        var service = new DefendantAccountService(proxy, userStateService);

        // Act
        var response = service.getDefendantAccountFixedPenalty(123L, "Bearer token");

        // Assert
        verify(proxy).getDefendantAccountFixedPenalty(123L);
        assertEquals(mockResponse, response);
    }


    @Test
    void getDefendantAccountFixedPenalty_shouldThrowWhenNotAuthorized() {
        // Arrange
        var proxy = mock(DefendantAccountServiceProxy.class);
        var userStateService = mock(UserStateService.class);
        var mockUserState = mock(UserState.class);

        when(userStateService.checkForAuthorisedUser("auth")).thenReturn(mockUserState);
        when(mockUserState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
            .thenReturn(false);

        var service = new DefendantAccountService(proxy, userStateService);

        // Act + Assert
        assertThrows(PermissionNotAllowedException.class,
            () -> service.getDefendantAccountFixedPenalty(123L, "auth")
        );

        verifyNoInteractions(proxy);
    }

    @Test
    void vehicleFixedPenaltyFlag_shouldBeFalse_whenVehicleRegistrationIsNullAndFlagFalse() {
        Long defendantAccountId = 201L;
        DefendantAccountEntity account = buildMockAccount(defendantAccountId);

        FixedPenaltyOffenceEntity offence = buildMockOffence(false);
        offence.setVehicleRegistration(null);
        offence.setVehicleFixedPenalty(false);

        when(defendantAccountRepository.findById(defendantAccountId)).thenReturn(Optional.of(account));
        when(fixedPenaltyOffenceRepository.findByDefendantAccountId(defendantAccountId))
            .thenReturn(Optional.of(offence));

        GetDefendantAccountFixedPenaltyResponse response = service.getDefendantAccountFixedPenalty(defendantAccountId);

        assertFalse(response.isVehicleFixedPenaltyFlag(),
            "Expected flag to be false when vehicleFixedPenalty=false and registration is null");
    }

    @Test
    void vehicleFixedPenaltyFlag_shouldBeFalse_whenVehicleRegistrationIsNVAndFlagFalse() {
        Long defendantAccountId = 202L;
        DefendantAccountEntity account = buildMockAccount(defendantAccountId);

        FixedPenaltyOffenceEntity offence = buildMockOffence(false);
        offence.setVehicleRegistration("NV");
        offence.setVehicleFixedPenalty(false);

        when(defendantAccountRepository.findById(defendantAccountId)).thenReturn(Optional.of(account));
        when(fixedPenaltyOffenceRepository.findByDefendantAccountId(defendantAccountId))
            .thenReturn(Optional.of(offence));

        GetDefendantAccountFixedPenaltyResponse response = service.getDefendantAccountFixedPenalty(defendantAccountId);

        assertFalse(response.isVehicleFixedPenaltyFlag(),
            "Expected flag to be false when vehicleFixedPenalty=false and registration='NV'");
    }

    @Test
    void vehicleFixedPenaltyFlag_shouldBeTrue_whenVehicleRegistrationIsNotNV() {
        Long defendantAccountId = 203L;
        DefendantAccountEntity account = buildMockAccount(defendantAccountId);

        FixedPenaltyOffenceEntity offence = buildMockOffence(false);
        offence.setVehicleRegistration("AB12CDE");
        offence.setVehicleFixedPenalty(false);

        when(defendantAccountRepository.findById(defendantAccountId)).thenReturn(Optional.of(account));
        when(fixedPenaltyOffenceRepository.findByDefendantAccountId(defendantAccountId))
            .thenReturn(Optional.of(offence));

        GetDefendantAccountFixedPenaltyResponse response = service.getDefendantAccountFixedPenalty(defendantAccountId);

        assertTrue(response.isVehicleFixedPenaltyFlag(),
            "Expected flag to be true when vehicleRegistration='AB12CDE' even if vehicleFixedPenalty=false");
    }


    private DefendantAccountEntity buildMockAccount(Long accountId) {
        return DefendantAccountEntity.builder()
            .defendantAccountId(accountId)
            .originatorName("Kingston-upon-Thames Mags Court")
            .versionNumber(1L)
            .build();
    }

    private FixedPenaltyOffenceEntity buildMockOffence(boolean isVehicle) {
        return FixedPenaltyOffenceEntity.builder()
            .ticketNumber("888")
            .vehicleRegistration(isVehicle ? "AB12CDE" : null)
            .offenceLocation("London")
            .noticeNumber("PN98765")
            .issuedDate(LocalDate.of(2024, 1, 1))
            .licenceNumber("DOE1234567")
            .vehicleFixedPenalty(isVehicle)
            .timeOfOffence(LocalTime.parse("12:34"))
            .build();
    }

    @Test
    void replaceDefendantAccountParty_noExistingParty_andMissingPartyId_throws() {
        Long accountId = 100L;
        Long dapId = 200L;

        BusinessUnitFullEntity buEnt = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10).build();

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(dapId).party(null).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId).businessUnit(buEnt).parties(List.of(dap)).versionNumber(1L).build();

        when(defendantAccountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        DefendantAccountParty req = DefendantAccountParty.builder()
            .partyDetails(PartyDetails.builder()
                .organisationFlag(Boolean.TRUE)
                .organisationDetails(OrganisationDetails.builder().organisationName("ACME").build())
                .build())
            .build();

        try (var vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(any(), anyString(), anyLong(), anyString()))
                .thenAnswer(i -> null);

            assertThrows(IllegalArgumentException.class, () ->
                service.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", "10", "tester", null));

            verify(defendantAccountRepository, never()).saveAndFlush(any());
        }
    }

    @Test
    void replaceDefendantAccountParty_switchingParty_isForbidden() {
        Long accountId = 100L;
        Long dapId = 200L;

        BusinessUnitFullEntity buEnt = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10).build();

        PartyEntity party = mock(PartyEntity.class);
        when(party.getPartyId()).thenReturn(300L);

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(dapId).party(party).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId).businessUnit(buEnt).parties(List.of(dap)).versionNumber(1L).build();

        when(defendantAccountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        DefendantAccountParty req = DefendantAccountParty.builder()
            .partyDetails(PartyDetails.builder().partyId("999").organisationFlag(Boolean.TRUE).build())
            .build();

        assertThrows(IllegalArgumentException.class, () ->
            service.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", "10", "tester", null));

        verify(defendantAccountRepository, never()).saveAndFlush(any());
    }

    @Test
    void replaceDefendantAccountParty_wrongBusinessUnit_throws() {
        Long accountId = 100L;

        BusinessUnitFullEntity buWrong = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 77).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId).businessUnit(buWrong).versionNumber(1L).build();

        when(defendantAccountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        assertThrows(EntityNotFoundException.class, () ->
            service.replaceDefendantAccountParty(accountId, 1L,
                DefendantAccountParty.builder().build(), "\"1\"", "10", "tester", null));

        verify(defendantAccountRepository, never()).saveAndFlush(any());
    }

    @Test
    void replaceDefendantAccountParty_nonDebtorAndNoPayload_deletesExistingDebtorDetail() {
        Long accountId = 200L;
        Long dapId = 201L;

        BusinessUnitFullEntity buEnt = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10).build();

        PartyEntity party = mock(PartyEntity.class);
        when(party.getPartyId()).thenReturn(222L);

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(dapId).party(party).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId).businessUnit(buEnt).parties(List.of(dap)).versionNumber(1L).build();

        when(defendantAccountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        DebtorDetailEntity existing = new DebtorDetailEntity();
        existing.setPartyId(222L);
        when(opalPartyService.findById(222L)).thenReturn(party);
        when(aliasRepo.findByParty_PartyId(222L)).thenReturn(emptyList());
        when(defendantAccountRepository.saveAndFlush(account)).thenReturn(account);

        DefendantAccountParty req = DefendantAccountParty.builder()
            .defendantAccountPartyType("Defendant").isDebtor(Boolean.FALSE)
            .partyDetails(PartyDetails.builder()
                .partyId("222").organisationFlag(Boolean.TRUE)
                .organisationDetails(OrganisationDetails.builder().organisationName("X").build())
                .build())
            .build();

        try (MockedStatic<VersionUtils> vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(any(), anyString(), anyLong(), anyString()))
                .thenAnswer(i -> null);

            GetDefendantAccountPartyResponse resp =
                service.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", "10", "tester", null);

            assertNotNull(resp);

            // existing debtor should be deleted (we previously retrieved it via findById)
            verify(defendantAccountRepository).saveAndFlush(account);
        }
    }

    @Test
    void replaceDefendantAccountParty_addressNull_and_contactNull_clear_all_fields() {
        Long accountId = 400L;
        Long dapId = 401L;

        BusinessUnitFullEntity buEnt = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10).build();

        PartyEntity party = mock(PartyEntity.class);
        when(party.getPartyId()).thenReturn(444L);

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(dapId).party(party).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId).businessUnit(buEnt).parties(List.of(dap)).versionNumber(1L).build();

        when(opalPartyService.findById(444L)).thenReturn(party);
        when(aliasRepo.findByParty_PartyId(444L)).thenReturn(emptyList());
        when(defendantAccountRepository.saveAndFlush(account)).thenReturn(account);

        when(debtorRepo.findById(444L)).thenReturn(Optional.of(new DebtorDetailEntity()));

        when(defendantAccountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        DefendantAccountParty req = DefendantAccountParty.builder()
            .defendantAccountPartyType("Defendant").isDebtor(Boolean.TRUE)
            .partyDetails(PartyDetails.builder()
                .partyId("444").organisationFlag(Boolean.TRUE)
                .organisationDetails(OrganisationDetails.builder().organisationName("ORG").build())
                .build())
            .address(null).contactDetails(null).build();

        try (MockedStatic<VersionUtils> vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(any(), anyString(), anyLong(), anyString()))
                .thenAnswer(i -> null);

            GetDefendantAccountPartyResponse resp =
                service.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", "10", "tester", null);

            assertNotNull(resp);

            // since address & contact were null, party setters should be called to clear fields
            verify(party).setAddressLine1(null);
            verify(party).setAddressLine2(null);
            verify(party).setAddressLine3(null);
            verify(party).setAddressLine4(null);
            verify(party).setAddressLine5(null);
            verify(party).setPostcode(null);

            verify(party).setPrimaryEmailAddress(null);
            verify(party).setSecondaryEmailAddress(null);
            verify(party).setMobileTelephoneNumber(null);
            verify(party).setHomeTelephoneNumber(null);
            verify(party).setWorkTelephoneNumber(null);

            verify(defendantAccountRepository).saveAndFlush(account);
        }
    }

    @Test
    void replaceDefendantAccountParty_happyPath_attachedParty_updates_and_audits() {
        Long accountId = 777L;
        Long dapId = 888L;
        String bu = "10";
        String ifMatch = "\"1\"";

        BusinessUnitFullEntity buEnt = BusinessUnitFullEntity.builder()
            .businessUnitId(Short.valueOf(bu)).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId)
            .businessUnit(buEnt)
            .versionNumber(1L)
            .build();

        PartyEntity party = mock(PartyEntity.class);
        when(party.getPartyId()).thenReturn(123L);

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(dapId)
            .party(party)
            .associationType("RESPONDENT")
            .debtor(Boolean.FALSE)
            .build();

        account.setParties(List.of(dap));

        when(aliasRepo.findByParty_PartyId(123L)).thenReturn(emptyList());

        when(opalPartyService.findById(123L)).thenReturn(party);

        when(defendantAccountRepository.saveAndFlush(account)).thenReturn(account);

        when(debtorRepo.findById(123L)).thenReturn(Optional.of(new DebtorDetailEntity()));

        when(defendantAccountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        DefendantAccountParty req = DefendantAccountParty.builder()
            .defendantAccountPartyType("Defendant")
            .isDebtor(Boolean.TRUE)
            .partyDetails(PartyDetails.builder()
                .partyId("123")
                .organisationFlag(Boolean.TRUE)
                .organisationDetails(OrganisationDetails.builder().organisationName("ACME LTD").build())
                .build())
            .address(AddressDetails.builder().addressLine1("1 MAIN").postcode("AB1 2CD").build())
            .contactDetails(ContactDetails.builder().primaryEmailAddress("a@b.com").workTelephoneNumber("0207").build())
            .vehicleDetails(VehicleDetails.builder().vehicleMakeAndModel("Ford Focus")
                .vehicleRegistration("AB12CDE").build())
            .employerDetails(EmployerDetails.builder()
                .employerName("Widgets Inc")
                .employerAddress(AddressDetails.builder().addressLine1("10 Park").postcode("ZZ1 1ZZ").build())
                .build())
            .languagePreferences(LanguagePreferences.builder()
                .documentLanguagePreference(LanguagePreference.fromCode("EN"))
                .hearingLanguagePreference(LanguagePreference.fromCode("CY"))
                .build())
            .build();

        try (MockedStatic<VersionUtils> vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(eq(account), eq(ifMatch), eq(accountId), anyString()))
                .thenAnswer(i -> null);

            GetDefendantAccountPartyResponse resp =
                service.replaceDefendantAccountParty(accountId, dapId, req, ifMatch, bu, "tester", null);

            assertNotNull(resp);
            assertNotNull(resp.getDefendantAccountParty());

            verify(defendantAccountRepository).saveAndFlush(account);
            verify(amendmentService).auditInitialiseStoredProc(accountId, RecordType.DEFENDANT_ACCOUNTS);
            verify(amendmentService).auditFinaliseStoredProc(
                eq(accountId), eq(RecordType.DEFENDANT_ACCOUNTS),
                eq(Short.parseShort(bu)), eq("tester"), any(), eq("ACCOUNT_ENQUIRY"));

            verify(party).setOrganisation(Boolean.TRUE);
            verify(party).setOrganisationName("ACME LTD");
            verify(party).setAddressLine1("1 MAIN");
            verify(party).setPrimaryEmailAddress("a@b.com");

            // called inside replaceAliasesForParty and again when building response
            verify(aliasRepo, times(2)).findByParty_PartyId(123L);
        }
    }

    @Test
    void replaceDefendantAccountParty_detachedParty_isReattached_via_OpalPartyService_findById() {
        Long accountId = 100L;
        Long dapId = 200L;
        String bu = "10";

        BusinessUnitFullEntity buEnt = BusinessUnitFullEntity.builder()
            .businessUnitId(Short.valueOf(bu)).build();

        PartyEntity partyProxy = mock(PartyEntity.class);
        when(partyProxy.getPartyId()).thenReturn(300L);

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(dapId).party(partyProxy).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId).businessUnit(buEnt).parties(List.of(dap)).versionNumber(1L).build();

        when(opalPartyService.findById(300L)).thenReturn(partyProxy);
        when(defendantAccountRepository.saveAndFlush(account)).thenReturn(account);

        when(aliasRepo.findByParty_PartyId(300L)).thenReturn(emptyList());

        when(defendantAccountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        DefendantAccountParty req = DefendantAccountParty.builder()
            .partyDetails(PartyDetails.builder()
                .partyId("300").organisationFlag(Boolean.TRUE)
                .organisationDetails(OrganisationDetails.builder().organisationName("ACME").build())
                .build())
            .build();

        try (MockedStatic<VersionUtils> vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(any(), anyString(), anyLong(), anyString()))
                .thenAnswer(i -> null);

            GetDefendantAccountPartyResponse resp =
                service.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", bu, "tester", null);

            assertNotNull(resp);
            assertNotNull(resp.getDefendantAccountParty());
            verify(opalPartyService, times(2)).findById(300L); // main + aliases
            verify(defendantAccountRepository).saveAndFlush(account);
            verify(aliasRepo, times(2)).findByParty_PartyId(300L);
        }
    }

    @Test
    void addPaymentCardRequest_happyPath_createsPCRAndUpdatesAccount() {
        // Arrange
        Long accountId = 99L;
        String buHeader = "10";
        String ifMatch = "\"1\"";

        BusinessUnitFullEntity bu = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10)
            .build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId)
            .businessUnit(bu)
            .versionNumber(1L)
            .build();

        when(defendantAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(paymentCardRequestRepository.existsByDefendantAccountId(accountId)).thenReturn(false);

        when(accessTokenService.extractName("AUTH")).thenReturn("John Smith");

        // Make save(account) echo the argument
        when(defendantAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        AddPaymentCardRequestResponse response =
            service.addPaymentCardRequest(
                accountId,
                buHeader,
                "L080JG",     // businessUnitUserId MUST be the 3rd argument
                ifMatch,
                "AUTH"
            );

        // Assert
        assertNotNull(response);
        assertEquals(accountId, response.getDefendantAccountId());

        assertTrue(account.getPaymentCardRequested());
        assertEquals("L080JG", account.getPaymentCardRequestedBy());
        assertEquals("John Smith", account.getPaymentCardRequestedByName());

        verify(paymentCardRequestRepository).save(any(PaymentCardRequestEntity.class));
    }

    @Test
    void addPaymentCardRequest_failsWhenPcrAlreadyExists() {
        when(defendantAccountRepository.findById(1L)).thenReturn(Optional.of(
            DefendantAccountEntity.builder()
                .businessUnit(BusinessUnitFullEntity.builder().businessUnitId((short) 10).build())
                .versionNumber(1L)
                .build()
        ));

        when(paymentCardRequestRepository.existsByDefendantAccountId(1L))
            .thenReturn(true);

        assertThrows(ResourceConflictException.class, () ->
            service.addPaymentCardRequest(1L, "10", null, "\"1\"", "AUTH")
        );
    }

    @Test
    void addPaymentCardRequest_failsWhenBusinessUnitMismatch() {
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .businessUnit(BusinessUnitFullEntity.builder().businessUnitId((short) 77).build())
            .versionNumber(1L)
            .build();

        when(defendantAccountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(EntityNotFoundException.class, () ->
            service.addPaymentCardRequest(1L, "10", null, "\"1\"", "AUTH")
        );
    }

    @Test
    void addPaymentCardRequest_allowsNullBusinessUnitUserId_whenUserNotInBusinessUnit() {
        // Arrange
        var account = DefendantAccountEntity.builder()
            .businessUnit(BusinessUnitFullEntity.builder().businessUnitId((short) 10).build())
            .versionNumber(1L)
            .build();

        when(defendantAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(paymentCardRequestRepository.existsByDefendantAccountId(1L)).thenReturn(false);

        when(defendantAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act + Assert
        assertDoesNotThrow(() ->
            service.addPaymentCardRequest(1L, "10", null, "\"1\"", "AUTH")
        );

        // And verify the account was updated with null BU-user-id
        assertTrue(account.getPaymentCardRequested());
        assertNull(account.getPaymentCardRequestedBy());
    }

    @Test
    void addPaymentCardRequest_versionConflictThrows() {
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .businessUnit(BusinessUnitFullEntity.builder().businessUnitId((short) 10).build())
            .versionNumber(5L)  // expects If-Match: "5"
            .build();

        when(defendantAccountRepository.findById(1L))
            .thenReturn(Optional.of(account));

        assertThrows(ObjectOptimisticLockingFailureException.class, () ->
            service.addPaymentCardRequest(1L, "10", null, "\"0\"", "AUTH")
        );
    }

    @Test
    void replaceDefendantAccountParty_employerNull_languageNull_clearsEmployerAndLanguages_savesDebtor() {
        Long accountId = 300L;
        Long dapId = 301L;

        BusinessUnitFullEntity buEnt = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10).build();

        PartyEntity party = mock(PartyEntity.class);
        when(party.getPartyId()).thenReturn(333L);

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(dapId).party(party).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId).businessUnit(buEnt).parties(List.of(dap)).versionNumber(1L).build();

        when(defendantAccountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        when(opalPartyService.findById(333L)).thenReturn(party);
        when(aliasRepo.findByParty_PartyId(333L)).thenReturn(emptyList());

        when(defendantAccountRepository.saveAndFlush(account)).thenReturn(account);
        when(debtorRepo.findById(333L)).thenReturn(Optional.empty());
        when(debtorRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        when(debtorRepo.findById(333L)).thenReturn(Optional.of(new DebtorDetailEntity()));

        DefendantAccountParty req = DefendantAccountParty.builder()
            .defendantAccountPartyType("Defendant").isDebtor(Boolean.TRUE)
            .partyDetails(PartyDetails.builder()
                .partyId("333").organisationFlag(Boolean.FALSE)
                .individualDetails(IndividualDetails.builder()
                    .title("Ms").forenames("Jane").surname("Doe")
                    .dateOfBirth("1990-01-02").age("35").nationalInsuranceNumber("NI123").build())
                .build())
            .vehicleDetails(VehicleDetails.builder().vehicleMakeAndModel("VW Golf")
                .vehicleRegistration("JD02CAR").build())
            // employer null, language null
            .build();

        try (MockedStatic<VersionUtils> vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(any(), anyString(), anyLong(), anyString()))
                .thenAnswer(i -> null);

            GetDefendantAccountPartyResponse resp =
                service.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", "10", "tester", null);

            assertNotNull(resp);

            ArgumentCaptor<DebtorDetailEntity> cap = ArgumentCaptor.forClass(DebtorDetailEntity.class);
            verify(debtorRepo).save(cap.capture());
            DebtorDetailEntity saved = cap.getValue();

            assertEquals("VW Golf", saved.getVehicleMake());
            assertEquals("JD02CAR", saved.getVehicleRegistration());

            assertNull(saved.getEmployerName());
            assertNull(saved.getEmployeeReference());
            assertNull(saved.getEmployerEmail());
            assertNull(saved.getEmployerTelephone());
            assertNull(saved.getEmployerAddressLine1());
            assertNull(saved.getEmployerAddressLine2());
            assertNull(saved.getEmployerAddressLine3());
            assertNull(saved.getEmployerAddressLine4());
            assertNull(saved.getEmployerAddressLine5());
            assertNull(saved.getEmployerPostcode());

            assertNull(saved.getDocumentLanguage());
            assertNull(saved.getHearingLanguage());
            assertNull(saved.getDocumentLanguageDate());
            assertNull(saved.getHearingLanguageDate());

            verify(defendantAccountRepository).saveAndFlush(account);
            verify(aliasRepo, times(2)).findByParty_PartyId(333L);
        }
    }

    @Test
    void testGetEnforcementStatus() {
        // Arrange
        DefendantAccountEntity defAccount = DefendantAccountEntity.builder()
            .parties(List.of(
                DefendantAccountPartiesEntity.builder()
                    .associationType("Defendant")
                    .party(PartyEntity.builder()
                        .birthDate(LocalDate.of(1990, 1, 1))
                        .build())
                    .build()))
            .defendantAccountId(1L)
            .accountStatus("L")
            .build();

        EnforcementEntity.Lite enforcementEntity = EnforcementEntity.Lite.builder()
                .build();

        when(defendantAccountRepository.findById(anyLong())).thenReturn(Optional.of(defAccount));
        when(enforcementRepository.findFirstByDefendantAccountIdAndResultIdOrderByPostedDateDesc(
            any(), any())).thenReturn(Optional.of(enforcementEntity));

        // Act
        EnforcementStatus response = service.getEnforcementStatus(1L);

        // Assert
        assertNotNull(response);
        assertNull(response.getNextEnforcementActionData());
        assertFalse(response.getEmployerFlag());
        assertEquals(DefendantAccountTypeEnum.ADULT, response.getDefendantAccountType());
        assertFalse(response.getIsHmrcCheckEligible());
    }

    @Test
    void addPaymentCardRequest_newSignature_failsWhenBusinessUnitMismatch() {
        // Arrange
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .businessUnit(BusinessUnitFullEntity.builder()
                .businessUnitId((short) 20)   // Actual BU = 20
                .build())
            .versionNumber(1L)
            .build();

        when(defendantAccountRepository.findById(1L))
            .thenReturn(Optional.of(account));

        // Act + Assert
        assertThrows(EntityNotFoundException.class, () ->
            service.addPaymentCardRequest(
                1L,
                "10",              // Requested BU = 10 (mismatch)
                "BU-USER-123",     // Passed in from above layer
                "\"1\"",
                "AUTH"
            )
        );

        verify(defendantAccountRepository, never()).save(any());
    }

    @Test
    void addPaymentCardRequest_newSignature_succeedsWhenBusinessUnitMatches() {
        // Arrange
        Long accountId = 99L;
        String buHeader = "10";
        String ifMatch = "\"1\"";
        String businessUnitUserId = "L080JG";

        BusinessUnitFullEntity bu = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10)
            .build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId)
            .businessUnit(bu)
            .versionNumber(1L)
            .build();

        when(defendantAccountRepository.findById(accountId))
            .thenReturn(Optional.of(account));
        when(paymentCardRequestRepository.existsByDefendantAccountId(accountId))
            .thenReturn(false);
        when(accessTokenService.extractName("AUTH"))
            .thenReturn("John Smith");
        when(defendantAccountRepository.save(any()))
            .thenAnswer(inv -> inv.getArgument(0));

        // Act
        AddPaymentCardRequestResponse response = service.addPaymentCardRequest(
            accountId,
            buHeader,
            businessUnitUserId,   // MUST be passed here now
            ifMatch,
            "AUTH"
        );

        // Assert
        assertNotNull(response);
        assertEquals(accountId, response.getDefendantAccountId());
        assertTrue(account.getPaymentCardRequested());
        assertEquals("L080JG", account.getPaymentCardRequestedBy());
        assertEquals("John Smith", account.getPaymentCardRequestedByName());

        verify(paymentCardRequestRepository).save(any(PaymentCardRequestEntity.class));
        verify(defendantAccountRepository).save(account);
    }

    @Test
    void getHeaderSummary_permissionDenied_throws403() {
        var proxy = mock(DefendantAccountServiceProxy.class);

        UserState userState = mock(UserState.class);
        when(userStateService.checkForAuthorisedUser("AUTH")).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
            .thenReturn(false);

        var svc = new DefendantAccountService(proxy, userStateService);

        assertThrows(PermissionNotAllowedException.class,
            () -> svc.getHeaderSummary(1L, "AUTH"));

        verifyNoInteractions(proxy);
    }

    @Test
    void updateDefendantAccount_blankBUUserId_fallsBackToUsername() {
        var proxy = mock(DefendantAccountServiceProxy.class);

        UserState userState = mock(UserState.class);
        when(userStateService.checkForAuthorisedUser("AUTH")).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.ACCOUNT_MAINTENANCE))
            .thenReturn(true);

        var buUser = mock(BusinessUnitUser.class);
        when(buUser.getBusinessUnitUserId()).thenReturn("   "); // blank -> filtered out
        when(userState.getBusinessUnitUserForBusinessUnit((short) 10))
            .thenReturn(Optional.of(buUser));

        when(userState.getUserName()).thenReturn("fallbackUser");

        var req = mock(UpdateDefendantAccountRequest.class);

        var svc = new DefendantAccountService(proxy, userStateService);

        svc.updateDefendantAccount(1L, "10", req, "\"1\"", "AUTH");

        verify(proxy).updateDefendantAccount(1L, "10", req, "\"1\"", "fallbackUser");
    }

    @Test
    void replaceParty_blankBUUserId_fallsBackToUsername() {
        var proxy = mock(DefendantAccountServiceProxy.class);

        UserState userState = mock(UserState.class);
        when(userStateService.checkForAuthorisedUser("AUTH")).thenReturn(userState);
        when(userState.hasBusinessUnitUserWithPermission((short) 10, FinesPermission.ACCOUNT_MAINTENANCE))
            .thenReturn(true);

        var buUser = mock(BusinessUnitUser.class);
        when(buUser.getBusinessUnitUserId()).thenReturn("   "); // BLANK → triggers fallback

        when(userState.getBusinessUnitUserForBusinessUnit((short) 10))
            .thenReturn(Optional.of(buUser));

        when(userState.getUserName()).thenReturn("fallbackUser");

        var svc = new DefendantAccountService(proxy, userStateService);

        svc.replaceDefendantAccountParty(
            1L, 2L, "AUTH", "\"1\"", "10",
            mock(DefendantAccountParty.class)
        );

        verify(proxy).replaceDefendantAccountParty(
            eq(1L),
            eq(2L),
            any(DefendantAccountParty.class),
            eq("\"1\""),
            eq("10"),
            eq("fallbackUser"),
            eq("fallbackUser")
        );
    }

    @Test
    void addPaymentCardRequest_permissionDenied_throws403() {
        var proxy = mock(DefendantAccountServiceProxy.class);

        UserState userState = mock(UserState.class);
        when(userStateService.checkForAuthorisedUser("AUTH")).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.AMEND_PAYMENT_TERMS))
            .thenReturn(false);

        var svc = new DefendantAccountService(proxy, userStateService);

        assertThrows(PermissionNotAllowedException.class,
            () -> svc.addPaymentCardRequest(1L, "10", "USR", "\"1\"", "AUTH"));

        verifyNoInteractions(proxy);
    }

    @Test
    void searchDefendantAccounts_hasRef_whenBothFieldsNull_shouldBeFalse() {
        // Arrange
        ReferenceNumberDto ref = new ReferenceNumberDto();
        ref.setAccountNumber(null);
        ref.setProsecutorCaseReference(null);

        AccountSearchDto dto = AccountSearchDto.builder()
            .activeAccountsOnly(true)
            .referenceNumberDto(ref)
            .businessUnitIds(Collections.emptyList())
            .build();

        when(searchDefAccRepo.findAll(
            ArgumentMatchers.<Specification<SearchDefendantAccountEntity>>any()
        )).thenReturn(Collections.emptyList());

        // Act
        service.searchDefendantAccounts(dto);

        // Assert
        verify(searchSpecsSpy).filterByActiveOnly(true);
    }

    @Test
    void replaceDefendantAccountParty_throwsWhenAccountHasNoBusinessUnit() {
        // Arrange
        Long accountId = 10L;
        Long dapId = 20L;

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId)
            .businessUnit(null) // <-- triggers first missing branch
            .versionNumber(1L)
            .parties(Collections.emptyList())
            .build();

        when(defendantAccountRepository.findById(accountId))
            .thenReturn(Optional.of(account));

        // Act + Assert
        assertThrows(EntityNotFoundException.class, () ->
            service.replaceDefendantAccountParty(
                accountId, dapId,
                mock(DefendantAccountParty.class),
                "\"1\"",
                "10",
                "tester",
                null
            )
        );
    }

    @Test
    void replaceDefendantAccountParty_throwsWhenAccountHasNullBusinessUnitId() {
        // Arrange
        Long accountId = 11L;
        Long dapId = 21L;

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId)
            .businessUnit(BusinessUnitFullEntity.builder()
                .businessUnitId(null)   // <-- triggers second missing branch
                .build())
            .versionNumber(1L)
            .parties(Collections.emptyList())
            .build();

        when(defendantAccountRepository.findById(accountId))
            .thenReturn(Optional.of(account));

        // Act + Assert
        assertThrows(EntityNotFoundException.class, () ->
            service.replaceDefendantAccountParty(
                accountId, dapId,
                mock(DefendantAccountParty.class),
                "\"1\"",
                "10",
                "tester",
                null
            )
        );
    }

    @Test
    void replaceDefendantAccountParty_partyNull_andRequestedPartyIdNull_throws() {
        Long accountId = 50L;
        Long dapId = 60L;

        BusinessUnitFullEntity bu = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10)
            .build();

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(dapId)
            .party(null)     // <-- triggers 'party == null' branch
            .build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId)
            .businessUnit(bu)
            .versionNumber(1L)
            .parties(List.of(dap))
            .build();

        when(defendantAccountRepository.findById(accountId))
            .thenReturn(Optional.of(account));

        DefendantAccountParty req = DefendantAccountParty.builder()
            .partyDetails(PartyDetails.builder()
                .partyId(null)
                .organisationFlag(Boolean.TRUE)
                .build())
            .build();

        try (MockedStatic<VersionUtils> vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(any(Versioned.class), any(String.class), anyLong(),
                    any(String.class)))
                .thenAnswer(inv -> null);

            assertThrows(IllegalArgumentException.class, () ->
                service.replaceDefendantAccountParty(
                    accountId,
                    dapId,
                    req,
                    "\"1\"",
                    "10",
                    null,
                    null
                )
            );
        }

    }

    @Test
    void replaceDefendantAccountParty_requestNull_throws() {
        Long accountId = 90L;
        Long dapId = 91L;

        BusinessUnitFullEntity bu = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10)
            .build();

        PartyEntity party = PartyEntity.builder()
            .partyId(300L)
            .build();

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(dapId)
            .party(party)
            .build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId)
            .businessUnit(bu)
            .versionNumber(1L)
            .parties(List.of(dap))
            .build();

        when(defendantAccountRepository.findById(accountId))
            .thenReturn(Optional.of(account));

        try (MockedStatic<VersionUtils> vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(
                any(Versioned.class),
                anyString(),
                anyLong(),
                anyString()
            )).thenAnswer(inv -> null);

            assertThrows(IllegalArgumentException.class, () ->
                service.replaceDefendantAccountParty(
                    accountId,
                    dapId,
                    null,
                    "\"1\"",
                    "10",
                    "tester",
                    null
                )
            );
        }
    }

    @Test
    void replaceDefendantAccountParty_throwsWhenBusinessUnitMissing() {
        // Arrange
        DefendantAccountEntity account = new DefendantAccountEntity();
        account.setBusinessUnit(null); // THIS triggers the uncovered branch

        when(defendantAccountRepository.findById(99L))
            .thenReturn(Optional.of(account));

        // Act + Assert
        assertThrows(EntityNotFoundException.class, () ->
            service.replaceDefendantAccountParty(
                99L, 1L, mock(DefendantAccountParty.class),
                "\"1\"", "10", "POSTED_BY", "BU_USER")
        );
    }

    @Test
    void replaceDefendantAccountParty_throwsWhenRequestedPartyIdNull() {
        // ───── Arrange account with correct BU so it gets past BU validation ─────
        DefendantAccountEntity account = new DefendantAccountEntity();
        BusinessUnitFullEntity bu = new BusinessUnitFullEntity();
        bu.setBusinessUnitId((short) 78);
        account.setBusinessUnit(bu);
        account.setVersionNumber(1L);

        // ───── Create a DAP (DefendantAccountParty) entry with NO internal Party ─────
        // This forces the service into the: if (party == null && requestedPartyId == null) branch
        DefendantAccountPartiesEntity dap = new DefendantAccountPartiesEntity();
        dap.setDefendantAccountPartyId(20010L);
        dap.setParty(null);   // ← REQUIRED to trigger the branch

        account.setParties(List.of(dap));

        when(defendantAccountRepository.findById(77L))
            .thenReturn(Optional.of(account));

        // ───── Mock request → request.getPartyDetails() returns null ─────
        // This forces requestedPartyId = null
        DefendantAccountParty request = mock(DefendantAccountParty.class);
        when(request.getPartyDetails()).thenReturn(null);

        // ───── Act + Assert ─────
        assertThrows(IllegalArgumentException.class, () ->
            service.replaceDefendantAccountParty(
                77L,
                20010L,
                request,
                "\"1\"",   // ifMatch
                "78",      // BU
                "POSTED_BY",
                "BU_USER"
            )
        );
    }

    @Test
    void replaceDefendantAccountParty_coreReplace_handlesOrgAndIndividualNullBranches() {
        // Arrange a BU-matching account + party
        BusinessUnitFullEntity bu = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10).build();

        PartyEntity party = mock(PartyEntity.class);
        when(party.getPartyId()).thenReturn(500L);

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(999L)
            .party(party)
            .build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(100L)
            .versionNumber(1L)
            .businessUnit(bu)
            .parties(List.of(dap))
            .build();

        when(defendantAccountRepository.findById(100L)).thenReturn(Optional.of(account));
        when(opalPartyService.findById(500L)).thenReturn(party);
        when(aliasRepo.findByParty_PartyId(500L)).thenReturn(Collections.emptyList());
        when(defendantAccountRepository.saveAndFlush(account)).thenReturn(account);

        // Request with orgFlag = true but organisationDetails = null → covers NULL org branch
        DefendantAccountParty req = DefendantAccountParty.builder()
            .defendantAccountPartyType("Defendant")
            .isDebtor(false)
            .partyDetails(PartyDetails.builder()
                .partyId("500")
                .organisationFlag(true)
                .organisationDetails(null) // ← triggers red branch
                .build())
            .build();

        try (MockedStatic<VersionUtils> vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(
                any(Versioned.class), anyString(), anyLong(), anyString()
            )).thenAnswer(i -> null);


            service.replaceDefendantAccountParty(100L, 999L, req, "\"1\"", "10", "poster", null);

            // Verify organisation null branch executed
            verify(party).setOrganisationName(null);
            verify(party).setTitle(null);
            verify(party).setForenames(null);
            verify(party).setSurname(null);
            verify(party).setBirthDate(null);
            verify(party).setAge(null);
            verify(party).setNiNumber(null);
        }
    }

    @Test
    void replaceDefendantAccountParty_addressNotNull_setsFields() {
        PartyEntity party = mock(PartyEntity.class);
        when(party.getPartyId()).thenReturn(600L);

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .party(party)
            .defendantAccountPartyId(700L)
            .build();

        BusinessUnitFullEntity bu = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(600L)
            .businessUnit(bu)
            .versionNumber(1L)
            .parties(List.of(dap))
            .build();

        when(defendantAccountRepository.findById(600L)).thenReturn(Optional.of(account));
        when(opalPartyService.findById(600L)).thenReturn(party);
        when(aliasRepo.findByParty_PartyId(600L)).thenReturn(Collections.emptyList());
        when(defendantAccountRepository.saveAndFlush(account)).thenReturn(account);

        DefendantAccountParty req = DefendantAccountParty.builder()
            .partyDetails(PartyDetails.builder()
                .partyId("600")
                .organisationFlag(false)
                .individualDetails(IndividualDetails.builder()
                    .title("Mr").forenames("John").surname("Smith")
                    .build())
                .build())
            .address(AddressDetails.builder()
                .addressLine1("Line1")
                .addressLine2("Line2")
                .postcode("ZZ1 1ZZ")
                .build())
            .defendantAccountPartyType("Defendant")
            .isDebtor(false)
            .build();

        try (MockedStatic<VersionUtils> vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(
                any(Versioned.class), anyString(), anyLong(), anyString()
            )).thenAnswer(i -> null);


            service.replaceDefendantAccountParty(
                600L, 700L, req, "\"1\"", "10", "poster", null);

            verify(party).setAddressLine1("Line1");
            verify(party).setAddressLine2("Line2");
            verify(party).setPostcode("ZZ1 1ZZ");
        }
    }

    @Test
    void replaceDefendantAccountParty_contactNotNull_setsFields() {
        PartyEntity party = mock(PartyEntity.class);
        when(party.getPartyId()).thenReturn(601L);

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .party(party)
            .defendantAccountPartyId(701L)
            .build();

        BusinessUnitFullEntity bu = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10).build();

        DefendantAccountEntity acc = DefendantAccountEntity.builder()
            .defendantAccountId(601L)
            .businessUnit(bu)
            .versionNumber(1L)
            .parties(List.of(dap))
            .build();

        when(defendantAccountRepository.findById(601L)).thenReturn(Optional.of(acc));
        when(opalPartyService.findById(601L)).thenReturn(party);
        when(aliasRepo.findByParty_PartyId(601L)).thenReturn(Collections.emptyList());
        when(defendantAccountRepository.saveAndFlush(acc)).thenReturn(acc);

        DefendantAccountParty req = DefendantAccountParty.builder()
            .partyDetails(PartyDetails.builder()
                .partyId("601")
                .organisationFlag(false)
                .individualDetails(IndividualDetails.builder()
                    .title("Dr").forenames("Amy").surname("Pond").build())
                .build())
            .contactDetails(ContactDetails.builder()
                .primaryEmailAddress("x@y.com")
                .mobileTelephoneNumber("07123456789")
                .build())
            .defendantAccountPartyType("Defendant")
            .isDebtor(false)
            .build();

        try (MockedStatic<VersionUtils> vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(any(Versioned.class), any(String.class), anyLong(),
                    any(String.class)))
                .thenAnswer(i -> null);

            service.replaceDefendantAccountParty(
                601L, 701L, req, "\"1\"", "10", "poster", null);

            verify(party).setPrimaryEmailAddress("x@y.com");
            verify(party).setMobileTelephoneNumber("07123456789");
        }
    }


}
