package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.GetDefendantAccountConsolidatedAccountsResult;
import uk.gov.hmcts.opal.entity.defendantaccount.ConsolidatedAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountStatus;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.exception.DefendantAccountNotFoundException;
import uk.gov.hmcts.opal.generated.model.ConsolidatedAccountDefendantAccount;
import uk.gov.hmcts.opal.generated.model.GetEnforcementStatusResponse.DefendantAccountTypeEnum;
import uk.gov.hmcts.opal.mapper.ConsolidatedAccountMapper;
import uk.gov.hmcts.opal.repository.ConsolidatedAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountSummaryViewRepository;
import uk.gov.hmcts.opal.service.persistence.DebtorDetailRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.persistence.EnforcementRepositoryService;
import uk.gov.hmcts.opal.service.persistence.EnforcerRepositoryService;

@ExtendWith(MockitoExtension.class)
class OpalDefendantAccountServiceCoreTest {

    @Mock
    private DefendantAccountRepositoryService defendantAccountRepositoryService;

    @Mock
    private DefendantAccountRepository defendantAccountRepository;

    @Mock
    private ConsolidatedAccountRepository consolidatedAccountRepository;

    @Mock
    private ConsolidatedAccountMapper consolidatedAccountMapper;

    @Mock
    private DefendantAccountSummaryViewRepository dasvRepository;

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
    private OpalDefendantAccountEnforcementService enforcementService;

    @Test
    void getConsolidatedAccounts_whenMasterExists_returnsWrappedPayloadWithMasterVersion() {
        Long defendantAccountId = 123L;
        DefendantAccountEntity masterAccount = DefendantAccountEntity.builder()
            .versionNumber(12L)
            .build();
        ConsolidatedAccountEntity consolidatedAccount = ConsolidatedAccountEntity.builder()
            .masterAccountId(defendantAccountId)
            .childAccountId(456L)
            .build();
        ConsolidatedAccountDefendantAccount mappedAccount = new ConsolidatedAccountDefendantAccount()
            .accountId(456L)
            .accountNumber("ACC456");

        when(defendantAccountRepository.findById(defendantAccountId)).thenReturn(Optional.of(masterAccount));
        when(consolidatedAccountRepository.findByMasterAccountIdOrderByChildAccountIdAsc(defendantAccountId))
            .thenReturn(List.of(consolidatedAccount));
        when(consolidatedAccountMapper.toResponse(List.of(consolidatedAccount))).thenReturn(List.of(mappedAccount));

        GetDefendantAccountConsolidatedAccountsResult result =
            service.getConsolidatedAccounts(defendantAccountId);

        assertEquals(masterAccount.getVersion(), result.getVersion());
        assertEquals(List.of(mappedAccount), result.getPayload());
    }

    @Test
    void getConsolidatedAccounts_whenMasterDoesNotExist_throwsDefendantAccountNotFoundException() {
        Long defendantAccountId = 123L;
        when(defendantAccountRepository.findById(defendantAccountId)).thenReturn(Optional.empty());

        DefendantAccountNotFoundException exception = assertThrows(
            DefendantAccountNotFoundException.class,
            () -> service.getConsolidatedAccounts(defendantAccountId)
        );

        assertEquals(defendantAccountId, exception.getDefendantAccountId());
        verifyNoInteractions(consolidatedAccountRepository, consolidatedAccountMapper);
    }

    @Test
    void testGetEnforcementStatus() {
        // Arrange
        DefendantAccountEntity defAccount = DefendantAccountEntity.builder()
            .parties(List.of(
                DefendantAccountPartiesEntity.builder()
                    .associationType(AssociationType.DEFENDANT)
                    .party(PartyEntity.builder()
                        .birthDate(LocalDate.of(1990, 1, 1))
                        .build())
                    .build()))
            .defendantAccountId(1L)
            .accountStatus(DefendantAccountStatus.LIVE)
            .build();

        EnforcementEntity enforcementEntity = EnforcementEntity.builder()
                .build();

        when(defendantAccountRepositoryService.findById(anyLong())).thenReturn(defAccount);
        when(enforcementRepositoryService.getEnforcementMostRecent(
            any(), any())).thenReturn(Optional.of(enforcementEntity));
        lenient().when(enforcerRepoService.findById(any())).thenReturn(Optional.empty());
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
