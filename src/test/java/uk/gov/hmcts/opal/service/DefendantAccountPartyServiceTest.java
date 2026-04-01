package uk.gov.hmcts.opal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.response.RemoveDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountPartyServiceProxy;

@ExtendWith(MockitoExtension.class)
class DefendantAccountPartyServiceTest {

    @Mock
    private DefendantAccountPartyServiceProxy defendantAccountPartyServiceProxy;

    @Mock
    private UserStateService userStateService;

    @Mock
    private UserState userState;

    @InjectMocks
    private DefendantAccountPartyService defendantAccountPartyService;

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @Test
    void getDefendantAccountParty_whenUserHasPermission_returnsResponse() {
        // Arrange
        String authHeader = "Bearer token";
        Long defendantAccountId = 1L;
        Long defendantAccountPartyId = 2L;

        GetDefendantAccountPartyResponse expectedResponse = mock(GetDefendantAccountPartyResponse.class);

        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(defendantAccountPartyServiceProxy.getDefendantAccountParty(defendantAccountId, defendantAccountPartyId))
            .thenReturn(expectedResponse);

        // Act
        GetDefendantAccountPartyResponse actual = defendantAccountPartyService
            .getDefendantAccountParty(defendantAccountId, defendantAccountPartyId, authHeader);

        // Assert
        assertThat(actual).isSameAs(expectedResponse);
        verify(userStateService).checkForAuthorisedUser(authHeader);
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verify(defendantAccountPartyServiceProxy).getDefendantAccountParty(defendantAccountId, defendantAccountPartyId);
    }

    @Test
    void getDefendantAccountParty_whenUserLacksPermission_throwsPermissionNotAllowedException() {
        // Arrange
        String authHeader = "Bearer token";
        Long defendantAccountId = 1L;
        Long defendantAccountPartyId = 2L;

        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);

