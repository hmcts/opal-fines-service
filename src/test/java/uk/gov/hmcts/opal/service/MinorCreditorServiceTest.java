package uk.gov.hmcts.opal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.Permission;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.CreditorAccountPaymentDetailsCommon;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountResponseMinorCreditorPayment;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.service.proxy.MinorCreditorSearchProxy;

@ExtendWith(MockitoExtension.class)
class MinorCreditorServiceTest {

    @Mock
    UserStateService userStateService;

    @Mock
    MinorCreditorSearchProxy minorCreditorSearchProxy;

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
        PermissionNotAllowedException ex = Assertions.assertThrows(
            PermissionNotAllowedException.class,
                () -> minorCreditorService.getMinorCreditorAccountHeaderSummary(123L, "authHeaderValue")
        );
        assertThat(ex.getPermission()).containsExactly(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
    }

    @Test
    void testGetMinorCreditorAccount() {
        // Arrange
        Long id = 123L;
        MinorCreditorAccountResponse response = new MinorCreditorAccountResponse();

        when(minorCreditorSearchProxy.getMinorCreditorAccount(id)).thenReturn(response);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allFinesPermissionUser());

        // Act
        MinorCreditorAccountResponse result = minorCreditorService.getMinorCreditorAccount(id, "authHeaderValue");

