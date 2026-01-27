package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.PaymentCardRequestEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;
import uk.gov.hmcts.opal.service.DefendantAccountPaymentTermsService;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.persistence.AmendmentRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.persistence.PaymentCardRequestRepositoryService;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountPaymentTermsServiceProxy;

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

    @InjectMocks
    OpalDefendantAccountPaymentTermsService service;

    @Test
    void addPaymentCardRequest_happyPath_createsPCRAndUpdatesAccount() {
        Long accountId = 99L;

        BusinessUnitFullEntity bu = BusinessUnitFullEntity.builder()
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
        when(accessTokenService.extractName("AUTH"))
            .thenReturn("John Smith");
        when(defendantAccountRepositoryService.save(any()))
            .thenAnswer(inv -> inv.getArgument(0));

        AddPaymentCardRequestResponse response = service.addPaymentCardRequest(
            accountId, "10", "L080JG", "\"1\"", "AUTH"
        );

        assertNotNull(response);
        assertEquals(accountId, response.getDefendantAccountId());
        assertTrue(account.getPaymentCardRequested());
        assertEquals("L080JG", account.getPaymentCardRequestedBy());
        assertEquals("John Smith", account.getPaymentCardRequestedByName());

        verify(paymentCardRequestRepositoryService).save(any(PaymentCardRequestEntity.class));
    }

    @Test
    void addPaymentCardRequest_failsWhenBusinessUnitMismatch() {
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .businessUnit(BusinessUnitFullEntity.builder().businessUnitId((short) 77).build())
            .versionNumber(1L)
            .build();

        when(defendantAccountRepositoryService.findById(1L)).thenReturn(account);

        assertThrows(EntityNotFoundException.class, () ->
            service.addPaymentCardRequest(1L, "10", null, "\"1\"", "AUTH")
        );
    }

    @Test
    void addPaymentCardRequest_allowsNullBusinessUnitUserId_whenUserNotInBusinessUnit() {
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .businessUnit(BusinessUnitFullEntity.builder().businessUnitId((short) 10).build())
            .versionNumber(1L)
            .build();

        when(defendantAccountRepositoryService.findById(1L)).thenReturn(account);
        when(paymentCardRequestRepositoryService.existsByDefendantAccountId(1L)).thenReturn(false);
        when(defendantAccountRepositoryService.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() ->
            service.addPaymentCardRequest(1L, "10", null, "\"1\"", "AUTH")
        );

        assertTrue(account.getPaymentCardRequested());
        assertNull(account.getPaymentCardRequestedBy());
    }

    @Test
    void addPaymentCardRequest_versionConflictThrows() {
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .businessUnit(BusinessUnitFullEntity.builder().businessUnitId((short) 10).build())
            .versionNumber(5L)  // expected If-Match
            .build();

        when(defendantAccountRepositoryService.findById(1L)).thenReturn(account);

        assertThrows(ObjectOptimisticLockingFailureException.class, () ->
            service.addPaymentCardRequest(1L, "10", null, "\"0\"", "AUTH")
        );
    }

    @Test
    void addPaymentCardRequest_newSignature_failsWhenBusinessUnitMismatch() {
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .businessUnit(BusinessUnitFullEntity.builder().businessUnitId((short) 20).build())
            .versionNumber(1L)
            .build();

        when(defendantAccountRepositoryService.findById(1L)).thenReturn(account);

        assertThrows(EntityNotFoundException.class, () ->
            service.addPaymentCardRequest(1L, "10", "BU-USER-123", "\"1\"", "AUTH")
        );

        verify(defendantAccountRepositoryService, never()).save(any());
    }


    @Test
    void addPaymentCardRequest_newSignature_succeedsWhenBusinessUnitMatches() {
        Long accountId = 99L;

        BusinessUnitFullEntity bu = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10).build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(accountId)
            .businessUnit(bu)
            .versionNumber(1L)
            .build();

        when(defendantAccountRepositoryService.findById(accountId)).thenReturn(account);
        when(paymentCardRequestRepositoryService.existsByDefendantAccountId(accountId)).thenReturn(false);
        when(accessTokenService.extractName("AUTH")).thenReturn("John Smith");
        when(defendantAccountRepositoryService.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AddPaymentCardRequestResponse response = service.addPaymentCardRequest(
            accountId, "10", "L080JG", "\"1\"", "AUTH"
        );

        assertNotNull(response);
        assertEquals(accountId, response.getDefendantAccountId());
        assertTrue(account.getPaymentCardRequested());
        assertEquals("L080JG", account.getPaymentCardRequestedBy());
        assertEquals("John Smith", account.getPaymentCardRequestedByName());

        verify(paymentCardRequestRepositoryService).save(any(PaymentCardRequestEntity.class));
        verify(defendantAccountRepositoryService).save(account);
    }

    @Test
    void addPaymentCardRequest_permissionDenied_throws403() {
        DefendantAccountPaymentTermsServiceProxy proxy = mock(DefendantAccountPaymentTermsServiceProxy.class);

        UserState userState = mock(UserState.class);
        when(userStateService.checkForAuthorisedUser("AUTH"))
            .thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.AMEND_PAYMENT_TERMS))
            .thenReturn(false);

        var svc = new DefendantAccountPaymentTermsService(proxy, userStateService);

        assertThrows(PermissionNotAllowedException.class,
            () -> svc.addPaymentCardRequest(1L, "10", "USR", "\"1\"", "AUTH")
        );

        verifyNoInteractions(proxy);
    }
}
