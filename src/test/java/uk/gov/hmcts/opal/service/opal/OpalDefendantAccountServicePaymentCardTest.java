package uk.gov.hmcts.opal.service.opal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.controllers.advice.GlobalExceptionHandler.PaymentCardRequestAlreadyExistsException;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.entity.PaymentCardRequestEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.exception.BusinessUnitUserNotFoundException;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.persistence.AmendmentRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.persistence.PaymentCardRequestRepositoryService;

@ExtendWith(MockitoExtension.class)
class OpalDefendantAccountServicePaymentCardTest {

    @Mock
    DefendantAccountRepositoryService defendantAccountRepositoryService;
    @Mock
    PaymentCardRequestRepositoryService paymentCardRequestRepositoryService;
    @Mock
    AmendmentRepositoryService amendmentRepositoryService;
    @Mock AccessTokenService accessTokenService;
    @Mock UserStateService userStateService;
    @Mock UserState userState;

    @InjectMocks
    OpalDefendantAccountPaymentTermsService service;

    @Test
    void addPaymentCardRequest_happyPath_createsPCRAndUpdatesAccount() {
        Long accountId = 99L;

        BusinessUnitEntity bu = BusinessUnitEntity.builder()
            .businessUnitId((short) 10).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId)
            .businessUnit(bu)
            .versionNumber(1L)
            .build();

        when(defendantAccountRepositoryService.findById(accountId))
            .thenReturn(account);
        when(paymentCardRequestRepositoryService.existsByDefendantAccountId(accountId))
            .thenReturn(false);
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.getDisplayName()).thenReturn("John Smith");
        when(defendantAccountRepositoryService.save(any()))
            .thenAnswer(inv -> inv.getArgument(0));

        AddPaymentCardRequestResponse response = service.addPaymentCardRequest(
            accountId, "10", "L080JG", "\"1\""
        );

        assertNotNull(response);
        assertEquals(accountId, response.getDefendantAccountId());
        assertTrue(account.getPaymentCardRequested());
        assertEquals("L080JG", account.getPaymentCardRequestedBy());
        assertEquals("John Smith", account.getPaymentCardRequestedByName());

        verify(paymentCardRequestRepositoryService).save(any(PaymentCardRequestEntity.class));
        verify(amendmentRepositoryService).auditFinaliseStoredProc(
            accountId,
            RecordType.DEFENDANT_ACCOUNTS,
            (short) 10,
            "L080JG",
            "John Smith",
            null,
            "ACCOUNT_ENQUIRY"
        );
    }

    @Test
    void addPaymentCardRequest_failsWhenPcrAlreadyExists() {
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 10).build())
            .versionNumber(1L)
            .build();

        when(defendantAccountRepositoryService.findById(1L)).thenReturn(account);
        when(paymentCardRequestRepositoryService.existsByDefendantAccountId(1L)).thenReturn(true);

        assertThrows(PaymentCardRequestAlreadyExistsException.class, () ->
            service.addPaymentCardRequest(1L, "10", "L080JG", "\"1\"")
        );
    }

    @Test
    void addPaymentCardRequest_failsWhenBusinessUnitMismatch() {
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 77).build())
            .versionNumber(1L)
            .build();

        when(defendantAccountRepositoryService.findById(1L)).thenReturn(account);

        assertThrows(EntityNotFoundException.class, () ->
            service.addPaymentCardRequest(1L, "10", "L080JG", "\"1\"")
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void addPaymentCardRequest_missingBusinessUnitUserId_throws401BeforePersistence(String businessUnitUserId) {
        // Arrange

        // Act
        BusinessUnitUserNotFoundException ex = assertThrows(
            BusinessUnitUserNotFoundException.class,
            () -> service.addPaymentCardRequest(1L, "10", businessUnitUserId, "\"1\"")
        );

        // Assert
        assertThat(ex.getBusinessUnitId()).isEqualTo((short) 10);
        verifyNoInteractions(
            defendantAccountRepositoryService,
            paymentCardRequestRepositoryService,
            amendmentRepositoryService,
            userStateService
        );
    }

    @Test
    void addPaymentCardRequest_versionConflictThrows() {
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 10).build())
            .versionNumber(5L)  // expected If-Match
            .build();

        when(defendantAccountRepositoryService.findById(1L)).thenReturn(account);

        assertThrows(ObjectOptimisticLockingFailureException.class, () ->
            service.addPaymentCardRequest(1L, "10", "L080JG", "\"0\"")
        );
    }

    @Test
    void addPaymentCardRequest_newSignature_failsWhenBusinessUnitMismatch() {
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 20).build())
            .versionNumber(1L)
            .build();

        when(defendantAccountRepositoryService.findById(1L)).thenReturn(account);

        assertThrows(EntityNotFoundException.class, () ->
            service.addPaymentCardRequest(1L, "10", "BU-USER-123", "\"1\"")
        );

        verify(defendantAccountRepositoryService, never()).save(any());
    }


    @Test
    void addPaymentCardRequest_newSignature_succeedsWhenBusinessUnitMatches() {
        Long accountId = 99L;

        BusinessUnitEntity bu = BusinessUnitEntity.builder()
            .businessUnitId((short) 10).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId)
            .businessUnit(bu)
            .versionNumber(1L)
            .build();

        when(defendantAccountRepositoryService.findById(accountId)).thenReturn(account);
        when(paymentCardRequestRepositoryService.existsByDefendantAccountId(accountId)).thenReturn(false);
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.getDisplayName()).thenReturn("John Smith");
        when(defendantAccountRepositoryService.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AddPaymentCardRequestResponse response = service.addPaymentCardRequest(
            accountId, "10", "L080JG", "\"1\""
        );

        assertNotNull(response);
        assertEquals(accountId, response.getDefendantAccountId());
        assertTrue(account.getPaymentCardRequested());
        assertEquals("L080JG", account.getPaymentCardRequestedBy());
        assertEquals("John Smith", account.getPaymentCardRequestedByName());

        verify(paymentCardRequestRepositoryService).save(any(PaymentCardRequestEntity.class));
        verify(defendantAccountRepositoryService).save(account);
    }
}