        // Assert
        assertNotNull(result);
        assertEquals(response, result);
        verify(minorCreditorSearchProxy).getMinorCreditorAccount(eq(id));
    }

    @Test
    void testGetMinorCreditorAccount_permissionNotAllowed() {
        // Arrange
        UserState noPermissionUser = mock(UserState.class);
        when(noPermissionUser.anyBusinessUnitUserHasPermission(
            FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noPermissionUser);

        // Act & Assert
        PermissionNotAllowedException ex = Assertions.assertThrows(
            PermissionNotAllowedException.class,
            () -> minorCreditorService.getMinorCreditorAccount(123L, "authHeaderValue")
        );
        assertThat(ex.getPermission()).containsExactly(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
    }

    @Test
    void testGetMinorCreditorAccountAtAGlance() {
        // Arrange
        Long id = 123L;
        GetMinorCreditorAccountAtAGlanceResponse response = GetMinorCreditorAccountAtAGlanceResponse.builder().build();

        when(minorCreditorSearchProxy.getMinorCreditorAtAGlance(id)).thenReturn(response);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allFinesPermissionUser());

        // Act
        GetMinorCreditorAccountAtAGlanceResponse result =
            minorCreditorService.getMinorCreditorAtAGlance(id, "authHeaderValue");

        // Assert
        assertNotNull(result);
        assertEquals(response, result);
        verify(minorCreditorSearchProxy).getMinorCreditorAtAGlance(eq(id));
    }

    @Test
    void testGetMinorCreditorAccountAtAGlance_permissionNotAllowed() {
        // Arrange
        UserState noPermissionUser = mock(UserState.class);
        when(noPermissionUser.anyBusinessUnitUserHasPermission(
            FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noPermissionUser);

        // Act & Assert
        PermissionNotAllowedException ex = Assertions.assertThrows(
            PermissionNotAllowedException.class,
            () -> minorCreditorService.getMinorCreditorAtAGlance(123L, "authHeaderValue")
        );
        assertThat(ex.getPermission()).containsExactly(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
    }

    @Test
    void testGetMinorCreditorAccount() {
        // Arrange
        Long id = 123L;
        MinorCreditorAccountResponse response = responseWithBacsDetails();

        when(minorCreditorSearchProxy.getMinorCreditorAccount(id)).thenReturn(response);
        when(userStateService.checkForAuthorisedUser()).thenReturn(UserStateUtil.allFinesPermissionUser());

        // Act
        MinorCreditorAccountResponse result = minorCreditorService.getMinorCreditorAccount(id);

        // Assert
        assertNotNull(result);
        assertEquals(response, result);
        verify(minorCreditorSearchProxy).getMinorCreditorAccount(eq(id));
    }

    @Test
    void testGetMinorCreditorAccount_permissionNotAllowed() {
        // Arrange
        UserState noPermissionUser = mock(UserState.class);
        when(noPermissionUser.anyBusinessUnitUserHasPermission(
            FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);
        when(userStateService.checkForAuthorisedUser()).thenReturn(noPermissionUser);

        // Act & Assert
        PermissionNotAllowedException ex = Assertions.assertThrows(
            PermissionNotAllowedException.class,
            () -> minorCreditorService.getMinorCreditorAccount(123L)
        );

        assertThat(ex.getPermission()).containsExactly(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
    }

    @Test
    void testGetMinorCreditorAccount_filtersBacsDetailsWithoutPermission() {
        // Arrange
        Long id = 123L;
        MinorCreditorAccountResponse response = responseWithBacsDetails();

        when(minorCreditorSearchProxy.getMinorCreditorAccount(id)).thenReturn(response);
        when(userStateService.checkForAuthorisedUser()).thenReturn(
            UserStateUtil.permissionUser((short) 10, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)
        );

        // Act
        MinorCreditorAccountResponse result = minorCreditorService.getMinorCreditorAccount(id);

        // Assert
        assertNotNull(result.getPayment());
        assertEquals(null, result.getPayment().getAccountName());
        assertEquals(null, result.getPayment().getSortCode());
        assertEquals(null, result.getPayment().getAccountNumber());
        assertEquals(null, result.getPayment().getAccountReference());
        assertEquals(true, result.getPayment().getPayByBacs());
        assertEquals(false, result.getPayment().getHoldPayment());
    }

    @Test
    void testGetMinorCreditorAccount_keepsBacsDetailsWithPermission() {
        // Arrange
        Long id = 123L;
        MinorCreditorAccountResponse response = responseWithBacsDetails();

        Permission searchPermission = UserStateUtil.permissionFor(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        Permission bacsPermission = Permission.builder()
            .permissionId(999L)
            .permissionName("View Creditor BACS")
            .build();

        when(minorCreditorSearchProxy.getMinorCreditorAccount(id)).thenReturn(response);
        when(userStateService.checkForAuthorisedUser()).thenReturn(
            UserStateUtil.permissionUser((short) 10, searchPermission, bacsPermission)
        );

        // Act
        MinorCreditorAccountResponse result = minorCreditorService.getMinorCreditorAccount(id);

        // Assert
        assertNotNull(result.getPayment());
        assertEquals("Test Name", result.getPayment().getAccountName());
        assertEquals("123456", result.getPayment().getSortCode());
        assertEquals("12345678", result.getPayment().getAccountNumber());
        assertEquals("REF123", result.getPayment().getAccountReference());
    }

    @Test
    void testPermissionNotAllowedException() {
        // Arrange
        UserState noPermissionUser = mock(UserState.class);
        when(noPermissionUser.anyBusinessUnitUserHasPermission(
            FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noPermissionUser);

        // Act & Assert
        PermissionNotAllowedException ex = Assertions.assertThrows(
            PermissionNotAllowedException.class,
            () -> minorCreditorService.searchMinorCreditors(MinorCreditorSearch.builder().build(), "authHeaderValue")
        );
        assertThat(ex.getPermission()).containsExactly(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
    }

    @Test
    void updateMinorCreditorAccount_missingEtag_throwsConflict() {
        PatchMinorCreditorAccountRequest request = validPatchRequest();

        assertThrows(
            ResourceConflictException.class, () ->
                minorCreditorService.updateMinorCreditorAccount(1L, request, null, "authHeaderValue", "10")
        );
    }

    @Test
    void updateMinorCreditorAccount_missingPaymentGroup_throwsIllegalArgument() {
        assertThrows(
            IllegalArgumentException.class, () ->
                minorCreditorService.updateMinorCreditorAccount(1L, null, BigInteger.ONE, "authHeaderValue", "10")
        );
    }

    @Test
    void updateMinorCreditorAccount_missingPaymentObject_throwsIllegalArgument() {
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon().partyId("1").organisationFlag(true))
            .address(new AddressDetailsCommon());

        assertThrows(
            IllegalArgumentException.class, () ->
                minorCreditorService.updateMinorCreditorAccount(1L, request, BigInteger.ONE, "authHeaderValue", "10")
        );
    }

    @Test
    void updateMinorCreditorAccount_missingHoldPayment_throwsIllegalArgument() {
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon().partyId("1").organisationFlag(true))
            .address(new AddressDetailsCommon())
            .payment(new CreditorAccountPaymentDetailsCommon());

        assertThrows(
            IllegalArgumentException.class, () ->
                minorCreditorService.updateMinorCreditorAccount(1L, request, BigInteger.ONE, "authHeaderValue", "10")
        );
    }

    @Test
    void updateMinorCreditorAccount_missingPartyDetails_throwsIllegalArgument() {
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .address(new AddressDetailsCommon())
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(true));

        assertThrows(
            IllegalArgumentException.class, () ->
                minorCreditorService.updateMinorCreditorAccount(1L, request, BigInteger.ONE, "authHeaderValue", "10")
        );
    }

    @Test
    void updateMinorCreditorAccount_missingAddress_throwsIllegalArgument() {
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon().partyId("1").organisationFlag(true))
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(true));

        assertThrows(
            IllegalArgumentException.class, () ->
                minorCreditorService.updateMinorCreditorAccount(1L, request, BigInteger.ONE, "authHeaderValue", "10")
        );
    }

    @Test
    void updateMinorCreditorAccount_missingBusinessUnit_throwsPermissionNotAllowed() {
        // Arrange
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(mock(UserState.class));

        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon().partyId("1").organisationFlag(true))
            .address(new AddressDetailsCommon())
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(true));

        // Act & Assert
        PermissionNotAllowedException ex = Assertions.assertThrows(
            PermissionNotAllowedException.class,
            () -> minorCreditorService.updateMinorCreditorAccount(1L, request, BigInteger.ONE, "authHeaderValue", null)
        );

        assertThat(ex.getPermission()).containsExactly(FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD);
        assertThat(ex.getBusinessUnitId()).isEqualTo(null);

    }

    @Test
    void updateMinorCreditorAccount_blankBusinessUnitUserId_fallsBackToUsername() {
        // Arrange
        UserState userState = mock(UserState.class);
        when(userState.hasBusinessUnitUserWithPermission((short) 10, FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD))
            .thenReturn(true);
        when(userState.hasBusinessUnitUserWithPermission((short) 10, FinesPermission.ACCOUNT_MAINTENANCE))
            .thenReturn(true);
        when(userState.getUserName()).thenReturn("test.user@hmcts.net");
        when(userState.getBusinessUnitUserForBusinessUnit((short) 10)).thenReturn(Optional.of(
            BusinessUnitUser.builder()
                .businessUnitUserId(" ")
                .businessUnitId((short) 10)
                .build()
        ));

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);
        when(minorCreditorSearchProxy.updateMinorCreditorAccount(eq(1L), any(), eq(BigInteger.ONE), any(),
            anyShort()))
            .thenReturn(new MinorCreditorAccountResponse());

        PatchMinorCreditorAccountRequest request = validPatchRequest();

        // Act
        minorCreditorService.updateMinorCreditorAccount(1L, request, BigInteger.ONE, "authHeaderValue", "10");

        // Assert
        ArgumentCaptor<String> postedByCaptor = ArgumentCaptor.forClass(String.class);
        verify(minorCreditorSearchProxy).updateMinorCreditorAccount(eq(1L), eq(request), eq(BigInteger.ONE),
            postedByCaptor.capture(), eq(Short.valueOf("10")));
        assertEquals("test.user@hmcts.net", postedByCaptor.getValue());
    }

    @Test
    void updateMinorCreditorAccount_paymentObjectWithoutHoldPermission_evenWhenHoldUnchanged_throwsPermissionNotAllowed(
    ) {
        // Arrange
        UserState userState = UserStateUtil.permissionUser((short) 10, FinesPermission.ACCOUNT_MAINTENANCE);
        PatchMinorCreditorAccountRequest request = unchangedHoldPatchRequest();

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);

        // Act & Assert
        PermissionNotAllowedException ex = Assertions.assertThrows(
            PermissionNotAllowedException.class,
            () -> minorCreditorService.updateMinorCreditorAccount(
                1L,
                request,
                BigInteger.ONE,
                "authHeaderValue",
                "10"
            )
        );

        // Assert
        assertThat(ex.getPermission()).containsExactly(FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD);
        assertThat(ex.getBusinessUnitId()).isEqualTo((short) 10);
    }

    @Test
    void updateMinorCreditorAccount_paymentObjectWithoutHoldPermission_whenHoldChanges_throwsPermissionNotAllowed() {
        // Arrange
        UserState userState = UserStateUtil.permissionUser((short) 10, FinesPermission.ACCOUNT_MAINTENANCE);
        PatchMinorCreditorAccountRequest request = validPatchRequest();

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);

        // Act & Assert
        PermissionNotAllowedException ex = Assertions.assertThrows(
            PermissionNotAllowedException.class,
            () -> minorCreditorService.updateMinorCreditorAccount(1L, request, BigInteger.ONE,
                "authHeaderValue", "10")
        );
        assertThat(ex.getPermission()).containsExactly(FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD);
        assertThat(ex.getBusinessUnitId()).isEqualTo((short) 10);
    }

    private PatchMinorCreditorAccountRequest validPatchRequest() {
        return new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon().partyId("1").organisationFlag(true))
            .address(new AddressDetailsCommon())
            .payment(new CreditorAccountPaymentDetailsCommon()
                         .holdPayment(true)
                         .payByBacs(true)
                         .accountName("Account Name")
                         .sortCode("112233")
                         .accountNumber("12345678")
                         .accountReference("PAY-REF"));
    }

    private PatchMinorCreditorAccountRequest unchangedHoldPatchRequest() {
        return new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon().partyId("1").organisationFlag(true))
            .address(new AddressDetailsCommon())
            .payment(new CreditorAccountPaymentDetailsCommon()
                         .holdPayment(true)
                         .payByBacs(true)
                         .accountName("Account Name")
                         .sortCode("112233")
                         .accountNumber("12345678")
                         .accountReference("PAY-REF"));
    }

    private MinorCreditorAccountResponse responseWithBacsDetails() {
        MinorCreditorAccountResponse response = new MinorCreditorAccountResponse();
        response.setCreditorAccountId(123L);
        response.setBusinessUnitId((short) 10);
        response.setPayment(new MinorCreditorAccountResponseMinorCreditorPayment()
            .accountName("Test Name")
            .sortCode("123456")
            .accountNumber("12345678")
            .accountReference("REF123")
            .payByBacs(true)
            .holdPayment(false));
        return response;
    }

    @Test
    void updateMinorCreditorAccount_missingPaymentGroup_throwsIllegalArgumentException() {
        // Arrange
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon().partyId("1").organisationFlag(false))
            .address(new AddressDetailsCommon());

        // Act + Assert
        IllegalArgumentException ex = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> minorCreditorService.updateMinorCreditorAccount(1L, request, BigInteger.ONE, "test.user", "10")
        );
        assertEquals("Payment, party_details and address groups must be provided", ex.getMessage());
    }

    @Test
    void updateMinorCreditorAccount_nullRequest_throwsIllegalArgumentException() {
        IllegalArgumentException ex = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> minorCreditorService.updateMinorCreditorAccount(1L, null, BigInteger.ONE, "test.user", "10")
        );
        assertEquals("Payment, party_details and address groups must be provided", ex.getMessage());
    }

    @Test
    void updateMinorCreditorAccount_nullHoldPayment_throwsIllegalArgumentException() {
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon().partyId("1").organisationFlag(false))
            .address(new AddressDetailsCommon())
            .payment(new CreditorAccountPaymentDetailsCommon());

        IllegalArgumentException ex = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> minorCreditorService.updateMinorCreditorAccount(1L, request, BigInteger.ONE, "test.user", "10")
        );
        assertEquals("Payment, party_details and address groups must be provided", ex.getMessage());
    }

    @Test
    void updateMinorCreditorAccount_missingPartyDetails_throwsIllegalArgumentException() {
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .address(new AddressDetailsCommon())
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(true));

        IllegalArgumentException ex = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> minorCreditorService.updateMinorCreditorAccount(1L, request, BigInteger.ONE, "test.user", "10")
        );
        assertEquals("Payment, party_details and address groups must be provided", ex.getMessage());
    }

    @Test
    void updateMinorCreditorAccount_missingAddress_throwsIllegalArgumentException() {
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon().partyId("1").organisationFlag(false))
            .payment(new CreditorAccountPaymentDetailsCommon().holdPayment(true));

        IllegalArgumentException ex = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> minorCreditorService.updateMinorCreditorAccount(1L, request, BigInteger.ONE, "test.user", "10")
        );
        assertEquals("Payment, party_details and address groups must be provided", ex.getMessage());
    }
}
