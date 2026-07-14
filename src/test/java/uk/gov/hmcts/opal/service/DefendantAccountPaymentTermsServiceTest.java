package uk.gov.hmcts.opal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.Permission;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.exception.BusinessUnitUserNotFoundException;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountPaymentTermsServiceProxy;

@ExtendWith(MockitoExtension.class)
class DefendantAccountPaymentTermsServiceTest {

    @Mock
    private DefendantAccountPaymentTermsServiceProxy defendantAccountPaymentTermsServiceProxy;

    @Mock
    private UserStateService userStateService;

    @Mock
    private UserState userState;

    @InjectMocks
    private DefendantAccountPaymentTermsService defendantAccountPaymentTermsService;

    @Test
    void getPaymentTerms_whenUserHasPermission_returnsProxyResult() {
        // Arrange
        Long defendantAccountId = 77L;
        GetDefendantAccountPaymentTermsResponse proxyResponse = new GetDefendantAccountPaymentTermsResponse();
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(defendantAccountPaymentTermsServiceProxy.getPaymentTerms(defendantAccountId)).thenReturn(proxyResponse);

        // Act
        GetDefendantAccountPaymentTermsResponse result =
            defendantAccountPaymentTermsService.getPaymentTerms(defendantAccountId);

        // Assert
        assertSame(proxyResponse, result, "Should return exactly the proxy response");
        verify(userStateService).getUserStateV1FromSecurityContext();
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verify(defendantAccountPaymentTermsServiceProxy).getPaymentTerms(defendantAccountId);
        verifyNoMoreInteractions(userStateService, userState, defendantAccountPaymentTermsServiceProxy);
    }

    @Test
    void getPaymentTerms_whenUserLacksPermission_throwsPermissionNotAllowed() {
        // Arrange
        Long defendantAccountId = 77L;
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);

        // Act
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountPaymentTermsService.getPaymentTerms(defendantAccountId)
        );

        // Assert
        assertTrue(
            ex.getMessage() == null || ex.getMessage().contains(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS.name()),
            "Exception should mention the denied permission"
        );
        verify(userStateService).getUserStateV1FromSecurityContext();
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verifyNoInteractions(defendantAccountPaymentTermsServiceProxy);
        verifyNoMoreInteractions(userStateService, userState);
    }

    @Test
    void addPaymentCardRequest_userHasBusinessUnitUserAndPermission_callsProxyWithDerivedId() {
        // Arrange
        Long defendantAccountId = 77L;
        String businessUnitId = "78";
        String businessUnitUserId = "L080JG";
        String ifMatch = "\"4\"";
        AddPaymentCardRequestResponse proxyResponse = new AddPaymentCardRequestResponse(defendantAccountId);
        BusinessUnitUser businessUnitUser = BusinessUnitUser.builder()
            .businessUnitUserId(businessUnitUserId)
            .build();

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.getBusinessUnitUserForBusinessUnit((short) 78)).thenReturn(Optional.of(businessUnitUser));
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.AMEND_PAYMENT_TERMS)).thenReturn(true);
        when(defendantAccountPaymentTermsServiceProxy.addPaymentCardRequest(
            defendantAccountId,
            businessUnitId,
            businessUnitUserId,
            ifMatch
        )).thenReturn(proxyResponse);

        // Act
        AddPaymentCardRequestResponse result = defendantAccountPaymentTermsService.addPaymentCardRequest(
            defendantAccountId,
            businessUnitId,
            ifMatch
        );

        // Assert
        assertSame(proxyResponse, result);
        verify(userStateService).getUserStateV1FromSecurityContext();
        verify(userState).getBusinessUnitUserForBusinessUnit((short) 78);
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.AMEND_PAYMENT_TERMS);
        verify(defendantAccountPaymentTermsServiceProxy).addPaymentCardRequest(
            defendantAccountId,
            businessUnitId,
            businessUnitUserId,
            ifMatch
        );
        verifyNoMoreInteractions(userStateService, userState, defendantAccountPaymentTermsServiceProxy);
    }

    @Test
    void addPaymentCardRequest_permissionInDifferentBusinessUnit_throws401AndDoesNotCallProxy() {
        // Arrange
        UserState userWithPermissionInDifferentBusinessUnit = UserState.builder()
            .businessUnitUser(Set.of(BusinessUnitUser.builder()
                .businessUnitId((short) 77)
                .businessUnitUserId("L077JG")
                .permissions(Set.of(Permission.builder()
                    .permissionId(FinesPermission.AMEND_PAYMENT_TERMS.getId())
                    .permissionName(FinesPermission.AMEND_PAYMENT_TERMS.getDescription())
                    .build()))
                .build()))
            .build();
        when(userStateService.getUserStateV1FromSecurityContext())
            .thenReturn(userWithPermissionInDifferentBusinessUnit);

        // Act
        BusinessUnitUserNotFoundException ex = assertThrows(
            BusinessUnitUserNotFoundException.class,
            () -> defendantAccountPaymentTermsService.addPaymentCardRequest(77L, "78", "\"4\"")
        );

        // Assert
        assertEquals((short) 78, ex.getBusinessUnitId());
        verify(userStateService).getUserStateV1FromSecurityContext();
        verifyNoInteractions(defendantAccountPaymentTermsServiceProxy);
        verifyNoMoreInteractions(userStateService);
    }

    @Test
    void addPaymentCardRequest_businessUnitUserWithoutPermission_throws403AndDoesNotCallProxy() {
        // Arrange
        BusinessUnitUser businessUnitUser = BusinessUnitUser.builder()
            .businessUnitUserId("L080JG")
            .build();

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.getBusinessUnitUserForBusinessUnit((short) 78)).thenReturn(Optional.of(businessUnitUser));
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.AMEND_PAYMENT_TERMS)).thenReturn(false);

        // Act
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountPaymentTermsService.addPaymentCardRequest(77L, "78", "\"4\"")
        );

        // Assert
        assertThat(ex.getPermission()).containsExactly(FinesPermission.AMEND_PAYMENT_TERMS);
        verify(userStateService).getUserStateV1FromSecurityContext();
        verify(userState).getBusinessUnitUserForBusinessUnit((short) 78);
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.AMEND_PAYMENT_TERMS);
        verifyNoInteractions(defendantAccountPaymentTermsServiceProxy);
        verifyNoMoreInteractions(userStateService, userState);
    }
}
