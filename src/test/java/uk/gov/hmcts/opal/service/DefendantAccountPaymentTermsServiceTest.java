package uk.gov.hmcts.opal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPaymentTermsRequest;
import uk.gov.hmcts.opal.service.opal.BusinessUnitService;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountPaymentTermsServiceProxy;

@ExtendWith(MockitoExtension.class)
class DefendantAccountPaymentTermsServiceTest {

    @Mock
    private DefendantAccountPaymentTermsServiceProxy defendantAccountPaymentTermsServiceProxy;

    @Mock
    private UserStateService userStateService;

    @Mock
    private BusinessUnitService businessUnitService;

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

        // verify interactions
        verify(userStateService).getUserStateV1FromSecurityContext();
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verify(defendantAccountPaymentTermsServiceProxy).getPaymentTerms(defendantAccountId);
        verifyNoMoreInteractions(userStateService, userState, defendantAccountPaymentTermsServiceProxy);
    }

    @Test
    void getPaymentTerms_whenUserLacksPermission_throwsPermissionNotAllowed() {
        // arrange
        Long defendantAccountId = 77L;
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);

        // act + assert
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountPaymentTermsService.getPaymentTerms(defendantAccountId)
        );
        assertTrue(
            ex.getMessage() == null || ex.getMessage().contains(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS.name()),
            "Exception should mention the denied permission"
        );

        // proxy must not be called
        verify(userStateService).getUserStateV1FromSecurityContext();
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verifyNoInteractions(defendantAccountPaymentTermsServiceProxy);
        verifyNoMoreInteractions(userStateService, userState);
    }

    @Test
    void addPaymentCardRequest_derivesBusinessUnitUserIdAndPostedByNameFromUserStateV2() {
        // arrange
        Long defendantAccountId = 77L;
        Short businessUnitId = (short) 10;
        String derivedBusinessUnitUserId = "USER01";
        String ifMatch = "\"1\"";
        DomainBusinessUnitUsers businessUnitUsers = businessUnitUsers(
            businessUnitUser((short) 10, derivedBusinessUnitUserId, FinesPermission.AMEND_PAYMENT_TERMS));
        AddPaymentCardRequestResponse proxyResponse = new AddPaymentCardRequestResponse(defendantAccountId);

        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userStateV2(businessUnitUsers));
        when(businessUnitService.hasBusinessUnitUserWithPermission(
            businessUnitUsers, businessUnitId, FinesPermission.AMEND_PAYMENT_TERMS)).thenReturn(true);
        when(businessUnitService.getBusinessUnitUserIdForBusinessUnit(
            businessUnitUsers, (short) 10, FinesPermission.AMEND_PAYMENT_TERMS)).thenReturn(derivedBusinessUnitUserId);
        when(defendantAccountPaymentTermsServiceProxy.addPaymentCardRequest(
            defendantAccountId, businessUnitId, derivedBusinessUnitUserId, "normal@users.com", ifMatch))
            .thenReturn(proxyResponse);

        // act
        AddPaymentCardRequestResponse result = defendantAccountPaymentTermsService.addPaymentCardRequest(
            defendantAccountId, businessUnitId, ifMatch);

        // assert
        assertSame(proxyResponse, result);
        verify(userStateService).getUserStateFromSecurityContext();
        verify(businessUnitService).hasBusinessUnitUserWithPermission(
            businessUnitUsers, businessUnitId, FinesPermission.AMEND_PAYMENT_TERMS);
        verify(businessUnitService).getBusinessUnitUserIdForBusinessUnit(
            businessUnitUsers, (short) 10, FinesPermission.AMEND_PAYMENT_TERMS);
        verify(defendantAccountPaymentTermsServiceProxy).addPaymentCardRequest(
            defendantAccountId, businessUnitId, derivedBusinessUnitUserId, "normal@users.com", ifMatch);
        verifyNoMoreInteractions(userStateService, businessUnitService, defendantAccountPaymentTermsServiceProxy);
    }

    @Test
    void addPaymentTerms_overwritesPostedDetailsFromUserState() {
        Long defendantAccountId = 77L;
        String businessUnitId = "78";
        String ifMatch = "\"1\"";

        UserState userWithPerm = UserStateUtil.permissionUser((short) 78, FinesPermission.AMEND_PAYMENT_TERMS);
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userWithPerm);

        AddDefendantAccountPaymentTermsRequest request = AddDefendantAccountPaymentTermsRequest.builder()
            .paymentTerms(PaymentTerms.builder()
                .postedDetails(PostedDetails.builder()
                    .postedBy("FE_USER")
                    .postedByName("FE_NAME")
                    .build())
                .build())
            .build();

        GetDefendantAccountPaymentTermsResponse proxyResponse = new GetDefendantAccountPaymentTermsResponse();
        when(defendantAccountPaymentTermsServiceProxy.addPaymentTerms(eq(defendantAccountId),
            eq(businessUnitId),
            eq("USER01"),
            eq("normal@users.com"),
            eq(ifMatch),
            any(AddDefendantAccountPaymentTermsRequest.class)))
            .thenReturn(proxyResponse);

        GetDefendantAccountPaymentTermsResponse result = defendantAccountPaymentTermsService.addPaymentTerms(
            defendantAccountId, businessUnitId, ifMatch, request);

        assertSame(proxyResponse, result);

        ArgumentCaptor<AddDefendantAccountPaymentTermsRequest> captor =
            ArgumentCaptor.forClass(AddDefendantAccountPaymentTermsRequest.class);
        verify(defendantAccountPaymentTermsServiceProxy).addPaymentTerms(eq(defendantAccountId),
            eq(businessUnitId),
            eq("USER01"),
            eq("normal@users.com"),
            eq(ifMatch),
            captor.capture());

        PostedDetails postedDetails = captor.getValue().getPaymentTerms().getPostedDetails();
        assertNotNull(postedDetails);
        assertEquals("USER01", postedDetails.getPostedBy());
        assertEquals("normal@users.com", postedDetails.getPostedByName());
    }

    @Test
    void addPaymentCardRequest_userHasBusinessUnitUserAndPermission_callsProxyWithDerivedId() {
        // Arrange
        Long defendantAccountId = 77L;
        short businessUnitId = 78;
        String businessUnitUserId = "L080JG";
        String ifMatch = "\"4\"";
        AddPaymentCardRequestResponse proxyResponse = new AddPaymentCardRequestResponse(defendantAccountId);
        DomainBusinessUnitUsers businessUnitUsers = businessUnitUsers(
            businessUnitUser((short) 78, businessUnitUserId, FinesPermission.AMEND_PAYMENT_TERMS));

        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userStateV2(businessUnitUsers));
        when(businessUnitService.hasBusinessUnitUserWithPermission(
            businessUnitUsers, businessUnitId, FinesPermission.AMEND_PAYMENT_TERMS)).thenReturn(true);
        when(businessUnitService.getBusinessUnitUserIdForBusinessUnit(
            businessUnitUsers, (short) 78, FinesPermission.AMEND_PAYMENT_TERMS)).thenReturn(businessUnitUserId);
        when(defendantAccountPaymentTermsServiceProxy.addPaymentCardRequest(
            defendantAccountId,
            businessUnitId,
            businessUnitUserId,
            "normal@users.com",
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
        verify(userStateService).getUserStateFromSecurityContext();
        verify(businessUnitService).hasBusinessUnitUserWithPermission(
            businessUnitUsers, businessUnitId, FinesPermission.AMEND_PAYMENT_TERMS);
        verify(businessUnitService).getBusinessUnitUserIdForBusinessUnit(
            businessUnitUsers, (short) 78, FinesPermission.AMEND_PAYMENT_TERMS);
        verify(defendantAccountPaymentTermsServiceProxy).addPaymentCardRequest(
            defendantAccountId,
            businessUnitId,
            businessUnitUserId,
            "normal@users.com",
            ifMatch
        );
        verifyNoMoreInteractions(userStateService, businessUnitService, defendantAccountPaymentTermsServiceProxy);
    }

    @Test
    void addPaymentCardRequest_permissionDenied_throws403() {
        // Arrange
        short businessUnitId = 10;
        DomainBusinessUnitUsers businessUnitUsers = businessUnitUsers(
            businessUnitUser(businessUnitId, "L010JG")
        );

        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userStateV2(businessUnitUsers));
        when(businessUnitService.hasBusinessUnitUserWithPermission(
            businessUnitUsers, businessUnitId, FinesPermission.AMEND_PAYMENT_TERMS)).thenReturn(false);

        // Act
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountPaymentTermsService.addPaymentCardRequest(1L, businessUnitId, "\"1\"")
        );

        // Assert
        assertThat(ex.getPermission()).containsExactly(FinesPermission.AMEND_PAYMENT_TERMS);
        assertThat(ex.getBusinessUnitId()).isEqualTo(businessUnitId);
        verify(userStateService).getUserStateFromSecurityContext();
        verify(businessUnitService).hasBusinessUnitUserWithPermission(
            businessUnitUsers, businessUnitId, FinesPermission.AMEND_PAYMENT_TERMS);
        verify(businessUnitService, never()).getBusinessUnitUserIdForBusinessUnit(
            any(DomainBusinessUnitUsers.class), eq(businessUnitId), eq(FinesPermission.AMEND_PAYMENT_TERMS));
        verifyNoInteractions(defendantAccountPaymentTermsServiceProxy);
        verifyNoMoreInteractions(userStateService, businessUnitService);
    }

    @Test
    void addPaymentCardRequest_missingBusinessUnitUser_throws403Exception() {
        // Arrange
        short businessUnitId = 10;
        DomainBusinessUnitUsers businessUnitUsers = businessUnitUsers();

        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userStateV2(businessUnitUsers));
        when(businessUnitService.hasBusinessUnitUserWithPermission(
            businessUnitUsers, businessUnitId, FinesPermission.AMEND_PAYMENT_TERMS)).thenReturn(false);

        // Act
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountPaymentTermsService.addPaymentCardRequest(1L, businessUnitId, "\"1\"")
        );

        // Assert
        assertThat(ex.getPermission()).containsExactly(FinesPermission.AMEND_PAYMENT_TERMS);
        assertThat(ex.getBusinessUnitId()).isEqualTo(businessUnitId);
        verify(userStateService).getUserStateFromSecurityContext();
        verify(businessUnitService).hasBusinessUnitUserWithPermission(
            businessUnitUsers, businessUnitId, FinesPermission.AMEND_PAYMENT_TERMS);
        verify(businessUnitService, never()).getBusinessUnitUserIdForBusinessUnit(
            any(DomainBusinessUnitUsers.class), eq(businessUnitId), eq(FinesPermission.AMEND_PAYMENT_TERMS));
        verifyNoInteractions(defendantAccountPaymentTermsServiceProxy);
        verifyNoMoreInteractions(userStateService, businessUnitService);
    }

    @Test
    void addPaymentCardRequest_permissionInDifferentBusinessUnit_throws403AndDoesNotCallProxy() {
        // Arrange
        DomainBusinessUnitUsers businessUnitUsers = businessUnitUsers(
            businessUnitUser((short) 77, "L077JG", FinesPermission.AMEND_PAYMENT_TERMS));
        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userStateV2(businessUnitUsers));

        // Act
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountPaymentTermsService.addPaymentCardRequest(77L, (short) 78, "\"4\"")
        );

        // Assert
        assertThat(ex.getPermission()).containsExactly(FinesPermission.AMEND_PAYMENT_TERMS);
        assertEquals((short) 78, ex.getBusinessUnitId());
        verify(userStateService).getUserStateFromSecurityContext();
        verify(businessUnitService).hasBusinessUnitUserWithPermission(
            businessUnitUsers, (short) 78, FinesPermission.AMEND_PAYMENT_TERMS);
        verifyNoInteractions(defendantAccountPaymentTermsServiceProxy);
        verifyNoMoreInteractions(userStateService, businessUnitService);
    }

    @Test
    void addPaymentCardRequest_selectedBusinessUnitWithoutPermission_throws403AndDoesNotCallProxy() {
        // Arrange
        DomainBusinessUnitUsers businessUnitUsers = businessUnitUsers(
            businessUnitUser((short) 78, "L078JG"),
            businessUnitUser((short) 77, "L077JG", FinesPermission.AMEND_PAYMENT_TERMS));
        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userStateV2(businessUnitUsers));

        // Act
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountPaymentTermsService.addPaymentCardRequest(77L, (short) 78, "\"4\"")
        );

        // Assert
        assertThat(ex.getPermission()).containsExactly(FinesPermission.AMEND_PAYMENT_TERMS);
        assertEquals((short) 78, ex.getBusinessUnitId());
        verify(userStateService).getUserStateFromSecurityContext();
        verify(businessUnitService).hasBusinessUnitUserWithPermission(
            businessUnitUsers, (short) 78, FinesPermission.AMEND_PAYMENT_TERMS);
        verifyNoInteractions(defendantAccountPaymentTermsServiceProxy);
        verifyNoMoreInteractions(userStateService, businessUnitService);
    }

    @Test
    void addPaymentCardRequest_noFinesDomain_throws403AndDoesNotCallProxy() {
        // Arrange
        UserStateV2 userStateWithoutFinesDomain = UserStateV2.builder()
            .userId(1L)
            .username("normal@users.com")
            .name("Normal User")
            .domains(Map.of())
            .build();
        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userStateWithoutFinesDomain);

        // Act
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountPaymentTermsService.addPaymentCardRequest(77L, (short) 78, "\"4\"")
        );

        // Assert
        assertThat(ex.getPermission()).containsExactly(FinesPermission.AMEND_PAYMENT_TERMS);
        assertEquals((short) 78, ex.getBusinessUnitId());

        ArgumentCaptor<DomainBusinessUnitUsers> businessUnitUsersCaptor =
            ArgumentCaptor.forClass(DomainBusinessUnitUsers.class);
        verify(userStateService).getUserStateFromSecurityContext();
        verify(businessUnitService).hasBusinessUnitUserWithPermission(
            businessUnitUsersCaptor.capture(), eq((short) 78), eq(FinesPermission.AMEND_PAYMENT_TERMS));
        assertThat(businessUnitUsersCaptor.getValue().getBusinessUnitUsers()).isEmpty();
        verifyNoInteractions(defendantAccountPaymentTermsServiceProxy);
        verifyNoMoreInteractions(userStateService, businessUnitService);
    }

    @Test
    void addPaymentCardRequest_nullFinesBusinessUnitUsers_throws403AndDoesNotCallProxy() {
        // Arrange
        DomainBusinessUnitUsers businessUnitUsers = DomainBusinessUnitUsers.builder().build();
        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userStateV2(businessUnitUsers));

        // Act
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountPaymentTermsService.addPaymentCardRequest(77L, (short) 78, "\"4\"")
        );

        // Assert
        assertThat(ex.getPermission()).containsExactly(FinesPermission.AMEND_PAYMENT_TERMS);
        assertEquals((short) 78, ex.getBusinessUnitId());
        verify(userStateService).getUserStateFromSecurityContext();
        verify(businessUnitService).hasBusinessUnitUserWithPermission(
            businessUnitUsers, (short) 78, FinesPermission.AMEND_PAYMENT_TERMS);
        verifyNoInteractions(defendantAccountPaymentTermsServiceProxy);
        verifyNoMoreInteractions(userStateService, businessUnitService);
    }

    private static UserStateV2 userStateV2(DomainBusinessUnitUsers businessUnitUsers) {
        return UserStateV2.builder()
            .userId(1L)
            .username("normal@users.com")
            .name("Normal User")
            .domains(Map.of(Domain.FINES, businessUnitUsers))
            .build();
    }

    private static DomainBusinessUnitUsers businessUnitUsers(BusinessUnitUser... businessUnitUsers) {
        return DomainBusinessUnitUsers.builder()
            .businessUnitUsers(List.of(businessUnitUsers))
            .build();
    }

    private static BusinessUnitUser businessUnitUser(
        short businessUnitId, String businessUnitUserId, FinesPermission... permissions) {

        return BusinessUnitUser.builder()
            .businessUnitId(businessUnitId)
            .businessUnitUserId(businessUnitUserId)
            .permissions(UserStateUtil.permissionsFor(permissions))
            .build();
    }
}
