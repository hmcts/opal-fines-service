package uk.gov.hmcts.opal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyShort;
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
        PermissionNotAllowedException ex = assertThrows(PermissionNotAllowedException.class, () ->
            defendantAccountPartyService
                .getDefendantAccountParty(defendantAccountId, defendantAccountPartyId, authHeader)
        );

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
    void replaceDefendantAccountParty_whenUserLacksPermission_throwsPermissionNotAllowedException() {
        // Arrange
        String authHeader = "Bearer token";
        Long defendantAccountId = 100L;
        Long defendantAccountPartyId = 200L;
        String ifMatch = "W/\"X\"";
        String businessUnitId = "3";
        DefendantAccountParty request = new DefendantAccountParty();

        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.hasBusinessUnitUserWithPermission(anyShort(), eq(FinesPermission.ACCOUNT_MAINTENANCE)))
            .thenReturn(false);

        // Act & Assert
        assertThrows(PermissionNotAllowedException.class, () ->
            defendantAccountPartyService.replaceDefendantAccountParty(
                defendantAccountId, defendantAccountPartyId, authHeader, ifMatch, businessUnitId, request
            )
        );

        verify(userStateService).checkForAuthorisedUser(authHeader);
        verify(userState).hasBusinessUnitUserWithPermission(anyShort(), eq(FinesPermission.ACCOUNT_MAINTENANCE));
        verifyNoInteractions(defendantAccountPartyServiceProxy);
    }
}
