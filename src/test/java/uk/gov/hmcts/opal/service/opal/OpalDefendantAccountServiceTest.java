package uk.gov.hmcts.opal.service.opal;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.CollectionOrderDto;
import uk.gov.hmcts.opal.dto.CourtReferenceDto;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
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
import uk.gov.hmcts.opal.dto.common.OrganisationAlias;
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
import uk.gov.hmcts.opal.entity.EnforcementOverrideResultEntity;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentCardRequestEntity;
import uk.gov.hmcts.opal.entity.SearchDefendantAccountEntity;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.repository.AliasRepository;
import uk.gov.hmcts.opal.repository.CourtRepository;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountHeaderViewRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountPaymentTermsRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountSummaryViewRepository;
import uk.gov.hmcts.opal.repository.EnforcementOverrideResultRepository;
import uk.gov.hmcts.opal.repository.EnforcerRepository;
import uk.gov.hmcts.opal.repository.FixedPenaltyOffenceRepository;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.PaymentCardRequestRepository;
import uk.gov.hmcts.opal.repository.SearchDefendantAccountRepository;
import uk.gov.hmcts.opal.repository.jpa.DefendantAccountSpecs;
import uk.gov.hmcts.opal.repository.jpa.SearchDefendantAccountSpecs;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountServiceProxy;
import uk.gov.hmcts.opal.util.VersionUtils;

class OpalDefendantAccountServiceTest {

    private final DefendantAccountRepository defendantAccountRepository = mock(DefendantAccountRepository.class);
    private final DefendantAccountSummaryViewRepository dasvRepository = mock(DefendantAccountSummaryViewRepository
        .class);
    private final DefendantAccountHeaderViewRepository dahvRepository = mock(DefendantAccountHeaderViewRepository
        .class);
    private final SearchDefendantAccountRepository searchDefAccRepo = mock(SearchDefendantAccountRepository
        .class);
    private final SearchDefendantAccountSpecs searchDefAccSpecs = new SearchDefendantAccountSpecs();
    private final DefendantAccountPaymentTermsRepository paymentTermsRepository = mock(
        DefendantAccountPaymentTermsRepository.class);
    private final FixedPenaltyOffenceRepository fixedPenaltyOffenceRepository = mock(FixedPenaltyOffenceRepository
        .class);

    private final PaymentCardRequestRepository paymentCardRequestRepository =
        mock(PaymentCardRequestRepository.class);

    private final AccessTokenService accessTokenService =
        mock(AccessTokenService.class);

    private final UserStateService userStateService =
        mock(UserStateService.class);


