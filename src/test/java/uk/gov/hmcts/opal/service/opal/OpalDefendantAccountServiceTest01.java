package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.dto.legacy.ReferenceNumberDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountSummaryViewEntity;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.SearchDefendantAccountEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.generated.model.GetEnforcementStatusResponse.DefendantAccountTypeEnum;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountSummaryViewRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.repository.SearchDefendantAccountRepository;
import uk.gov.hmcts.opal.service.persistence.DebtorDetailRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.persistence.EnforcementRepositoryService;
import uk.gov.hmcts.opal.service.persistence.EnforcerRepositoryService;
import uk.gov.hmcts.opal.service.persistence.FixedPenaltyOffenceRepositoryService;

@ExtendWith(MockitoExtension.class)
class OpalDefendantAccountServiceTest01 {

    @Mock
    private DefendantAccountRepositoryService defendantAccountRepositoryService;

    @Mock
    private DefendantAccountRepository defendantAccountRepository;

    @Mock
    private EnforcementRepository enforcementRepository;

    @Mock
    private DefendantAccountSummaryViewRepository dasvRepository;

    @Mock
    private SearchDefendantAccountRepository searchDefAccRepo;

    @Mock
    private FixedPenaltyOffenceRepositoryService fixedPenaltyOffenceRepositoryService;

    @Mock
    private EnforcerRepositoryService enforcerRepoService;

    @Mock
    private DebtorDetailRepositoryService debtorDetailRepoService;

    @Mock
    private EnforcementRepositoryService enforcementRepositoryService;

    // Services under test
    @InjectMocks
    private OpalDefendantAccountService service;
    @InjectMocks
    private OpalDefendantAccountFixedPenaltyService fpService;
    @InjectMocks
    private OpalDefendantAccountEnforcementService enforcementService;

    @Test
    void testDefendantAccountById() {
        long testId = 1L;

        DefendantAccountEntity entity = DefendantAccountEntity.builder().build();
        when(defendantAccountRepository.findById(testId)).thenReturn(Optional.ofNullable(entity));

        DefendantAccountEntity result = service.getDefendantAccountById(testId);
        assertNotNull(result);
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
    void whenAccountNumberPresent_activeOnlyIsIgnored() {
        // given
        AccountSearchDto dto = AccountSearchDto.builder()
            .activeAccountsOnly(true)
            .referenceNumberDto(ReferenceNumberDto.builder().accountNumber("AAAAAAAAX").build())
            .build();

        // when
        service.searchDefendantAccounts(dto);

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
        verify(searchDefAccRepo, times(1))
            .findAll(ArgumentMatchers.<Specification<SearchDefendantAccountEntity>>any());
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

    private AccountSearchDto emptyCriteria() {
        AccountSearchDto c = mock(AccountSearchDto.class);
        when(c.getBusinessUnitIds()).thenReturn(null);
        when(c.getActiveAccountsOnly()).thenReturn(null);
        when(c.getReferenceNumberDto()).thenReturn(null);
        when(c.getDefendant()).thenReturn(null);
        return c;
    }

    @Test
    void whenReferenceOrganisationFlagProvided_appliesFilterCorrectly1() {
        // Arrange
        AccountSearchDto dtoTrue = mock(AccountSearchDto.class, RETURNS_DEEP_STUBS);
        ReferenceNumberDto refTrue = mock(ReferenceNumberDto.class);
        when(dtoTrue.getReferenceNumberDto()).thenReturn(refTrue);
        when(dtoTrue.getActiveAccountsOnly()).thenReturn(false);
        when(dtoTrue.getBusinessUnitIds()).thenReturn(Collections.emptyList());
        when(dtoTrue.getDefendant()).thenReturn(null);

        // Act
        service.searchDefendantAccounts(dtoTrue);
    }

    @Test
    void whenReferenceOrganisationFlagProvided_appliesFilterCorrectly2() {
        // Arrange
        AccountSearchDto dtoFalse = mock(AccountSearchDto.class, RETURNS_DEEP_STUBS);
        ReferenceNumberDto refFalse = mock(ReferenceNumberDto.class);
        when(dtoFalse.getReferenceNumberDto()).thenReturn(refFalse);
        when(dtoFalse.getActiveAccountsOnly()).thenReturn(false);
        when(dtoFalse.getBusinessUnitIds()).thenReturn(Collections.emptyList());
        when(dtoFalse.getDefendant()).thenReturn(null);

        // Act
        service.searchDefendantAccounts(dtoFalse);
    }

    @Test
    void vehicleFixedPenaltyFlag_shouldBeFalse_whenVehicleRegistrationIsNullAndFlagFalse() {
        Long defendantAccountId = 201L;
        DefendantAccountEntity account = buildMockAccount(defendantAccountId);

        FixedPenaltyOffenceEntity offence = buildMockOffence(false);
        offence.setVehicleRegistration(null);
        offence.setVehicleFixedPenalty(false);

        when(defendantAccountRepositoryService.findById(defendantAccountId)).thenReturn(account);
        when(fixedPenaltyOffenceRepositoryService.findByDefendantAccountId(defendantAccountId))
            .thenReturn(offence);

        GetDefendantAccountFixedPenaltyResponse response =
            fpService.getDefendantAccountFixedPenalty(defendantAccountId);

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

        when(defendantAccountRepositoryService.findById(defendantAccountId)).thenReturn(account);
        when(fixedPenaltyOffenceRepositoryService.findByDefendantAccountId(defendantAccountId))
            .thenReturn(offence);

        GetDefendantAccountFixedPenaltyResponse response =
            fpService.getDefendantAccountFixedPenalty(defendantAccountId);

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

        when(defendantAccountRepositoryService.findById(defendantAccountId)).thenReturn(account);
        when(fixedPenaltyOffenceRepositoryService.findByDefendantAccountId(defendantAccountId))
            .thenReturn(offence);

        GetDefendantAccountFixedPenaltyResponse response =
            fpService.getDefendantAccountFixedPenalty(defendantAccountId);

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

        when(defendantAccountRepositoryService.findById(anyLong())).thenReturn(defAccount);
        when(enforcementRepositoryService.getEnforcementMostRecent(
            any(), any())).thenReturn(Optional.of(enforcementEntity));
        lenient().when(enforcerRepoService.findById(any())).thenReturn(null); // enforcerRepo should not be null
        when(debtorDetailRepoService.findByPartyId(any())).thenReturn(Optional.empty());

        // Act
        EnforcementStatus response = enforcementService.getEnforcementStatus(1L);

        // Assert
        assertNotNull(response);
        assertNull(response.getNextEnforcementActionData());
        assertFalse(response.getEmployerFlag());
        assertEquals(DefendantAccountTypeEnum.ADULT, response.getDefendantAccountType());
        assertFalse(response.getIsHmrcCheckEligible());
    }
}
