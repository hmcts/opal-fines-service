package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountSummaryViewEntity;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.generated.model.GetEnforcementStatusResponse.DefendantAccountTypeEnum;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountSummaryViewRepository;
import uk.gov.hmcts.opal.service.persistence.DebtorDetailRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.persistence.EnforcementRepositoryService;
import uk.gov.hmcts.opal.service.persistence.EnforcerRepositoryService;
import uk.gov.hmcts.opal.service.persistence.FixedPenaltyOffenceRepositoryService;

@ExtendWith(MockitoExtension.class)
class OpalDefendantAccountServiceTest {

    @Mock
    private DefendantAccountRepositoryService defendantAccountRepositoryService;

    @Mock
    private DefendantAccountRepository defendantAccountRepository;


    @Mock
    private DefendantAccountSummaryViewRepository dasvRepository;

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
    void vehicleFixedPenaltyFlag_shouldBeFalse_whenVehicleRegistrationIsNullAndFlagFalse() {
        Long defendantAccountId = 201L;
        DefendantAccountEntity account = buildMockAccount(defendantAccountId);

        FixedPenaltyOffenceEntity offence = buildMockOffence();
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

        FixedPenaltyOffenceEntity offence = buildMockOffence();
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

        FixedPenaltyOffenceEntity offence = buildMockOffence();
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

    private FixedPenaltyOffenceEntity buildMockOffence() {
        return FixedPenaltyOffenceEntity.builder()
            .ticketNumber("888")
            .vehicleRegistration(null)
            .offenceLocation("London")
            .noticeNumber("PN98765")
            .issuedDate(LocalDate.of(2024, 1, 1))
            .licenceNumber("DOE1234567")
            .vehicleFixedPenalty(false)
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