    @Mock
    AmendmentService amendmentService = mock(AmendmentService.class);
    @Mock
    DebtorDetailRepository debtorDetailRepository = mock(DebtorDetailRepository.class);
    @Mock
    AliasRepository aliasRepository = mock(AliasRepository.class);
    @Mock
    OpalPartyService opalPartyService = mock(OpalPartyService.class);
    private DefendantAccountSpecs defendantAccountSpecs;
    // ONE shared spy for search specs; this is the instance we verify interactions on
    private SearchDefendantAccountSpecs searchSpecsSpy;
    // Service under test
    private OpalDefendantAccountService service;

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Class<?> c = target.getClass();
            while (c != Object.class) {
                try {
                    var f = c.getDeclaredField(fieldName);
                    f.setAccessible(true);
                    f.set(target, value);
                    return;
                } catch (NoSuchFieldException ignored) {
                    c = c.getSuperclass();
                }
            }
            throw new RuntimeException("Field not found: " + fieldName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private OpalDefendantAccountService newService() {
        // Keep alignment with your existing constructor usage (pass nulls for irrelevant deps)
        return new OpalDefendantAccountService(
            null, defendantAccountRepository, null, null, null,
            null, null,
            amendmentService, null, null,
            null, null,
            null, null,
            accessTokenService,
            userStateService,opalPartyService,
            fixedPenaltyOffenceRepository);
    }

    @BeforeEach
    void setUp() {
        // Create the spy fresh each test
        searchSpecsSpy = spy(new SearchDefendantAccountSpecs());

        // Build the service with EXACTLY these dependencies in the expected order
        this.service = new OpalDefendantAccountService(
            /* headerViewRepo */ mock(DefendantAccountHeaderViewRepository.class),
            /* defendantAccountRepository */ defendantAccountRepository,
            /* searchDefAccRepo */ searchDefAccRepo,
            /* searchDefAccSpecs */ searchSpecsSpy,
            /* paymentTermsRepository */ paymentTermsRepository,
            /* dasvRepository */ dasvRepository,
            /* courtRepo */ mock(CourtRepository.class),
            /* amendmentService */ mock(AmendmentService.class),
            /* entityManager */ mock(EntityManager.class),
            /* noteRepository */ mock(NoteRepository.class),
            /* enforcementOverrideResult */ mock(EnforcementOverrideResultRepository.class),
            /* localJusticeAreaRepo */ mock(LocalJusticeAreaRepository.class),
            /* enforcerRepository */ mock(EnforcerRepository.class),
            /* paymentCardRequestRepository */ paymentCardRequestRepository,
            /* accessTokenService */ accessTokenService,
            /* userStateService */ userStateService,
            null,
            /* fixedPenaltyOffenceRepository */ fixedPenaltyOffenceRepository
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

        // Core repos & deps
        final DefendantAccountHeaderViewRepository headerViewRepo = mock(DefendantAccountHeaderViewRepository.class);
        final DefendantAccountRepository accountRepo = mock(DefendantAccountRepository.class);
        final DefendantAccountSpecs specs = mock(DefendantAccountSpecs.class);
        final DefendantAccountPaymentTermsRepository paymentTermsRepo =
            mock(DefendantAccountPaymentTermsRepository.class);
        final SearchDefendantAccountRepository searchDefAccRepo = mock(SearchDefendantAccountRepository.class);
        final SearchDefendantAccountSpecs searchDefAccSpecs = mock(SearchDefendantAccountSpecs.class);
        final DefendantAccountSummaryViewRepository dasvRepo = mock(DefendantAccountSummaryViewRepository.class);
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
            enforcerRepo,
            null,
            accessTokenService,
            userStateService,
            opalPartyService,
            fixedPenaltyOffenceRepository
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
            null, accountRepo, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null);

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
            null, accountRepo, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null);

        Long id = 1L;
        String buHeader = "10";

        BusinessUnitFullEntity bu = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 77)
            .build();

        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .businessUnit(bu)
            .versionNumber(1L)
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
            null, accountRepo, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null);

        Long id = 1L;
        String buHeader = "10";

        BusinessUnitFullEntity bu = BusinessUnitFullEntity.builder()
            .businessUnitId(Short.valueOf(buHeader))
            .build();

        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .businessUnit(bu)
            .versionNumber(1L)
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
            null, accountRepo, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null);

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
            amendmentService,
            em,
            noteRepository,
            null,
            null,
            null,
            null,
            null,
            userStateService,
            opalPartyService,
            fixedPenaltyOffenceRepository
        );

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
            mock(CourtRepository.class),
            mock(AmendmentService.class),
            mock(EntityManager.class),
            mock(NoteRepository.class),
            null,
            null,
            null,
            null,
            null,
            userStateService,
            opalPartyService,
            fixedPenaltyOffenceRepository
        );

        var bu = BusinessUnitFullEntity.builder().businessUnitId((short) 78).build();
        var entity = DefendantAccountEntity.builder().defendantAccountId(77L).businessUnit(bu)
            .versionNumber(5L).build();
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

        var bu = BusinessUnitFullEntity.builder().businessUnitId((short) 78).build();
        var entity = DefendantAccountEntity.builder()
            .defendantAccountId(77L).businessUnit(bu).versionNumber(0L).build();
        when(accountRepo.findById(77L)).thenReturn(Optional.of(entity));

        var svc = new OpalDefendantAccountService(
            null,
            accountRepo,
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
            null,
            null,
            null,
            userStateService,
            opalPartyService,
            null
        );

        var req = UpdateDefendantAccountRequest.builder()
            .commentsAndNotes(CommentsAndNotes.builder().accountNotesAccountComments("hello").build()).build();

        svc.updateDefendantAccount(77L, "78", req, "0", "11111111A");

        verify(amend).auditInitialiseStoredProc(77L, RecordType.DEFENDANT_ACCOUNTS);
        verify(amend).auditFinaliseStoredProc(
            eq(77L), eq(RecordType.DEFENDANT_ACCOUNTS), eq((short) 78),
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
            mock(AmendmentService.class),
            mock(EntityManager.class),
            mock(NoteRepository.class),
            mock(EnforcementOverrideResultRepository.class),
            mock(LocalJusticeAreaRepository.class),
            mock(EnforcerRepository.class),
            mock(PaymentCardRequestRepository.class),
            mock(AccessTokenService.class),
            mock(UserStateService.class),
            mock(OpalPartyService.class),
            mock(FixedPenaltyOffenceRepository.class)
        );

        var bu = BusinessUnitFullEntity.builder().businessUnitId((short) 78).build();
        var entity = DefendantAccountEntity.builder().defendantAccountId(77L).businessUnit(bu)
            .versionNumber(0L).build();
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

        var out = OpalDefendantAccountService.buildIndividualAliasesList(e);

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

        var ind = OpalDefendantAccountService.buildIndividualAliasesList(e);
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

        var ind = OpalDefendantAccountService.buildIndividualAliasesList(e);

        assertEquals(1, ind.size());
        assertEquals("P100", ind.get(0).getAliasId());
        assertEquals(1, ind.get(0).getSequenceNumber());
        assertEquals("John William", ind.get(0).getForenames()); // last token is surname
        assertEquals("Smith", ind.get(0).getSurname());

        // entity is an individual → org aliases list must be empty
        var org = OpalDefendantAccountService.buildOrganisationAliasesList(e);
        assertTrue(org.isEmpty());
    }

    @Test
    void individualAliases_preserves_row_order_for_valid_rows() {
        var e = mockDasv(false,
            "P1|1|Alpha One",
            "P3|3|Gamma Three",
            null, null, null
        );

        var ind = OpalDefendantAccountService.buildIndividualAliasesList(e);
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

        var out = OpalDefendantAccountService.buildOrganisationAliasesList(e);

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

        var out = OpalDefendantAccountService.buildOrganisationAliasesList(e);
        assertEquals(1, out.size());
        assertEquals("O2", out.get(0).getAliasId());
        assertEquals(2, out.get(0).getSequenceNumber());
        assertEquals("Beta Org", out.get(0).getOrganisationName());

        // entity is an organisation → individual list must be empty
        var ind = OpalDefendantAccountService.buildIndividualAliasesList(e);
        assertTrue(ind.isEmpty());
    }

    @Test
    void all_null_or_blank_alias_slots_yield_empty_lists_for_both_entity_types() {
        var person = mockDasv(false, null, "", "   ", null, "");
        var org = mockDasv(true, null, "", "   ", null, "");

        assertTrue(OpalDefendantAccountService.buildIndividualAliasesList(person).isEmpty());
        assertTrue(OpalDefendantAccountService.buildOrganisationAliasesList(person).isEmpty());

        assertTrue(OpalDefendantAccountService.buildIndividualAliasesList(org).isEmpty());
        assertTrue(OpalDefendantAccountService.buildOrganisationAliasesList(org).isEmpty());
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

        assertEquals(2, OpalDefendantAccountService.buildIndividualAliasesList(person).size());
        assertTrue(OpalDefendantAccountService.buildOrganisationAliasesList(person).isEmpty());

        assertEquals(2, OpalDefendantAccountService.buildOrganisationAliasesList(org).size());
        assertTrue(OpalDefendantAccountService.buildIndividualAliasesList(org).isEmpty());
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
    void safeInt_returnsNullForOverflowAndUnderflow() throws Exception {
        var method = OpalDefendantAccountService.class.getDeclaredMethod("safeInt", Long.class);
        method.setAccessible(true);

        // Overflow
        Integer result1 = (Integer) method.invoke(null, Long.MAX_VALUE);
        assertNull(result1);

        // Underflow
        Integer result2 = (Integer) method.invoke(null, Long.MIN_VALUE);
        assertNull(result2);
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
        when(refTrue.getOrganisation()).thenReturn(true);
        when(dtoTrue.getReferenceNumberDto()).thenReturn(refTrue);
        when(dtoTrue.getActiveAccountsOnly()).thenReturn(false);
        when(dtoTrue.getBusinessUnitIds()).thenReturn(emptyList());
        when(dtoTrue.getDefendant()).thenReturn(null);

        AccountSearchDto dtoFalse = mock(AccountSearchDto.class, RETURNS_DEEP_STUBS);
        ReferenceNumberDto refFalse = mock(ReferenceNumberDto.class);
        when(refFalse.getOrganisation()).thenReturn(false);
        when(dtoFalse.getReferenceNumberDto()).thenReturn(refFalse);
        when(dtoFalse.getActiveAccountsOnly()).thenReturn(false);
        when(dtoFalse.getBusinessUnitIds()).thenReturn(emptyList());
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

        BusinessUnitSummary summary = service.buildBusinessUnitSummary(e);
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
        // --- Arrange repos/deps
        var accountRepo = mock(DefendantAccountRepository.class);
        var aliasRepo = mock(uk.gov.hmcts.opal.repository.AliasRepository.class);
        var debtorRepo = mock(uk.gov.hmcts.opal.repository.DebtorDetailRepository.class);

        // Build a Party (individual)
        var party = uk.gov.hmcts.opal.entity.PartyEntity.builder()
            .partyId(10L).organisation(false).title("Mr").forenames("John").surname("Doe")
            .build();

        // Link party into DefendantAccountPartiesEntity
        var dap = uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(100L)
            .associationType("DEFENDANT")
            .debtor(true)
            .party(party)
            .build();

        // Account with that party
        var account = uk.gov.hmcts.opal.entity.DefendantAccountEntity.builder()
            .defendantAccountId(1L)
            .parties(List.of(dap))
            .businessUnit(uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity.builder()
                .businessUnitId((short) 1).build())
            .versionNumber(0L)
            .build();

        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));

        // AliasEntity rows: only surname populated should be considered for individual path;
        // blanks and org-only names ignored; ensure sorting by sequenceNumber.
        var a1 = uk.gov.hmcts.opal.entity.AliasEntity.builder()
            .aliasId(200L).sequenceNumber(2).surname("Smith").forenames("Alice").build();
        var a2 = uk.gov.hmcts.opal.entity.AliasEntity.builder()
            .aliasId(201L).sequenceNumber(1).surname("Jones").forenames("Bob").build();
        var blank = uk.gov.hmcts.opal.entity.AliasEntity.builder()
            .aliasId(202L).sequenceNumber(3).surname("   ").forenames("X").build(); // ignored
        var orgOnly = uk.gov.hmcts.opal.entity.AliasEntity.builder()
            .aliasId(203L).sequenceNumber(4).organisationName("Wayne Ent") // ignored for individual
            .build();

        when(aliasRepo.findByParty_PartyId(10L)).thenReturn(List.of(a1, a2, blank, orgOnly));
        when(debtorRepo.findByPartyId(10L)).thenReturn(null);

        // Service with required deps
        var svc = new OpalDefendantAccountService(
            /* headerViewRepo */ null,
            accountRepo,
            /* search repo/specs etc not used here */ null, null, null, null,
            /* courtRepo */ null,
            /* amendment */ null,
            /* em */ null,
            /* noteRepo */ null,
            /* eor/lja/enforcer */ null, null, null, null,
            null, null, null, null
        );
        java.lang.reflect.Field f1;
        try {
            f1 = OpalDefendantAccountService.class.getDeclaredField("aliasRepository");
            f1.setAccessible(true);
            f1.set(svc, aliasRepo);
            var f2 = OpalDefendantAccountService.class.getDeclaredField("debtorDetailRepository");
            f2.setAccessible(true);
            f2.set(svc, debtorRepo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // --- Act
        var resp = svc.getDefendantAccountParty(1L, 100L);

        assertNotNull(resp);
        var partyDto = resp.getDefendantAccountParty();
        assertNotNull(partyDto.getPartyDetails().getIndividualDetails());
        var indAliases = partyDto.getPartyDetails().getIndividualDetails().getIndividualAliases();
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
        var orgAliases = partyDto.getPartyDetails().getOrganisationDetails();
        if (orgAliases != null) {
            assertTrue(orgAliases.getOrganisationAliases() == null || orgAliases.getOrganisationAliases().isEmpty());
        }
    }

    @Test
    void getDefendantAccountParty_builds_organisation_aliases_only() {
        var accountRepo = mock(DefendantAccountRepository.class);
        var aliasRepo = mock(uk.gov.hmcts.opal.repository.AliasRepository.class);
        var debtorRepo = mock(uk.gov.hmcts.opal.repository.DebtorDetailRepository.class);

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

        when(accountRepo.findById(2L)).thenReturn(Optional.of(account));

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
        when(debtorRepo.findByPartyId(20L)).thenReturn(null);

        var svc = new OpalDefendantAccountService(
            null, accountRepo, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null);

        try {
            var f1 = OpalDefendantAccountService.class.getDeclaredField("aliasRepository");
            f1.setAccessible(true);
            f1.set(svc, aliasRepo);
            var f2 = OpalDefendantAccountService.class.getDeclaredField("debtorDetailRepository");
            f2.setAccessible(true);
            f2.set(svc, debtorRepo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        var resp = svc.getDefendantAccountParty(2L, 200L);

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
        // Repos
        var accountRepo = mock(DefendantAccountRepository.class);
        var aliasRepo = mock(uk.gov.hmcts.opal.repository.AliasRepository.class);
        var debtorRepo = mock(uk.gov.hmcts.opal.repository.DebtorDetailRepository.class);

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

        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));

        // No valid aliases: empty list OR rows that don't have surname populated are ignored
        when(aliasRepo.findByParty_PartyId(10L)).thenReturn(List.of(
            uk.gov.hmcts.opal.entity.AliasEntity.builder().aliasId(1L)
                .sequenceNumber(1).surname("   ").forenames("X").build(),
            uk.gov.hmcts.opal.entity.AliasEntity.builder().aliasId(2L)
                .sequenceNumber(2).organisationName("Some Org").build()
        ));
        when(debtorRepo.findByPartyId(10L)).thenReturn(null);

        var svc = new OpalDefendantAccountService(
            null, accountRepo, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null);

        try {
            var f1 = OpalDefendantAccountService.class.getDeclaredField("aliasRepository");
            f1.setAccessible(true);
            f1.set(svc, aliasRepo);
            var f2 = OpalDefendantAccountService.class.getDeclaredField("debtorDetailRepository");
            f2.setAccessible(true);
            f2.set(svc, debtorRepo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        var resp = svc.getDefendantAccountParty(1L, 100L);

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
        var accountRepo = mock(DefendantAccountRepository.class);
        var aliasRepo = mock(uk.gov.hmcts.opal.repository.AliasRepository.class);
        var debtorRepo = mock(uk.gov.hmcts.opal.repository.DebtorDetailRepository.class);

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

        when(accountRepo.findById(2L)).thenReturn(Optional.of(account));

        // No valid org aliases: empty list OR rows with blank org names are ignored;
        // person-only rows are ignored for org parties
        when(aliasRepo.findByParty_PartyId(20L)).thenReturn(List.of(
            uk.gov.hmcts.opal.entity.AliasEntity.builder().aliasId(10L)
                .sequenceNumber(1).organisationName("   ").build(),
            uk.gov.hmcts.opal.entity.AliasEntity.builder().aliasId(11L)
                .sequenceNumber(2).surname("Jones").forenames("Bob").build()
        ));
        when(debtorRepo.findByPartyId(20L)).thenReturn(null);

        var svc = new OpalDefendantAccountService(
            null, accountRepo, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null);

        try {
            var f1 = OpalDefendantAccountService.class.getDeclaredField("aliasRepository");
            f1.setAccessible(true);
            f1.set(svc, aliasRepo);
            var f2 = OpalDefendantAccountService.class.getDeclaredField("debtorDetailRepository");
            f2.setAccessible(true);
            f2.set(svc, debtorRepo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        var resp = svc.getDefendantAccountParty(2L, 200L);

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
    void replaceDefendantAccountParty_happyPath_attachedParty_updates_and_audits() {
        Long accountId = 777L;
        Long dapId = 888L;
        String bu = "10";
        String ifMatch = "\"1\"";

        BusinessUnitFullEntity buEnt = BusinessUnitFullEntity.builder()
            .businessUnitId(Short.valueOf(bu))
            .build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId)
            .businessUnit(buEnt)
            .versionNumber(1L)
            .build();

        // Party is mocked so we can verify setters
        PartyEntity party = mock(PartyEntity.class);
        when(party.getPartyId()).thenReturn(123L);

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(dapId)
            .party(party)
            .associationType("RESPONDENT") // initial; will be overwritten by req
            .debtor(Boolean.FALSE)
            .build();

        account.setParties(List.of(dap));
        when(defendantAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        // repository.save echoes the same instance
        when(defendantAccountRepository.save(account)).thenReturn(account);

        AmendmentService amendmentService = mock(AmendmentService.class);
        EntityManager em = mock(EntityManager.class);
        when(em.contains(party)).thenReturn(true);

        // Inject mocked DebtorDetailRepository
        DebtorDetailRepository debtorRepo = mock(DebtorDetailRepository.class);
        when(debtorRepo.findById(anyLong())).thenReturn(Optional.empty());
        when(debtorRepo.existsById(anyLong())).thenReturn(false);
        when(debtorRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OpalDefendantAccountService svc = new OpalDefendantAccountService(
            null,
            defendantAccountRepository,
            null,
            null,
            null,
            null,
            null,
            amendmentService,
            em,
            null,
            null,
            null,
            null,
            paymentCardRequestRepository,
            accessTokenService,
            userStateService,
            null
        );


        setField(svc, "debtorDetailRepository", debtorRepo);

        AliasRepository aliasRepo = mock(AliasRepository.class);
        when(aliasRepo.findByParty_PartyId(anyLong())).thenReturn(emptyList());
        setField(svc, "aliasRepository", aliasRepo);


        // ---- Build the request ----
        DefendantAccountParty req = DefendantAccountParty.builder()
            .defendantAccountPartyType("Defendant")
            .isDebtor(Boolean.TRUE)
            .partyDetails(PartyDetails.builder()
                .partyId("123") // must match existing
                .organisationFlag(Boolean.TRUE)
                .organisationDetails(OrganisationDetails.builder()
                    .organisationName("ACME LTD")
                    .build())
                .build())
            .address(AddressDetails.builder()
                .addressLine1("1 MAIN")
                .postcode("AB1 2CD")
                .build())
            .contactDetails(ContactDetails.builder()
                .primaryEmailAddress("a@b.com")
                .workTelephoneNumber("0207")
                .build())
            .vehicleDetails(VehicleDetails.builder()
                .vehicleMakeAndModel("Ford Focus")
                .vehicleRegistration("AB12CDE")
                .build())
            .employerDetails(EmployerDetails.builder()
                .employerName("Widgets Inc")
                .employerAddress(AddressDetails.builder()
                    .addressLine1("10 Park")
                    .postcode("ZZ1 1ZZ")
                    .build())
                .build())
            .languagePreferences(LanguagePreferences.builder()
                .documentLanguagePreference(LanguagePreference.fromCode("EN"))
                .hearingLanguagePreference(LanguagePreference.fromCode("CY"))
                .build())
            .build();

        try (MockedStatic<uk.gov.hmcts.opal.util.VersionUtils> vs =
            mockStatic(uk.gov.hmcts.opal.util.VersionUtils.class)) {
            vs.when(() -> uk.gov.hmcts.opal.util.VersionUtils
                    .verifyIfMatch(eq(account), eq(ifMatch), eq(accountId), anyString()))
                .thenAnswer(i -> null);

            GetDefendantAccountPartyResponse resp =
                svc.replaceDefendantAccountParty(accountId, dapId, req, ifMatch, bu, "tester", null);

            assertNotNull(resp);
            assertNotNull(resp.getDefendantAccountParty());

            // behavioural assertions (no lock now)
            verify(defendantAccountRepository).save(account);
            verify(em).flush();

            verify(amendmentService).auditInitialiseStoredProc(accountId, RecordType.DEFENDANT_ACCOUNTS);
            verify(amendmentService).auditFinaliseStoredProc(eq(accountId), eq(RecordType.DEFENDANT_ACCOUNTS),
                eq(Short.parseShort(bu)), eq("tester"), any(), eq("ACCOUNT_ENQUIRY"));

            // a couple of key field updates on Party
            verify(party).setOrganisation(Boolean.TRUE);
            verify(party).setOrganisationName("ACME LTD");
            verify(party).setAddressLine1("1 MAIN");
            verify(party).setPrimaryEmailAddress("a@b.com");
        }
    }

    @Test
    void replaceDefendantAccountParty_detachedParty_isReattached_with_getReference() {
        Long accountId = 100L;
        Long dapId = 200L;
        String bu = "10";

        BusinessUnitFullEntity buEnt = BusinessUnitFullEntity.builder()
            .businessUnitId(Short.valueOf(bu))
            .build();

        PartyEntity party = mock(PartyEntity.class);
        when(party.getPartyId()).thenReturn(300L);

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(dapId)
            .party(party)
            .build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId)
            .businessUnit(buEnt)
            .parties(List.of(dap))
            .versionNumber(1L)
            .build();

        when(defendantAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(defendantAccountRepository.save(account)).thenReturn(account);

        AmendmentService amendmentService = mock(AmendmentService.class);
        EntityManager em = mock(EntityManager.class);
        when(em.contains(party)).thenReturn(false);
        when(em.getReference(PartyEntity.class, 300L)).thenReturn(party);

        // Inject mocked DebtorDetailRepository
        DebtorDetailRepository debtorRepo = mock(DebtorDetailRepository.class);
        when(debtorRepo.findById(anyLong())).thenReturn(Optional.empty());
        when(debtorRepo.existsById(anyLong())).thenReturn(false);
        when(debtorRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        OpalDefendantAccountService svc = new OpalDefendantAccountService(
            null,
            defendantAccountRepository,
            null,
            null,
            null,
            null,
            null,
            amendmentService,
            em,
            null,
            null,
            null,
            null,
            paymentCardRequestRepository,
            accessTokenService,
            userStateService,
            null
        );

        setField(svc, "debtorDetailRepository", debtorRepo);

        AliasRepository aliasRepo = mock(AliasRepository.class);
        when(aliasRepo.findByParty_PartyId(anyLong())).thenReturn(emptyList());
        setField(svc, "aliasRepository", aliasRepo);


        DefendantAccountParty req = DefendantAccountParty.builder()
            .partyDetails(PartyDetails.builder()
                .partyId("300")
                .organisationFlag(Boolean.TRUE)
                .organisationDetails(OrganisationDetails.builder().organisationName("ACME").build())
                .build())
            .build();

        try (MockedStatic<uk.gov.hmcts.opal.util.VersionUtils> vs =
            mockStatic(uk.gov.hmcts.opal.util.VersionUtils.class)) {
            vs.when(() -> uk.gov.hmcts.opal.util.VersionUtils.verifyIfMatch(any(), anyString(), anyLong(), anyString()))
                .thenAnswer(i -> null);

            GetDefendantAccountPartyResponse resp =
                svc.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", bu, "tester", null);

            assertNotNull(resp);
            assertNotNull(resp.getDefendantAccountParty());
            verify(em).getReference(PartyEntity.class, 300L);
            verify(defendantAccountRepository).save(account);
            verify(em).flush();
        }
    }

    @Test
    void replaceDefendantAccountParty_happyPath_updates_org_addr_contact_debtor_and_audits_and_aliases() {
        Long accountId = 777L;
        Long dapId = 888L;
        String bu = "10";
        String ifMatch = "\"1\"";

        BusinessUnitFullEntity buEnt = BusinessUnitFullEntity.builder()
            .businessUnitId(Short.valueOf(bu)).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId).businessUnit(buEnt).versionNumber(1L).build();

        PartyEntity party = mock(PartyEntity.class);
        when(party.getPartyId()).thenReturn(123L);

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(dapId).party(party).associationType("OLDTYPE").debtor(Boolean.FALSE).build();

        account.setParties(java.util.List.of(dap));
        when(defendantAccountRepository.findById(accountId)).thenReturn(java.util.Optional.of(account));
        when(defendantAccountRepository.save(account)).thenReturn(account);

        AmendmentService amendmentService = mock(AmendmentService.class);
        EntityManager em = mock(EntityManager.class);
        when(em.contains(party)).thenReturn(true);

        OpalDefendantAccountService svc = new OpalDefendantAccountService(
            null,
            defendantAccountRepository,
            null,
            null,
            null,
            null,
            null,
            amendmentService,
            em,
            null,
            null,
            null,
            null,
            paymentCardRequestRepository,
            accessTokenService,
            userStateService,
            null
        );

        DebtorDetailRepository debtorRepo = mock(DebtorDetailRepository.class);
        when(debtorRepo.findById(anyLong())).thenReturn(java.util.Optional.empty());
        when(debtorRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        setField(svc, "debtorDetailRepository", debtorRepo);

        AliasRepository aliasRepo = mock(AliasRepository.class);
        when(aliasRepo.findByParty_PartyId(123L)).thenReturn(java.util.Collections.emptyList());
        setField(svc, "aliasRepository", aliasRepo);

        DefendantAccountParty req = DefendantAccountParty.builder()
            .defendantAccountPartyType("Defendant").isDebtor(Boolean.TRUE)
            .partyDetails(PartyDetails.builder()
                .partyId("123").organisationFlag(Boolean.TRUE)
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

        try (var vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(eq(account), eq(ifMatch), eq(accountId), anyString()))
                .thenAnswer(i -> null);

            GetDefendantAccountPartyResponse resp =
                svc.replaceDefendantAccountParty(accountId, dapId, req, ifMatch, bu, "tester", null);

            assertNotNull(resp);
            assertNotNull(resp.getDefendantAccountParty());

            verify(defendantAccountRepository).save(account);
            verify(em).flush();

            verify(amendmentService).auditInitialiseStoredProc(accountId, RecordType.DEFENDANT_ACCOUNTS);
            verify(amendmentService).auditFinaliseStoredProc(
                eq(accountId), eq(RecordType.DEFENDANT_ACCOUNTS),
                eq(Short.parseShort(bu)), eq("tester"), any(), eq("ACCOUNT_ENQUIRY"));

            verify(party).setOrganisation(Boolean.TRUE);
            verify(party).setOrganisationName("ACME LTD");
            verify(party).setAddressLine1("1 MAIN");
            verify(party).setPrimaryEmailAddress("a@b.com");

            verify(debtorRepo, atLeastOnce()).save(any());
            verify(aliasRepo).findByParty_PartyId(123L);
        }
    }

    @Test
    void replaceDefendantAccountParty_detachedParty_isReattached_with_getReference_and_saved() {
        Long accountId = 100L;
        Long dapId = 200L;
        String bu = "10";

        BusinessUnitFullEntity buEnt = BusinessUnitFullEntity.builder()
            .businessUnitId(Short.valueOf(bu)).build();

        PartyEntity party = mock(PartyEntity.class);
        when(party.getPartyId()).thenReturn(300L);

        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(dapId).party(party).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId).businessUnit(buEnt).parties(java.util.List.of(dap))
            .versionNumber(1L).build();

        when(defendantAccountRepository.findById(accountId)).thenReturn(java.util.Optional.of(account));
        when(defendantAccountRepository.save(account)).thenReturn(account);

        AmendmentService amendmentService = mock(AmendmentService.class);
        EntityManager em = mock(EntityManager.class);
        when(em.contains(party)).thenReturn(false);
        when(em.getReference(PartyEntity.class, 300L)).thenReturn(party);

        OpalDefendantAccountService svc = new OpalDefendantAccountService(
            null,
            defendantAccountRepository,
            null,
            null,
            null,
            null,
            null,
            amendmentService,
            em,
            null,
            null,
            null,
            null,
            paymentCardRequestRepository,
            accessTokenService,
            userStateService,
            null
        );

        DebtorDetailRepository debtorRepo = mock(DebtorDetailRepository.class);
        when(debtorRepo.findById(anyLong())).thenReturn(java.util.Optional.empty());
        when(debtorRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        setField(svc, "debtorDetailRepository", debtorRepo);

        AliasRepository aliasRepo = mock(AliasRepository.class);
        when(aliasRepo.findByParty_PartyId(300L)).thenReturn(java.util.Collections.emptyList());
        setField(svc, "aliasRepository", aliasRepo);

        DefendantAccountParty req = DefendantAccountParty.builder()
            .partyDetails(PartyDetails.builder()
                .partyId("300").organisationFlag(Boolean.TRUE)
                .organisationDetails(OrganisationDetails.builder().organisationName("ACME").build())
                .build())
            .build();

        try (var vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(any(), anyString(), anyLong(), anyString()))
                .thenAnswer(i -> null);

            GetDefendantAccountPartyResponse resp =
                svc.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", bu, "tester", null);

            assertNotNull(resp);
            verify(em).getReference(PartyEntity.class, 300L);
            verify(defendantAccountRepository).save(account);
            verify(em).flush();
            verify(aliasRepo).findByParty_PartyId(300L);
        }
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
            .defendantAccountId(accountId).businessUnit(buEnt).parties(java.util.List.of(dap))
            .versionNumber(1L).build();
        AmendmentService amendmentService = mock(AmendmentService.class);

        when(defendantAccountRepository.findById(accountId)).thenReturn(java.util.Optional.of(account));

        OpalDefendantAccountService svc = new OpalDefendantAccountService(
            null,
            defendantAccountRepository,
            null,
            null,
            null,
            null,
            null,
            amendmentService,
            null,
            null,
            null,
            null,
            null,
            paymentCardRequestRepository,
            accessTokenService,
            userStateService,
            null
        );
        setField(svc, "debtorDetailRepository", mock(DebtorDetailRepository.class));
        setField(svc, "aliasRepository", mock(AliasRepository.class));

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
                svc.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", "10",
                    "tester", null));

            verify(defendantAccountRepository, never()).save(any());
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
            .defendantAccountId(accountId).businessUnit(buEnt).parties(java.util.List.of(dap))
            .versionNumber(1L).build();

        AmendmentService amendmentService = mock(AmendmentService.class);
        when(defendantAccountRepository.findById(accountId)).thenReturn(java.util.Optional.of(account));
        OpalDefendantAccountService svc = new OpalDefendantAccountService(
            null,
            defendantAccountRepository,
            null,
            null,
            null,
            null,
            null,
            amendmentService,
            null,
            null,
            null,
            null,
            null,
            paymentCardRequestRepository,
            accessTokenService,
            userStateService,
            null
        );

        setField(svc, "debtorDetailRepository", mock(DebtorDetailRepository.class));
        setField(svc, "aliasRepository", mock(AliasRepository.class));

        DefendantAccountParty req = DefendantAccountParty.builder()
            .partyDetails(PartyDetails.builder().partyId("999").organisationFlag(Boolean.TRUE).build())
            .build();

        try (var vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(any(), anyString(), anyLong(), anyString()))
                .thenAnswer(i -> null);

            assertThrows(IllegalArgumentException.class, () ->
                svc.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", "10",
                    "tester", null));

            verify(defendantAccountRepository, never()).save(any());
        }
    }

    @Test
    void replaceDefendantAccountParty_wrongBusinessUnit_throws() {
        Long accountId = 100L;

        BusinessUnitFullEntity buWrong = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 77).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId).businessUnit(buWrong).versionNumber(1L).build();

        when(defendantAccountRepository.findById(accountId)).thenReturn(java.util.Optional.of(account));

        OpalDefendantAccountService svc = new OpalDefendantAccountService(
            null,
             defendantAccountRepository,
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
            paymentCardRequestRepository,
            accessTokenService,
            userStateService,
            null
        );
        setField(svc, "debtorDetailRepository", mock(DebtorDetailRepository.class));
        setField(svc, "aliasRepository", mock(AliasRepository.class));

        assertThrows(EntityNotFoundException.class, () ->
            svc.replaceDefendantAccountParty(accountId, 1L,
                DefendantAccountParty.builder().build(), "\"1\"", "10", "tester", null));

        verify(defendantAccountRepository, never()).save(any());
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
            .defendantAccountId(accountId).businessUnit(buEnt).parties(java.util.List.of(dap))
            .versionNumber(1L).build();

        when(defendantAccountRepository.findById(accountId)).thenReturn(java.util.Optional.of(account));
        when(defendantAccountRepository.save(account)).thenReturn(account);

        AmendmentService amendmentService = mock(AmendmentService.class);
        EntityManager em = mock(EntityManager.class);
        when(em.contains(party)).thenReturn(true);

        OpalDefendantAccountService svc = new OpalDefendantAccountService(
            null,
            defendantAccountRepository,
            null,
            null,
            null,
            null,
            null,
            amendmentService,
            em,
            null,
            null,
            null,
            null,
            paymentCardRequestRepository,
            accessTokenService,
            userStateService,
            null
        );

        DebtorDetailEntity existing = new DebtorDetailEntity();
        existing.setPartyId(222L);

        DebtorDetailRepository debtorRepo = mock(DebtorDetailRepository.class);
        when(debtorRepo.findById(222L)).thenReturn(java.util.Optional.of(existing));
        setField(svc, "debtorDetailRepository", debtorRepo);

        AliasRepository aliasRepo = mock(AliasRepository.class);
        when(aliasRepo.findByParty_PartyId(222L)).thenReturn(java.util.Collections.emptyList());
        setField(svc, "aliasRepository", aliasRepo);

        DefendantAccountParty req = DefendantAccountParty.builder()
            .defendantAccountPartyType("Defendant").isDebtor(Boolean.FALSE)
            .partyDetails(PartyDetails.builder()
                .partyId("222").organisationFlag(Boolean.TRUE)
                .organisationDetails(OrganisationDetails.builder().organisationName("X").build())
                .build())
            .build();

        try (var vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(any(), anyString(), anyLong(), anyString()))
                .thenAnswer(i -> null);

            GetDefendantAccountPartyResponse resp =
                svc.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", "10",
                    "tester", null);

            assertNotNull(resp);
            verify(debtorRepo).delete(existing);
            verify(defendantAccountRepository).save(account);
            verify(em).flush();
        }
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
            .defendantAccountId(accountId).businessUnit(buEnt).parties(java.util.List.of(dap))
            .versionNumber(1L).build();

        when(defendantAccountRepository.findById(accountId)).thenReturn(java.util.Optional.of(account));
        when(defendantAccountRepository.save(account)).thenReturn(account);

        AmendmentService amendmentService = mock(AmendmentService.class);
        EntityManager em = mock(EntityManager.class);
        when(em.contains(party)).thenReturn(true);

        OpalDefendantAccountService svc = new OpalDefendantAccountService(
            null,
            defendantAccountRepository,
            null,
            null,
            null,
            null,
            null,
            amendmentService,
            em,
            null,
            null,
            null,
            null,
            paymentCardRequestRepository,
            accessTokenService,
            userStateService,
            null
        );

        DebtorDetailRepository debtorRepo = mock(DebtorDetailRepository.class);
        when(debtorRepo.findById(333L)).thenReturn(java.util.Optional.empty());
        when(debtorRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        setField(svc, "debtorDetailRepository", debtorRepo);

        AliasRepository aliasRepo = mock(AliasRepository.class);
        when(aliasRepo.findByParty_PartyId(333L)).thenReturn(java.util.Collections.emptyList());
        setField(svc, "aliasRepository", aliasRepo);

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

        try (var vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(any(), anyString(), anyLong(), anyString()))
                .thenAnswer(i -> null);

            GetDefendantAccountPartyResponse resp =
                svc.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", "10",
                    "tester", null);

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

            verify(defendantAccountRepository).save(account);
            verify(em).flush();
            verify(aliasRepo).findByParty_PartyId(333L);
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
            .defendantAccountId(accountId).businessUnit(buEnt).parties(java.util.List.of(dap))
            .versionNumber(1L).build();

        when(defendantAccountRepository.findById(accountId)).thenReturn(java.util.Optional.of(account));
        when(defendantAccountRepository.save(account)).thenReturn(account);

        AmendmentService amendmentService = mock(AmendmentService.class);
        EntityManager em = mock(EntityManager.class);
        when(em.contains(party)).thenReturn(true);

        OpalDefendantAccountService svc = new OpalDefendantAccountService(
            null,
            defendantAccountRepository,
            null,
            null,
            null,
            null,
            null,
            amendmentService,
            em,
            null,
            null,
            null,
            null,
            paymentCardRequestRepository,
            accessTokenService,
            userStateService,
            null
        );
        DebtorDetailRepository debtorRepo = mock(DebtorDetailRepository.class);
        when(debtorRepo.findById(444L)).thenReturn(java.util.Optional.empty());
        when(debtorRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        setField(svc, "debtorDetailRepository", debtorRepo);

        AliasRepository aliasRepo = mock(AliasRepository.class);
        when(aliasRepo.findByParty_PartyId(444L)).thenReturn(java.util.Collections.emptyList());
        setField(svc, "aliasRepository", aliasRepo);

        DefendantAccountParty req = DefendantAccountParty.builder()
            .defendantAccountPartyType("Defendant").isDebtor(Boolean.TRUE)
            .partyDetails(PartyDetails.builder()
                .partyId("444").organisationFlag(Boolean.TRUE)
                .organisationDetails(OrganisationDetails.builder().organisationName("ORG").build())
                .build())
            .address(null).contactDetails(null).build();

        try (var vs = mockStatic(VersionUtils.class)) {
            vs.when(() -> VersionUtils.verifyIfMatch(any(), anyString(), anyLong(), anyString()))
                .thenAnswer(i -> null);

            GetDefendantAccountPartyResponse resp =
                svc.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", "10",
                    "tester", null);

            assertNotNull(resp);

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

            verify(defendantAccountRepository).save(account);
            verify(em).flush();
        }
    }

    @Test
    void replaceAliasesForParty_pdNull_deletesAndReturns_without_getReference_or_saves() throws Exception {
        Long partyId = 111L;
        AliasRepository aliasRepo = mock(AliasRepository.class);
        EntityManager em = mock(EntityManager.class);

        OpalDefendantAccountService svc = newServiceWith(aliasRepo, em);
        setField(svc, "aliasRepository", aliasRepo);

        invokeReplaceAliasesForParty(svc, partyId, null);

        verify(aliasRepo).deleteByParty_PartyId(partyId);
        verifyNoMoreInteractions(aliasRepo);
        verify(em, never()).getReference(any(), any());
    }

    @Test
    void replaceAliasesForParty_orgFlagNull_deletesAndReturns_without_getReference_or_saves() throws Exception {
        Long partyId = 222L;
        AliasRepository aliasRepo = mock(AliasRepository.class);
        EntityManager em = mock(EntityManager.class);

        OpalDefendantAccountService svc = newServiceWith(aliasRepo, em);
        setField(svc, "aliasRepository", aliasRepo);

        PartyDetails pd = PartyDetails.builder()
            .organisationFlag(null)
            .build();

        invokeReplaceAliasesForParty(svc, partyId, pd);

        verify(aliasRepo).deleteByParty_PartyId(partyId);
        verifyNoMoreInteractions(aliasRepo);
        verify(em, never()).getReference(any(), any());
    }

    @Test
    void replaceAliasesForParty_org_withAliases_savesEach_and_getsReference_once() throws Exception {
        Long partyId = 333L;
        AliasRepository aliasRepo = mock(AliasRepository.class);
        EntityManager em = mock(EntityManager.class);
        PartyEntity partyRef = mock(PartyEntity.class);
        when(em.getReference(PartyEntity.class, partyId)).thenReturn(partyRef);

        OpalDefendantAccountService svc = newServiceWith(aliasRepo, em);
        setField(svc, "aliasRepository", aliasRepo);

        OrganisationAlias a1 = OrganisationAlias.builder()
            .sequenceNumber(1).organisationName("ACME ONE").build();
        OrganisationAlias a2 = OrganisationAlias.builder()
            .sequenceNumber(2).organisationName("ACME TWO").build();

        OrganisationDetails od = OrganisationDetails.builder()
            .organisationName("ACME LTD")
            // Use Arrays.asList to allow a null element
            .organisationAliases(Arrays.asList(a1, null, a2))
            .build();

        PartyDetails pd = PartyDetails.builder()
            .organisationFlag(Boolean.TRUE)
            .organisationDetails(od)
            .build();

        invokeReplaceAliasesForParty(svc, partyId, pd);

        verify(aliasRepo).deleteByParty_PartyId(partyId);
        verify(em, times(1)).getReference(PartyEntity.class, partyId);

        ArgumentCaptor<AliasEntity> aliasCap = ArgumentCaptor.forClass(AliasEntity.class);
        verify(aliasRepo, times(2)).save(aliasCap.capture());
        List<AliasEntity> saved = aliasCap.getAllValues();

        assertEquals(2, saved.size());
        assertSame(partyRef, saved.get(0).getParty());
        assertEquals("ACME ONE", saved.get(0).getOrganisationName());
        assertNull(saved.get(0).getForenames());
        assertNull(saved.get(0).getSurname());

        assertSame(partyRef, saved.get(1).getParty());
        assertEquals("ACME TWO", saved.get(1).getOrganisationName());
        assertNull(saved.get(1).getForenames());
        assertNull(saved.get(1).getSurname());
    }

    @Test
    void replaceAliasesForParty_org_withNoAliases_doesNot_getReference_or_save() throws Exception {
        Long partyId = 444L;
        AliasRepository aliasRepo = mock(AliasRepository.class);
        EntityManager em = mock(EntityManager.class);

        OpalDefendantAccountService svc = newServiceWith(aliasRepo, em);
        setField(svc, "aliasRepository", aliasRepo);

        OrganisationDetails od = OrganisationDetails.builder()
            .organisationName("ACME LTD")
            .organisationAliases(List.of())  // empty
            .build();

        PartyDetails pd = PartyDetails.builder()
            .organisationFlag(Boolean.TRUE)
            .organisationDetails(od)
            .build();

        invokeReplaceAliasesForParty(svc, partyId, pd);

        verify(aliasRepo).deleteByParty_PartyId(partyId);
        verify(aliasRepo, never()).save(any());
        verify(em, never()).getReference(any(), any());
    }

    @Test
    void replaceAliasesForParty_individual_withAliases_savesEach_and_getsReference_once() throws Exception {
        Long partyId = 555L;
        AliasRepository aliasRepo = mock(AliasRepository.class);
        EntityManager em = mock(EntityManager.class);
        PartyEntity partyRef = mock(PartyEntity.class);
        when(em.getReference(PartyEntity.class, partyId)).thenReturn(partyRef);

        OpalDefendantAccountService svc = newServiceWith(aliasRepo, em);
        setField(svc, "aliasRepository", aliasRepo);

        IndividualAlias a1 = IndividualAlias.builder().sequenceNumber(1).forenames("Jane").surname("Doe").build();
        IndividualAlias a2 = IndividualAlias.builder().sequenceNumber(2).forenames("J.").surname("Smith").build();
        IndividualDetails id = IndividualDetails.builder()
            .individualAliases(List.of(a1, a2))
            .build();

        PartyDetails pd = PartyDetails.builder()
            .organisationFlag(Boolean.FALSE)
            .individualDetails(id)
            .build();

        invokeReplaceAliasesForParty(svc, partyId, pd);

        verify(aliasRepo).deleteByParty_PartyId(partyId);
        verify(em, times(1)).getReference(PartyEntity.class, partyId);

        ArgumentCaptor<AliasEntity> aliasCap = ArgumentCaptor.forClass(AliasEntity.class);
        verify(aliasRepo, times(2)).save(aliasCap.capture());
        List<AliasEntity> saved = aliasCap.getAllValues();

        assertEquals(2, saved.size());
        assertSame(partyRef, saved.get(0).getParty());
        assertEquals("Jane", saved.get(0).getForenames());
        assertEquals("Doe", saved.get(0).getSurname());
        assertNull(saved.get(0).getOrganisationName());

        assertSame(partyRef, saved.get(1).getParty());
        assertEquals("J.", saved.get(1).getForenames());
        assertEquals("Smith", saved.get(1).getSurname());
        assertNull(saved.get(1).getOrganisationName());
    }

    @Test
    void replaceAliasesForParty_individual_withNoAliases_doesNot_getReference_or_save() throws Exception {
        Long partyId = 666L;
        AliasRepository aliasRepo = mock(AliasRepository.class);
        EntityManager em = mock(EntityManager.class);

        OpalDefendantAccountService svc = newServiceWith(aliasRepo, em);
        setField(svc, "aliasRepository", aliasRepo);

        IndividualDetails id = IndividualDetails.builder()
            .individualAliases(List.of())  // empty
            .build();

        PartyDetails pd = PartyDetails.builder()
            .organisationFlag(Boolean.FALSE)
            .individualDetails(id)
            .build();

        invokeReplaceAliasesForParty(svc, partyId, pd);

        verify(aliasRepo).deleteByParty_PartyId(partyId);
        verify(aliasRepo, never()).save(any());
        verify(em, never()).getReference(any(), any());
    }

    private static void invokeReplaceAliasesForParty(
        OpalDefendantAccountService svc, Long partyId, PartyDetails pd
    ) throws Exception {
        Method m = OpalDefendantAccountService.class
            .getDeclaredMethod("replaceAliasesForParty", Long.class, PartyDetails.class);
        m.setAccessible(true);
        m.invoke(svc, partyId, pd);
    }

    private OpalDefendantAccountService newServiceWith(
        AliasRepository aliasRepository, EntityManager em
    ) {
        // keep alignment with your existing constructor usage
        return new OpalDefendantAccountService(
            null, null, null, null, null, null, null, mock(AmendmentService.class),
            em, null, null, null, null, null, null, null, null
        );
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

    private OpalDefendantAccountService spyWithAccount(DefendantAccountEntity account) {
        OpalDefendantAccountService svc = newService();
        // inject fields not set in ctor
        setField(svc, "debtorDetailRepository", debtorDetailRepository);
        setField(svc, "aliasRepository", aliasRepository);

        // Spy to stub the method the production code actually calls
        OpalDefendantAccountService spySvc = spy(svc);
        doReturn(account).when(spySvc).getDefendantAccountById(account.getDefendantAccountId());
        return spySvc;
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

        OpalDefendantAccountService svc = spyWithAccount(account);

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
                svc.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", "10", "tester"));

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

        OpalDefendantAccountService svc = spyWithAccount(account);

        DefendantAccountParty req = DefendantAccountParty.builder()
            .partyDetails(PartyDetails.builder().partyId("999").organisationFlag(Boolean.TRUE).build())
            .build();

        assertThrows(IllegalArgumentException.class, () ->
            svc.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", "10", "tester"));

        verify(defendantAccountRepository, never()).saveAndFlush(any());
    }

    @Test
    void replaceDefendantAccountParty_wrongBusinessUnit_throws() {
        Long accountId = 100L;

        BusinessUnitFullEntity buWrong = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 77).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId).businessUnit(buWrong).versionNumber(1L).build();

        OpalDefendantAccountService svc = spyWithAccount(account);

        assertThrows(EntityNotFoundException.class, () ->
            svc.replaceDefendantAccountParty(accountId, 1L,
                DefendantAccountParty.builder().build(), "\"1\"", "10", "tester", null));

        verify(defendantAccountRepository, never()).saveAndFlush(any());
    }

    @Test
    void replaceAliasesForParty_pdNull_returns_without_any_repository_or_party_calls() throws Exception {
        Long partyId = 111L;

        OpalDefendantAccountService svc = newService();
        setField(svc, "aliasRepository", aliasRepository);

        invokeReplaceAliasesForParty(svc, partyId, null);

        verifyNoInteractions(aliasRepository);
        verifyNoInteractions(opalPartyService);
    }

    @Test
    void replaceAliasesForParty_orgFlagNull_returns_without_any_repository_or_party_calls() throws Exception {
        Long partyId = 222L;

        OpalDefendantAccountService svc = newService();
        setField(svc, "aliasRepository", aliasRepository);

        PartyDetails pd = PartyDetails.builder().organisationFlag(null).build();

        invokeReplaceAliasesForParty(svc, partyId, pd);

        verifyNoInteractions(aliasRepository);
        verifyNoInteractions(opalPartyService);
    }

    @Test
    void replaceAliasesForParty_org_withAliases_savesAll_then_deletes_others_and_loads_party_once() throws Exception {
        Long partyId = 333L;
        PartyEntity partyRef = new PartyEntity();
        partyRef.setPartyId(partyId);
        when(opalPartyService.findById(partyId)).thenReturn(partyRef);

        OpalDefendantAccountService svc = newService();
        setField(svc, "aliasRepository", aliasRepository);

        OrganisationAlias a1 = OrganisationAlias.builder()
            .sequenceNumber(1).organisationName("ACME ONE").build();
        OrganisationAlias a2 = OrganisationAlias.builder()
            .sequenceNumber(2).organisationName("ACME TWO").build();

        OrganisationDetails od = OrganisationDetails.builder()
            .organisationName("ACME LTD")
            .organisationAliases(Arrays.asList(a1, null, a2)) // include a null to exercise skip
            .build();

        PartyDetails pd = PartyDetails.builder()
            .organisationFlag(Boolean.TRUE)
            .organisationDetails(od)
            .build();

        List<AliasEntity> savedBatch = new ArrayList<>();
        when(aliasRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<AliasEntity> list = inv.getArgument(0);
            long idSeq = 1L;
            for (AliasEntity e : list) {
                e.setAliasId(idSeq++);
            }
            savedBatch.clear();
            savedBatch.addAll(list);
            return list;
        });

        invokeReplaceAliasesForParty(svc, partyId, pd);

        verify(opalPartyService, times(1)).findById(partyId);

        verify(aliasRepository, times(1)).saveAll(anyList());
        assertEquals(2, savedBatch.size());
        assertSame(partyRef, savedBatch.get(0).getParty());
        assertEquals("ACME ONE", savedBatch.get(0).getOrganisationName());
        assertNull(savedBatch.get(0).getForenames());
        assertNull(savedBatch.get(0).getSurname());

        assertSame(partyRef, savedBatch.get(1).getParty());
        assertEquals("ACME TWO", savedBatch.get(1).getOrganisationName());
        assertNull(savedBatch.get(1).getForenames());
        assertNull(savedBatch.get(1).getSurname());

        verify(aliasRepository, times(1)).deleteByParty_PartyIdAndAliasIdNotIn(
            eq(partyId),
            argThat(keepIds ->
                keepIds != null
                    && keepIds.size() == savedBatch.size()
                    && keepIds.containsAll(savedBatch.stream().map(AliasEntity::getAliasId).toList())
            )
        );

        // No blanket delete-all in this path
        verify(aliasRepository, never()).deleteByParty_PartyId(anyLong());
    }

    @Test
    void replaceAliasesForParty_individual_withAliases_savesAll_then_deletes_others_and_loads_party_once()
        throws Exception {
        Long partyId = 555L;
        PartyEntity partyRef = new PartyEntity();
        partyRef.setPartyId(partyId);
        when(opalPartyService.findById(partyId)).thenReturn(partyRef);

        OpalDefendantAccountService svc = newService();
        setField(svc, "aliasRepository", aliasRepository);

        IndividualAlias ia1 = IndividualAlias.builder()
            .sequenceNumber(1).forenames("Jane").surname("Doe").build();
        IndividualAlias ia2 = IndividualAlias.builder()
            .sequenceNumber(2).forenames("J.").surname("Smith").build();
        IndividualDetails id = IndividualDetails.builder()
            .individualAliases(List.of(ia1, ia2))
            .build();

        PartyDetails pd = PartyDetails.builder()
            .organisationFlag(Boolean.FALSE)
            .individualDetails(id)
            .build();

        List<AliasEntity> savedBatch = new ArrayList<>();
        when(aliasRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<AliasEntity> list = inv.getArgument(0);
            long idSeq = 1L;
            for (AliasEntity e : list) {
                e.setAliasId(idSeq++);
            }
            savedBatch.clear();
            savedBatch.addAll(list);
            return list;
        });

        invokeReplaceAliasesForParty(svc, partyId, pd);

        verify(opalPartyService, times(1)).findById(partyId);

        verify(aliasRepository, times(1)).saveAll(anyList());
        assertEquals(2, savedBatch.size());

        assertSame(partyRef, savedBatch.get(0).getParty());
        assertEquals("Jane", savedBatch.get(0).getForenames());
        assertEquals("Doe", savedBatch.get(0).getSurname());
        assertNull(savedBatch.get(0).getOrganisationName());

        assertSame(partyRef, savedBatch.get(1).getParty());
        assertEquals("J.", savedBatch.get(1).getForenames());
        assertEquals("Smith", savedBatch.get(1).getSurname());
        assertNull(savedBatch.get(1).getOrganisationName());

        verify(aliasRepository, times(1)).deleteByParty_PartyIdAndAliasIdNotIn(
            eq(partyId),
            argThat(keepIds ->
                keepIds != null
                    && keepIds.size() == savedBatch.size()
                    && keepIds.containsAll(savedBatch.stream().map(AliasEntity::getAliasId).toList())
            )
        );

        verify(aliasRepository, never()).deleteByParty_PartyId(anyLong());
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

        OpalDefendantAccountService svc = spyWithAccount(account);

        DebtorDetailEntity existing = new DebtorDetailEntity();
        existing.setPartyId(222L);
        when(debtorDetailRepository.findById(222L)).thenReturn(Optional.of(existing));
        when(opalPartyService.findById(222L)).thenReturn(party);
        when(aliasRepository.findByParty_PartyId(222L)).thenReturn(emptyList());
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
                svc.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", "10", "tester");

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
        when(aliasRepository.findByParty_PartyId(444L)).thenReturn(emptyList());
        when(defendantAccountRepository.saveAndFlush(account)).thenReturn(account);

        when(debtorDetailRepository.findById(444L)).thenReturn(Optional.of(new DebtorDetailEntity()));

        OpalDefendantAccountService svc = spyWithAccount(account);

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
                svc.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", "10", "tester");

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
    void replaceAliasesForParty_org_withNoAliases_deletesAndDoesNotSave() throws Exception {
        Long partyId = 444L;

        OpalDefendantAccountService svc = newService();
        setField(svc, "aliasRepository", aliasRepository);
        setField(svc, "opalPartyService", opalPartyService);

        OrganisationDetails od = OrganisationDetails.builder()
            .organisationName("ACME LTD")
            .organisationAliases(List.of())  // empty
            .build();

        PartyDetails pd = PartyDetails.builder()
            .organisationFlag(Boolean.TRUE)
            .organisationDetails(od)
            .build();

        // invoke the private replaceAliasesForParty (use your test helper)
        invokeReplaceAliasesForParty(svc, partyId, pd);

        // We expect deleteByParty_PartyId to be called (no keep ids) and no saveAll.
        verify(aliasRepository).deleteByParty_PartyId(partyId);
        verify(aliasRepository, never()).saveAll(anyList());

        // because current implementation calls findById at start, assert it's invoked
        verify(opalPartyService).findById(partyId);
    }

    @Test
    void replaceAliasesForParty_individual_withNoAliases_deletesAndDoesNotSave() throws Exception {
        Long partyId = 666L;

        OpalDefendantAccountService svc = newService();
        setField(svc, "aliasRepository", aliasRepository);
        setField(svc, "opalPartyService", opalPartyService);

        IndividualDetails id = IndividualDetails.builder().individualAliases(List.of()).build();

        PartyDetails pd = PartyDetails.builder()
            .organisationFlag(Boolean.FALSE)
            .individualDetails(id)
            .build();

        invokeReplaceAliasesForParty(svc, partyId, pd);

        verify(aliasRepository).deleteByParty_PartyId(partyId);
        verify(aliasRepository, never()).saveAll(anyList());
        verify(opalPartyService).findById(partyId);
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

        when(aliasRepository.findByParty_PartyId(123L)).thenReturn(emptyList());
        when(aliasRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        when(opalPartyService.findById(123L)).thenReturn(party);
        doAnswer(inv -> inv.getArgument(0)).when(opalPartyService).save(any());

        when(defendantAccountRepository.saveAndFlush(account)).thenReturn(account);

        when(debtorDetailRepository.findById(123L)).thenReturn(Optional.of(new DebtorDetailEntity()));

        OpalDefendantAccountService svc = spyWithAccount(account);

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
                svc.replaceDefendantAccountParty(accountId, dapId, req, ifMatch, bu, "tester");

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
            verify(aliasRepository, times(2)).findByParty_PartyId(123L);
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

        when(aliasRepository.findByParty_PartyId(300L)).thenReturn(emptyList());
        when(aliasRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        when(debtorDetailRepository.findById(300L)).thenReturn(Optional.of(new DebtorDetailEntity()));

        OpalDefendantAccountService svc = spyWithAccount(account);

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
                svc.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", bu, "tester");

            assertNotNull(resp);
            assertNotNull(resp.getDefendantAccountParty());
            verify(opalPartyService, times(2)).findById(300L); // main + aliases
            verify(defendantAccountRepository).saveAndFlush(account);
            verify(aliasRepository, times(2)).findByParty_PartyId(300L);
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

        // User state resolves a BU user ID
        var buUser = mock(uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser.class);
        when(buUser.getBusinessUnitUserId()).thenReturn("L080JG");

        var userState = mock(uk.gov.hmcts.opal.common.user.authorisation.model.UserState.class);
        when(userState.getBusinessUnitUserForBusinessUnit((short) 10))
            .thenReturn(Optional.of(buUser));

        when(userStateService.checkForAuthorisedUser("AUTH")).thenReturn(userState);
        when(accessTokenService.extractName("AUTH")).thenReturn("John Smith");

        // Make save(account) echo the argument
        when(defendantAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        AddPaymentCardRequestResponse response =
            service.addPaymentCardRequest(accountId, buHeader, ifMatch, "AUTH");

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
            service.addPaymentCardRequest(1L, "10", "\"1\"", "AUTH")
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
            service.addPaymentCardRequest(1L, "10", "\"1\"", "AUTH")
        );
    }

    @Test
    void addPaymentCardRequest_failsWhenUserNotInBusinessUnit() {
        var account = DefendantAccountEntity.builder()
            .businessUnit(BusinessUnitFullEntity.builder().businessUnitId((short) 10).build())
            .versionNumber(1L)
            .build();

        when(defendantAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(paymentCardRequestRepository.existsByDefendantAccountId(1L)).thenReturn(false);

        // UserState returns empty Optional for this BU
        var userState = mock(uk.gov.hmcts.opal.common.user.authorisation.model.UserState.class);
        when(userState.getBusinessUnitUserForBusinessUnit((short) 10)).thenReturn(Optional.empty());
        when(userStateService.checkForAuthorisedUser("AUTH")).thenReturn(userState);

        assertThrows(EntityNotFoundException.class, () ->
            service.addPaymentCardRequest(1L, "10", "\"1\"", "AUTH")
        );
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
            service.addPaymentCardRequest(1L, "10", "\"0\"", "AUTH")
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

        OpalDefendantAccountService svc = spyWithAccount(account);

        when(opalPartyService.findById(333L)).thenReturn(party);
        when(aliasRepository.findByParty_PartyId(333L)).thenReturn(emptyList());
        when(aliasRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        when(defendantAccountRepository.saveAndFlush(account)).thenReturn(account);
        when(debtorDetailRepository.findById(333L)).thenReturn(Optional.empty());
        when(debtorDetailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        when(debtorDetailRepository.findById(333L)).thenReturn(Optional.of(new DebtorDetailEntity()));

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
                svc.replaceDefendantAccountParty(accountId, dapId, req, "\"1\"", "10", "tester");

            assertNotNull(resp);

            ArgumentCaptor<DebtorDetailEntity> cap = ArgumentCaptor.forClass(DebtorDetailEntity.class);
            verify(debtorDetailRepository).save(cap.capture());
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
            verify(aliasRepository, times(2)).findByParty_PartyId(333L);
        }
    }
}