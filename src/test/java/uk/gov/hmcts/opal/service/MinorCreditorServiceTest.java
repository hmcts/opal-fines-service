package uk.gov.hmcts.opal.service;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.generated.model.CreditorAccountPaymentDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.service.proxy.MinorCreditorSearchProxy;

@ExtendWith(MockitoExtension.class)
class MinorCreditorServiceTest {

    @Mock
    UserStateService userStateService;

    @Mock
    MinorCreditorSearchProxy minorCreditorSearchProxy;

    @Mock
    CreditorAccountRepository creditorAccountRepository;

    @InjectMocks
    private MinorCreditorService minorCreditorService;

    @Test
    void testPostSearchMinorCreditors() {
        // Arrange
        PostMinorCreditorAccountsSearchResponse postMinorCreditorAccountsSearchResponse =
            PostMinorCreditorAccountsSearchResponse.builder().build();

        when(minorCreditorSearchProxy.searchMinorCreditors(any())).thenReturn(postMinorCreditorAccountsSearchResponse);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allFinesPermissionUser());

        // Act
        PostMinorCreditorAccountsSearchResponse result =
            minorCreditorService.searchMinorCreditors(
                (MinorCreditorSearch.builder().build()), "authHeaderValue");

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetMinorCreditorAccountHeaderSummary() {
        // Arrange
        long id = 123L;
        GetMinorCreditorAccountHeaderSummaryResponse response =
            GetMinorCreditorAccountHeaderSummaryResponse.builder().build();

        when(minorCreditorSearchProxy.getHeaderSummary(id)).thenReturn(response);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allFinesPermissionUser());

        // Act
        GetMinorCreditorAccountHeaderSummaryResponse result =
            minorCreditorService.getMinorCreditorAccountHeaderSummary(id, "authHeaderValue");

        // Assert
        assertNotNull(result);
        assertEquals(response, result);
        verify(minorCreditorSearchProxy).getHeaderSummary(eq(id));
    }

    @Test
    void testGetMinorCreditorAccountHeaderSummary_permissionNotAllowed() {
        // Arrange
        UserState noPermissionUser = mock(UserState.class);
        when(noPermissionUser.anyBusinessUnitUserHasPermission(
            FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noPermissionUser);

        // Act & Assert
        assertThrows(
            PermissionNotAllowedException.class, () ->
                minorCreditorService.getMinorCreditorAccountHeaderSummary(123L, "authHeaderValue")
        );
    }

    @Test
    void testPermissionNotAllowedException() {
        // Arrange
        UserState noPermissionUser = mock(UserState.class);
        when(noPermissionUser.anyBusinessUnitUserHasPermission(
            FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noPermissionUser);

        // Act & Assert
        assertThrows(
            PermissionNotAllowedException.class, () ->
            minorCreditorService.searchMinorCreditors(MinorCreditorSearch.builder().build(), "authHeaderValue")
        );
    }

    @Test
    void updateMinorCreditorAccount_missingEtag_throwsConflict() {
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(true));

        assertThrows(
            ResourceConflictException.class, () ->
                minorCreditorService.updateMinorCreditorAccount(1L, request, null, "authHeaderValue")
        );
    }

    @Test
    void updateMinorCreditorAccount_missingPaymentGroup_throwsIllegalArgument() {
        assertThrows(
            IllegalArgumentException.class, () ->
                minorCreditorService.updateMinorCreditorAccount(1L, null, BigInteger.ONE, "authHeaderValue")
        );
    }

    @Test
    void updateMinorCreditorAccount_nonMinorAccount_throwsNotFound() {
        CreditorAccountEntity.Lite account = CreditorAccountEntity.Lite.builder()
            .creditorAccountId(1L)
            .businessUnitId((short) 10)
            .creditorAccountType(CreditorAccountType.MJ)
            .build();

        when(creditorAccountRepository.findById(1L)).thenReturn(Optional.of(account));

        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(true));

        assertThrows(
            jakarta.persistence.EntityNotFoundException.class, () ->
                minorCreditorService.updateMinorCreditorAccount(1L, request, BigInteger.ONE, "authHeaderValue")
        );
    }

    @Test
    void updateMinorCreditorAccount_missingBusinessUnit_throwsPermissionNotAllowed() {
        CreditorAccountEntity.Lite account = CreditorAccountEntity.Lite.builder()
            .creditorAccountId(1L)
            .creditorAccountType(CreditorAccountType.MN)
            .businessUnitId(null)
            .build();

        when(creditorAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(mock(UserState.class));

        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(true));

        assertThrows(
            PermissionNotAllowedException.class, () ->
                minorCreditorService.updateMinorCreditorAccount(1L, request, BigInteger.ONE, "authHeaderValue")
        );
    }

    @Test
    void updateMinorCreditorAccount_blankBusinessUnitUserId_fallsBackToUsername() {
        CreditorAccountEntity.Lite account = CreditorAccountEntity.Lite.builder()
            .creditorAccountId(1L)
            .creditorAccountType(CreditorAccountType.MN)
            .businessUnitId((short) 10)
            .build();

        UserState userState = mock(UserState.class);
        when(userState.hasBusinessUnitUserWithPermission((short) 10, FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD))
            .thenReturn(true);
        when(userState.getUserName()).thenReturn("test.user@hmcts.net");
        when(userState.getBusinessUnitUserForBusinessUnit((short) 10)).thenReturn(Optional.of(
            BusinessUnitUser.builder()
                .businessUnitUserId(" ")
                .businessUnitId((short) 10)
                .build()
        ));

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);
        when(creditorAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(minorCreditorSearchProxy.updateMinorCreditorAccount(eq(1L), any(), eq(BigInteger.ONE), any()))
            .thenReturn(new MinorCreditorAccountResponse());

        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(true));

        minorCreditorService.updateMinorCreditorAccount(1L, request, BigInteger.ONE, "authHeaderValue");

        ArgumentCaptor<String> postedByCaptor = ArgumentCaptor.forClass(String.class);
        verify(minorCreditorSearchProxy).updateMinorCreditorAccount(eq(1L), eq(request), eq(BigInteger.ONE),
            postedByCaptor.capture());
        assertEquals("test.user@hmcts.net", postedByCaptor.getValue());
    }

}