        // Act & Assert
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class, () ->
                defendantAccountPartyService
                    .getDefendantAccountParty(defendantAccountId, defendantAccountPartyId, authHeader)
        );

        assertThat(ex.getPermission()).containsExactly(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);

        verify(userStateService).checkForAuthorisedUser(authHeader);
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verifyNoInteractions(defendantAccountPartyServiceProxy);
    }

    @Test
    void replaceDefendantAccountParty_whenUserHasPermission_passesPostedByAndBusinessUnitUserIdToProxy() {
        // Arrange
        String authHeader = "Bearer token";
        Long defendantAccountId = 10L;
        Long defendantAccountPartyId = 20L;
        String ifMatch = "W/\"1\"";
        String businessUnitId = "5";
        short buId = Short.parseShort(businessUnitId);

        DefendantAccountParty request = new DefendantAccountParty(); // DTO - constructor should exist
        GetDefendantAccountPartyResponse expectedResponse = mock(GetDefendantAccountPartyResponse.class);

        BusinessUnitUser buUser = mock(BusinessUnitUser.class);
        when(buUser.getBusinessUnitUserId()).thenReturn("b-user-id");
        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.getBusinessUnitUserForBusinessUnit(buId)).thenReturn(Optional.of(buUser));
        when(userState.hasBusinessUnitUserWithPermission(eq(buId), eq(FinesPermission.ACCOUNT_MAINTENANCE)))
            .thenReturn(true);

        when(defendantAccountPartyServiceProxy.replaceDefendantAccountParty(
            anyLong(), anyLong(), any(DefendantAccountParty.class), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(expectedResponse);

        // Act
        GetDefendantAccountPartyResponse actual = defendantAccountPartyService.replaceDefendantAccountParty(
            defendantAccountId, defendantAccountPartyId, authHeader, ifMatch, businessUnitId, request
        );

        // Assert
        assertThat(actual).isSameAs(expectedResponse);

        // Capture the last two String arguments (postedBy and businessUnitUserId)
        ArgumentCaptor<String> postedByCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> buUserIdCaptor = ArgumentCaptor.forClass(String.class);

        verify(defendantAccountPartyServiceProxy).replaceDefendantAccountParty(
            eq(defendantAccountId),
            eq(defendantAccountPartyId),
            eq(request),
            eq(ifMatch),
            eq(businessUnitId),
            postedByCaptor.capture(),
            buUserIdCaptor.capture()
        );

        // When BusinessUnitUser present and has a non-blank id, postedBy should be that id and businessUnitUserId same
        assertThat(postedByCaptor.getValue()).isEqualTo("b-user-id");
        assertThat(buUserIdCaptor.getValue()).isEqualTo("b-user-id");
    }

    @Test
    void addDefendantAccountParty_whenUserHasPermission_passesPostedByAndBusinessUnitUserIdToProxy() {
        // Arrange
        String authHeader = "Bearer token";
        Long defendantAccountId = 10L;
        Long defendantAccountPartyId = 20L;
        String ifMatch = "W/\"1\"";
        String businessUnitId = "5";
        short buId = Short.parseShort(businessUnitId);

        // DTO - constructor should exist
        AddDefendantAccountPartyRequest request = new AddDefendantAccountPartyRequest();
        GetDefendantAccountPartyResponse expectedResponse = mock(GetDefendantAccountPartyResponse.class);

        BusinessUnitUser buUser = mock(BusinessUnitUser.class);
        when(buUser.getBusinessUnitUserId()).thenReturn("b-user-id");
        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.getBusinessUnitUserForBusinessUnit(buId)).thenReturn(Optional.of(buUser));
        when(userState.hasBusinessUnitUserWithPermission(eq(buId), eq(FinesPermission.ACCOUNT_MAINTENANCE)))
            .thenReturn(true);

        when(defendantAccountPartyServiceProxy.addDefendantAccountParty(
            anyLong(), anyString(), anyString(), anyString(), anyString(), any(AddDefendantAccountPartyRequest.class)))
            .thenReturn(expectedResponse);

        // Act
        GetDefendantAccountPartyResponse actual = defendantAccountPartyService.addDefendantAccountParty(
            defendantAccountId, authHeader, ifMatch, businessUnitId, request
        );

        // Assert
        assertThat(actual).isSameAs(expectedResponse);

        // Capture the last two String arguments (postedBy and businessUnitUserId)
        ArgumentCaptor<String> postedByCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> buUserIdCaptor = ArgumentCaptor.forClass(String.class);

        verify(defendantAccountPartyServiceProxy).addDefendantAccountParty(
            eq(defendantAccountId),
            eq(businessUnitId),
            buUserIdCaptor.capture(),
            postedByCaptor.capture(),
            eq(ifMatch),
            eq(request)
        );

        // When BusinessUnitUser present and has a non-blank id, postedBy should be that id and businessUnitUserId same
        assertThat(postedByCaptor.getValue()).isEqualTo("b-user-id");
        assertThat(buUserIdCaptor.getValue()).isEqualTo("b-user-id");
    }


    @Test
    void replaceDefendantAccountParty_whenBusinessUnitUserMissing_usesUserNameForPostedByAndEmptyBusinessUnitUserId() {
        // Arrange
        String authHeader = "Bearer token";
        Long defendantAccountId = 11L;
        Long defendantAccountPartyId = 22L;
        String ifMatch = "W/\"2\"";
        String businessUnitId = "7";
        short buId = Short.parseShort(businessUnitId);

        DefendantAccountParty request = new DefendantAccountParty();
        GetDefendantAccountPartyResponse expectedResponse = mock(GetDefendantAccountPartyResponse.class);

        // No BusinessUnitUser present
        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.getBusinessUnitUserForBusinessUnit(buId)).thenReturn(Optional.empty());
        when(userState.getUserName()).thenReturn("theUserName");
        when(userState.hasBusinessUnitUserWithPermission(eq(buId), eq(FinesPermission.ACCOUNT_MAINTENANCE)))
            .thenReturn(true);

        when(defendantAccountPartyServiceProxy.replaceDefendantAccountParty(
            anyLong(), anyLong(), any(DefendantAccountParty.class), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(expectedResponse);

        // Act
        GetDefendantAccountPartyResponse actual = defendantAccountPartyService.replaceDefendantAccountParty(
            defendantAccountId, defendantAccountPartyId, authHeader, ifMatch, businessUnitId, request
        );

        // Assert
        assertThat(actual).isSameAs(expectedResponse);

        ArgumentCaptor<String> postedByCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> buUserIdCaptor = ArgumentCaptor.forClass(String.class);

        verify(defendantAccountPartyServiceProxy).replaceDefendantAccountParty(
            eq(defendantAccountId),
            eq(defendantAccountPartyId),
            eq(request),
            eq(ifMatch),
            eq(businessUnitId),
            postedByCaptor.capture(),
            buUserIdCaptor.capture()
        );

        assertThat(postedByCaptor.getValue()).isEqualTo("theUserName");
        // when no business unit user present, the helper returns empty string
        assertThat(buUserIdCaptor.getValue()).isEqualTo("");
    }

    @Test
    void addDefendantAccountParty_whenBusinessUnitUserMissing_usesUserNameForPostedByAndEmptyBusinessUnitUserId() {
        // Arrange
        String authHeader = "Bearer token";
        Long defendantAccountId = 11L;
        Long defendantAccountPartyId = 22L;
        String ifMatch = "W/\"2\"";
        String businessUnitId = "7";
        short buId = Short.parseShort(businessUnitId);

        AddDefendantAccountPartyRequest request = new AddDefendantAccountPartyRequest();
        GetDefendantAccountPartyResponse expectedResponse = mock(GetDefendantAccountPartyResponse.class);

        // No BusinessUnitUser present
        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.getBusinessUnitUserForBusinessUnit(buId)).thenReturn(Optional.empty());
        when(userState.getUserName()).thenReturn("theUserName");
        when(userState.hasBusinessUnitUserWithPermission(eq(buId), eq(FinesPermission.ACCOUNT_MAINTENANCE)))
            .thenReturn(true);

        when(defendantAccountPartyServiceProxy.addDefendantAccountParty(
            anyLong(), anyString(), anyString(), anyString(), anyString(), any(AddDefendantAccountPartyRequest.class)))
            .thenReturn(expectedResponse);

        // Act
        GetDefendantAccountPartyResponse actual = defendantAccountPartyService.addDefendantAccountParty(
            defendantAccountId, authHeader, ifMatch, businessUnitId, request
        );

        // Assert
        assertThat(actual).isSameAs(expectedResponse);

        ArgumentCaptor<String> postedByCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> buUserIdCaptor = ArgumentCaptor.forClass(String.class);

        verify(defendantAccountPartyServiceProxy).addDefendantAccountParty(
            eq(defendantAccountId),
            eq(businessUnitId),
            buUserIdCaptor.capture(),
            postedByCaptor.capture(),
            eq(ifMatch),
            eq(request)
        );

        assertThat(postedByCaptor.getValue()).isEqualTo("theUserName");
        // when no business unit user present, the helper returns empty string
        assertThat(buUserIdCaptor.getValue()).isEqualTo("");
    }


    @Test
    void replaceDefendantAccountParty_whenUserLacksPermission_throwsPermissionNotAllowedException() {
        // Arrange
        String authHeader = "Bearer token";
        Long defendantAccountId = 100L;
        Long defendantAccountPartyId = 200L;
        String ifMatch = "W/\"X\"";
        Short businessUnitId = 3;
        String stringBusinessUnitId = String.valueOf(businessUnitId);
        DefendantAccountParty request = new DefendantAccountParty();

        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.hasBusinessUnitUserWithPermission(businessUnitId, FinesPermission.ACCOUNT_MAINTENANCE))
            .thenReturn(false);

        // Act & Assert
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountPartyService.replaceDefendantAccountParty(
                defendantAccountId, defendantAccountPartyId, authHeader, ifMatch, stringBusinessUnitId, request)
        );

        assertThat(ex.getPermission()).containsExactly(FinesPermission.ACCOUNT_MAINTENANCE);
        assertThat(ex.getBusinessUnitId()).isEqualTo(businessUnitId);

        verify(userStateService).checkForAuthorisedUser(authHeader);
        verify(userState).hasBusinessUnitUserWithPermission(businessUnitId, FinesPermission.ACCOUNT_MAINTENANCE);
        verifyNoInteractions(defendantAccountPartyServiceProxy);
    }

    @Test
    void addDefendantAccountParty_whenUserLacksPermission_throwsPermissionNotAllowedException() {
        // Arrange
        String authHeader = "Bearer token";
        Long defendantAccountId = 100L;
        Long defendantAccountPartyId = 200L;
        String ifMatch = "W/\"X\"";
        Short businessUnitId = 3;
        String stringBusinessUnitId = String.valueOf(businessUnitId);
        AddDefendantAccountPartyRequest request = new AddDefendantAccountPartyRequest();

        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.hasBusinessUnitUserWithPermission(businessUnitId, FinesPermission.ACCOUNT_MAINTENANCE))
            .thenReturn(false);

        // Act & Assert
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountPartyService.addDefendantAccountParty(
                defendantAccountId, authHeader, ifMatch, stringBusinessUnitId, request)
        );

        assertThat(ex.getPermission()).containsExactly(FinesPermission.ACCOUNT_MAINTENANCE);
        assertThat(ex.getBusinessUnitId()).isEqualTo(businessUnitId);

        verify(userStateService).checkForAuthorisedUser(authHeader);
        verify(userState).hasBusinessUnitUserWithPermission(businessUnitId, FinesPermission.ACCOUNT_MAINTENANCE);
        verifyNoInteractions(defendantAccountPartyServiceProxy);
    }

    @Test
    void removeDefendantAccountParty_whenUserHasPermission_passesPostedByAndBusinessUnitUserIdToProxy() {
        // Arrange
        String authHeader = "Bearer token";
        Long defendantAccountId = 33L;
        Long defendantAccountPartyId = 44L;
        String businessUnitId = "9";
        short buId = Short.parseShort(businessUnitId);
        String ifMatch = "W/\"3\"";
        DefendantAccountParty request = new DefendantAccountParty();
        RemoveDefendantAccountPartyResponse expectedResponse = mock(RemoveDefendantAccountPartyResponse.class);

        BusinessUnitUser buUser = mock(BusinessUnitUser.class);
        when(buUser.getBusinessUnitUserId()).thenReturn("bu-user-id");
        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.getBusinessUnitUserForBusinessUnit(buId)).thenReturn(Optional.of(buUser));
        when(userState.hasBusinessUnitUserWithPermission(buId, FinesPermission.ACCOUNT_MAINTENANCE))
            .thenReturn(true);
        when(defendantAccountPartyServiceProxy.removeDefendantAccountParty(anyLong(), anyLong(), anyString(),
            anyString(),
            anyString(), anyString(), any(DefendantAccountParty.class))).thenReturn(expectedResponse);

        // Act
        RemoveDefendantAccountPartyResponse actual = defendantAccountPartyService.removeDefendantAccountParty(
            defendantAccountId, defendantAccountPartyId, businessUnitId, ifMatch, authHeader, request);

        // Assert
        assertThat(actual).isSameAs(expectedResponse);

        // Capture the postedBy and businessUnitUserId arguments for verification
        ArgumentCaptor<String> buUserIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> postedByCaptor = ArgumentCaptor.forClass(String.class);

        verify(defendantAccountPartyServiceProxy).removeDefendantAccountParty(
            eq(defendantAccountId),
            eq(defendantAccountPartyId),
            eq(businessUnitId),
            buUserIdCaptor.capture(),
            eq(ifMatch),
            postedByCaptor.capture(),
            eq(request)
        );

        // When businessUnitUserId is not null, not blank, it should be the same as the value derived from the auth
        // token
        assertThat(buUserIdCaptor.getValue()).isEqualTo("bu-user-id");
        assertThat(postedByCaptor.getValue()).isEqualTo("bu-user-id");
    }

    @Test
    void removeDefendantAccountParty_whenBusinessUnitUserMissing_usesUserNameAndEmptyBusinessUnitUserId() {
        // Arrange
        String authHeader = "Bearer token";
        Long defendantAccountId = 55L;
        Long defendantAccountPartyId = 66L;
        String businessUnitId = "11";
        short buId = Short.parseShort(businessUnitId);
        String ifMatch = "W/\"4\"";
        DefendantAccountParty request = new DefendantAccountParty();
        RemoveDefendantAccountPartyResponse expectedResponse = mock(RemoveDefendantAccountPartyResponse.class);

        // No BusinessUnitUser present
        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.getBusinessUnitUserForBusinessUnit(buId)).thenReturn(Optional.empty());
        when(userState.getUserName()).thenReturn("fallback-user");
        when(userState.hasBusinessUnitUserWithPermission(buId, FinesPermission.ACCOUNT_MAINTENANCE))
            .thenReturn(true);
        when(defendantAccountPartyServiceProxy.removeDefendantAccountParty(anyLong(), anyLong(), anyString(),
            anyString(),
            anyString(), anyString(), any(DefendantAccountParty.class))).thenReturn(expectedResponse);

        // Act
        RemoveDefendantAccountPartyResponse actual = defendantAccountPartyService.removeDefendantAccountParty(
            defendantAccountId, defendantAccountPartyId, businessUnitId, ifMatch, authHeader, request);

        // Assert
        assertThat(actual).isSameAs(expectedResponse);

        ArgumentCaptor<String> buUserIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> postedByCaptor = ArgumentCaptor.forClass(String.class);

        verify(defendantAccountPartyServiceProxy).removeDefendantAccountParty(
            eq(defendantAccountId),
            eq(defendantAccountPartyId),
            eq(businessUnitId),
            buUserIdCaptor.capture(),
            eq(ifMatch),
            postedByCaptor.capture(),
            eq(request)
        );

        // When BusinessUnitUser is not provided the helper returns an empty string
        assertThat(buUserIdCaptor.getValue()).isEqualTo("");
        assertThat(postedByCaptor.getValue()).isEqualTo("fallback-user");
    }

    @Test
    void removeDefendantAccountParty_whenUserLacksPermission_throwsPermissionNotAllowedException() {
        // Arrange
        String authHeader = "Bearer token";
        Long defendantAccountId = 77L;
        Long defendantAccountPartyId = 88L;
        String businessUnitId = "13";
        short buId = Short.parseShort(businessUnitId);
        String ifMatch = "W/\"5\"";
        DefendantAccountParty request = new DefendantAccountParty();

        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.hasBusinessUnitUserWithPermission(buId, FinesPermission.ACCOUNT_MAINTENANCE)).thenReturn(false);

        PermissionNotAllowedException ex = assertThrows(PermissionNotAllowedException.class, () ->
            defendantAccountPartyService.removeDefendantAccountParty(
                defendantAccountId, defendantAccountPartyId, businessUnitId, ifMatch, authHeader, request)
        );

        // When the user does not have the correct permission, the call is not passed to the proxy
        assertThat(ex.getPermission()).containsExactly(FinesPermission.ACCOUNT_MAINTENANCE);
        assertThat(ex.getBusinessUnitId()).isNull();
        verify(userStateService).checkForAuthorisedUser(authHeader);
        verify(userState).hasBusinessUnitUserWithPermission(buId, FinesPermission.ACCOUNT_MAINTENANCE);
        verifyNoInteractions(defendantAccountPartyServiceProxy);
    }
}
