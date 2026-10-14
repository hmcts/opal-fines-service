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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.generated.model.AddPartyRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountParty;
import uk.gov.hmcts.opal.generated.model.PartyResponseDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.RemoveDefendantAccountPartyRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.RemoveDefendantAccountPartyResponseDefendantAccount;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountPartyServiceProxy;
import uk.gov.hmcts.opal.service.opal.DefendantAccountPartyPdplLoggingService;

@ExtendWith(MockitoExtension.class)
class DefendantAccountPartyServiceTest {

    @Mock
    private DefendantAccountPartyServiceProxy defendantAccountPartyServiceProxy;

    @Mock
    private UserStateService userStateService;

    @Mock
    private DefendantAccountPartyPdplLoggingService pdplLoggingService;

    @Mock
    private UserState userState;

    @InjectMocks
    private DefendantAccountPartyService defendantAccountPartyService;

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @Test
    void getDefendantAccountParty_whenUserHasPermission_returnsResponse() {
        // Arrange
        Long defendantAccountId = 1L;
        Long defendantAccountPartyId = 2L;

        PartyResponseDefendantAccount expectedResponse = mock(PartyResponseDefendantAccount.class);

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(defendantAccountPartyServiceProxy.getDefendantAccountParty(defendantAccountId, defendantAccountPartyId))
            .thenReturn(expectedResponse);

        // Act
        PartyResponseDefendantAccount actual = defendantAccountPartyService
            .getDefendantAccountParty(defendantAccountId, defendantAccountPartyId);

        // Assert
        assertThat(actual).isSameAs(expectedResponse);
        verify(userStateService).getUserStateV1FromSecurityContext();
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verify(defendantAccountPartyServiceProxy).getDefendantAccountParty(defendantAccountId, defendantAccountPartyId);
    }

    @Test
    void getDefendantAccountParty_whenUserLacksPermission_throwsPermissionNotAllowedException() {
        // Arrange
        Long defendantAccountId = 1L;
        Long defendantAccountPartyId = 2L;

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);

        // Act & Assert
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class, () ->
                defendantAccountPartyService
                    .getDefendantAccountParty(defendantAccountId, defendantAccountPartyId)
        );

        assertThat(ex.getPermission()).containsExactly(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);

        verify(userStateService).getUserStateV1FromSecurityContext();
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verifyNoInteractions(defendantAccountPartyServiceProxy);
    }

    @Test
    void replaceDefendantAccountParty_whenUserHasPermission_passesPostedByAndBusinessUnitUserIdToProxy() {
        // Arrange
        Long defendantAccountId = 10L;
        Long defendantAccountPartyId = 20L;
        String ifMatch = "W/\"1\"";
        String businessUnitId = "5";
        short buId = Short.parseShort(businessUnitId);

        DefendantAccountParty request = new DefendantAccountParty();
        PartyResponseDefendantAccount expectedResponse = mock(PartyResponseDefendantAccount.class);

        BusinessUnitUser buUser = mock(BusinessUnitUser.class);
        when(buUser.getBusinessUnitUserId()).thenReturn("b-user-id");
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.getBusinessUnitUserForBusinessUnit(buId)).thenReturn(Optional.of(buUser));
        when(userState.getUserName()).thenReturn("theUserName");
        when(userState.hasBusinessUnitUserWithPermission(eq(buId), eq(FinesPermission.ACCOUNT_MAINTENANCE)))
            .thenReturn(true);

        when(defendantAccountPartyServiceProxy.replaceDefendantAccountParty(
            anyLong(), anyLong(), any(DefendantAccountParty.class), anyString(), anyString(), anyString(), anyString(),
            anyString()))
            .thenReturn(expectedResponse);

        // Act
        PartyResponseDefendantAccount actual = defendantAccountPartyService.replaceDefendantAccountParty(
            defendantAccountId, defendantAccountPartyId, ifMatch, businessUnitId, request
        );

        // Assert
        assertThat(actual).isSameAs(expectedResponse);

        ArgumentCaptor<String> postedByCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> postedByNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> buUserIdCaptor = ArgumentCaptor.forClass(String.class);

        verify(defendantAccountPartyServiceProxy).replaceDefendantAccountParty(
            eq(defendantAccountId),
            eq(defendantAccountPartyId),
            eq(request),
            eq(ifMatch),
            eq(businessUnitId),
            postedByCaptor.capture(),
            postedByNameCaptor.capture(),
            buUserIdCaptor.capture()
        );

        assertThat(postedByCaptor.getValue()).isEqualTo("b-user-id");
        assertThat(postedByNameCaptor.getValue()).isEqualTo("theUserName");
        assertThat(buUserIdCaptor.getValue()).isEqualTo("b-user-id");
    }

    @Test
    void addDefendantAccountParty_whenUserHasPermission_passesPostedByAndBusinessUnitUserIdToProxy() {
        // Arrange
        Long defendantAccountId = 10L;
        Long defendantAccountPartyId = 20L;
        String ifMatch = "W/\"1\"";
        String businessUnitId = "5";
        short buId = Short.parseShort(businessUnitId);

        // DTO - constructor should exist
        AddPartyRequestDefendantAccount request = defendantRequest();
        PartyResponseDefendantAccount expectedResponse = mock(PartyResponseDefendantAccount.class);

        BusinessUnitUser buUser = mock(BusinessUnitUser.class);
        when(buUser.getBusinessUnitUserId()).thenReturn("b-user-id");
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.getBusinessUnitUserForBusinessUnit(buId)).thenReturn(Optional.of(buUser));
        when(userState.getUserName()).thenReturn("theUserName");
        when(userState.hasBusinessUnitUserWithPermission(eq(buId), eq(FinesPermission.ACCOUNT_MAINTENANCE)))
            .thenReturn(true);

        when(defendantAccountPartyServiceProxy.addDefendantAccountParty(
            anyLong(), anyString(), anyString(), anyString(), anyString(), anyString(),
            any(AddPartyRequestDefendantAccount.class)))
            .thenReturn(expectedResponse);

        // Act
        PartyResponseDefendantAccount actual = defendantAccountPartyService.addDefendantAccountParty(
            defendantAccountId, ifMatch, businessUnitId, request
        );

        // Assert
        assertThat(actual).isSameAs(expectedResponse);

        ArgumentCaptor<String> postedByCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> postedByNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> buUserIdCaptor = ArgumentCaptor.forClass(String.class);

        verify(defendantAccountPartyServiceProxy).addDefendantAccountParty(
            eq(defendantAccountId),
            eq(businessUnitId),
            buUserIdCaptor.capture(),
            postedByCaptor.capture(),
            postedByNameCaptor.capture(),
            eq(ifMatch),
            eq(request)
        );

        assertThat(postedByCaptor.getValue()).isEqualTo("b-user-id");
        assertThat(postedByNameCaptor.getValue()).isEqualTo("theUserName");
        assertThat(buUserIdCaptor.getValue()).isEqualTo("b-user-id");
    }

    @Test
    void addDefendantAccountParty_nonPayingParentGuardianCallsLoggerAndReturnsResponse() {
        boolean isDebtor = false;
        AddPartyRequestDefendantAccount request = parentGuardianRequest(isDebtor);
        PartyResponseDefendantAccount response = responseWithPartyId("12345");
        stubPermittedAdd(response);
        when(pdplLoggingService.logAddedParentGuardian("12345", userState)).thenReturn(true);

        PartyResponseDefendantAccount result = defendantAccountPartyService.addDefendantAccountParty(
            10L, "\"1\"", "5", request);

        assertThat(result).isSameAs(response);
        verify(pdplLoggingService).logAddedParentGuardian("12345", userState);
    }

    @Test
    void addDefendantAccountParty_payingParentGuardianDoesNotCallPdpoLogger() {
        boolean isDebtor = true;
        stubPermittedAdd(responseWithPartyId("12345"));

        defendantAccountPartyService.addDefendantAccountParty(10L, "\"1\"", "5", parentGuardianRequest(isDebtor));

        verifyNoInteractions(pdplLoggingService);
    }

    @Test
    void addDefendantAccountParty_defendantDoesNotCallPdpoLogger() {
        boolean isDebtor = false;
        stubPermittedAdd(responseWithPartyId("12345"));
        AddPartyRequestDefendantAccount request = parentGuardianRequest(isDebtor);
        request.getDefendantAccountParty().setDefendantAccountPartyType(
            DefendantAccountParty.DefendantAccountPartyTypeEnum.DEFENDANT);

        defendantAccountPartyService.addDefendantAccountParty(10L, "\"1\"", "5", request);

        verifyNoInteractions(pdplLoggingService);
    }

    @Test
    void addDefendantAccountParty_missingAddedPartyIdDoesNotReturnSuccess() {
        boolean isDebtor = false;
        stubPermittedAdd(responseWithPartyId(null));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            defendantAccountPartyService.addDefendantAccountParty(
                10L, "\"1\"", "5", parentGuardianRequest(isDebtor)));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exception.getReason()).isEqualTo("Unable to record required Parent/Guardian PDPO log");
        verifyNoInteractions(pdplLoggingService);
    }

    @Test
    void addDefendantAccountParty_queueRejectionDoesNotReturnSuccess() {
        boolean isDebtor = false;
        stubPermittedAdd(responseWithPartyId("12345"));
        when(pdplLoggingService.logAddedParentGuardian("12345", userState)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            defendantAccountPartyService.addDefendantAccountParty(
                10L, "\"1\"", "5", parentGuardianRequest(isDebtor)));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        verify(pdplLoggingService).logAddedParentGuardian("12345", userState);
    }

    @Test
    void addDefendantAccountParty_queueExceptionDoesNotReturnSuccess() {
        boolean isDebtor = false;
        stubPermittedAdd(responseWithPartyId("12345"));
        when(pdplLoggingService.logAddedParentGuardian("12345", userState))
            .thenThrow(new IllegalStateException("queue unavailable"));

        assertThrows(IllegalStateException.class, () -> defendantAccountPartyService.addDefendantAccountParty(
            10L, "\"1\"", "5", parentGuardianRequest(isDebtor)));
    }

    @Test
    void addDefendantAccountParty_proxyFailureDoesNotCallPdpoLogger() {
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.getBusinessUnitUserForBusinessUnit((short) 5)).thenReturn(Optional.empty());
        when(userState.getUserName()).thenReturn("user");
        when(userState.hasBusinessUnitUserWithPermission((short) 5, FinesPermission.ACCOUNT_MAINTENANCE))
            .thenReturn(true);
        ResponseStatusException proxyFailure = new ResponseStatusException(HttpStatus.CONFLICT);
        when(defendantAccountPartyServiceProxy.addDefendantAccountParty(
            anyLong(), anyString(), anyString(), anyString(), anyString(), anyString(), any()))
            .thenThrow(proxyFailure);

        ResponseStatusException actual = assertThrows(ResponseStatusException.class, () ->
            defendantAccountPartyService.addDefendantAccountParty(
                10L, "\"1\"", "5", parentGuardianRequest(false)));

        assertThat(actual).isSameAs(proxyFailure);
        verifyNoInteractions(pdplLoggingService);
    }

    @Test
    void addDefendantAccountParty_nonPayingParentGuardianWithoutPermissionDoesNotCallPdpoLogger() {
        short businessUnitId = 3;
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.hasBusinessUnitUserWithPermission(businessUnitId, FinesPermission.ACCOUNT_MAINTENANCE))
            .thenReturn(false);

        PermissionNotAllowedException exception = assertThrows(PermissionNotAllowedException.class, () ->
            defendantAccountPartyService.addDefendantAccountParty(
                100L, "W/\"1\"", String.valueOf(businessUnitId), parentGuardianRequest(false)));

        assertThat(exception.getPermission()).containsExactly(FinesPermission.ACCOUNT_MAINTENANCE);
        assertThat(exception.getBusinessUnitId()).isEqualTo(businessUnitId);
        verifyNoInteractions(defendantAccountPartyServiceProxy, pdplLoggingService);
    }

    private void stubPermittedAdd(PartyResponseDefendantAccount response) {
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.getBusinessUnitUserForBusinessUnit((short) 5)).thenReturn(Optional.empty());
        when(userState.getUserName()).thenReturn("user");
        when(userState.hasBusinessUnitUserWithPermission((short) 5, FinesPermission.ACCOUNT_MAINTENANCE))
            .thenReturn(true);
        when(defendantAccountPartyServiceProxy.addDefendantAccountParty(
            anyLong(), anyString(), anyString(), anyString(), anyString(), anyString(), any()))
            .thenReturn(response);
    }

    private AddPartyRequestDefendantAccount parentGuardianRequest(boolean debtor) {
        return AddPartyRequestDefendantAccount.builder()
            .defendantAccountParty(DefendantAccountParty.builder()
                .defendantAccountPartyType(DefendantAccountParty.DefendantAccountPartyTypeEnum.PARENT_GUARDIAN)
                .isDebtor(debtor)
                .build())
            .build();
    }

    private AddPartyRequestDefendantAccount defendantRequest() {
        return AddPartyRequestDefendantAccount.builder()
            .defendantAccountParty(DefendantAccountParty.builder()
                .defendantAccountPartyType(DefendantAccountParty.DefendantAccountPartyTypeEnum.DEFENDANT)
                .isDebtor(true)
                .build())
            .build();
    }

    private PartyResponseDefendantAccount responseWithPartyId(String partyId) {
        return PartyResponseDefendantAccount.builder()
            .defendantAccountParty(DefendantAccountParty.builder()
                .partyDetails(PartyDetailsCommonStrict.builder().partyId(partyId).build())
                .build())
            .build();
    }


    @Test
    void replaceDefendantAccountParty_whenBusinessUnitUserMissing_usesUserNameForPostedByAndEmptyBusinessUnitUserId() {
        // Arrange
        Long defendantAccountId = 11L;
        Long defendantAccountPartyId = 22L;
        String ifMatch = "W/\"2\"";
        String businessUnitId = "7";
        short buId = Short.parseShort(businessUnitId);

        DefendantAccountParty request = new DefendantAccountParty();
        PartyResponseDefendantAccount expectedResponse = mock(PartyResponseDefendantAccount.class);

        // No BusinessUnitUser present
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.getBusinessUnitUserForBusinessUnit(buId)).thenReturn(Optional.empty());
        when(userState.getUserName()).thenReturn("theUserName");
        when(userState.hasBusinessUnitUserWithPermission(eq(buId), eq(FinesPermission.ACCOUNT_MAINTENANCE)))
            .thenReturn(true);

        when(defendantAccountPartyServiceProxy.replaceDefendantAccountParty(
            anyLong(), anyLong(), any(DefendantAccountParty.class), anyString(), anyString(), anyString(), anyString(),
            anyString()))
            .thenReturn(expectedResponse);

        // Act
        PartyResponseDefendantAccount actual = defendantAccountPartyService.replaceDefendantAccountParty(
            defendantAccountId, defendantAccountPartyId, ifMatch, businessUnitId, request
        );

        // Assert
        assertThat(actual).isSameAs(expectedResponse);

        ArgumentCaptor<String> postedByCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> postedByNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> buUserIdCaptor = ArgumentCaptor.forClass(String.class);

        verify(defendantAccountPartyServiceProxy).replaceDefendantAccountParty(
            eq(defendantAccountId),
            eq(defendantAccountPartyId),
            eq(request),
            eq(ifMatch),
            eq(businessUnitId),
            postedByCaptor.capture(),
            postedByNameCaptor.capture(),
            buUserIdCaptor.capture()
        );

        assertThat(postedByCaptor.getValue()).isEqualTo("theUserName");
        assertThat(postedByNameCaptor.getValue()).isEqualTo("theUserName");
        // when no business unit user present, the helper returns empty string
        assertThat(buUserIdCaptor.getValue()).isEqualTo("");
    }

    @Test
    void addDefendantAccountParty_whenBusinessUnitUserMissing_usesUserNameForPostedByAndEmptyBusinessUnitUserId() {
        // Arrange
        Long defendantAccountId = 11L;
        Long defendantAccountPartyId = 22L;
        String ifMatch = "W/\"2\"";
        String businessUnitId = "7";
        short buId = Short.parseShort(businessUnitId);

        AddPartyRequestDefendantAccount request = defendantRequest();
        PartyResponseDefendantAccount expectedResponse = mock(PartyResponseDefendantAccount.class);

        // No BusinessUnitUser present
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.getBusinessUnitUserForBusinessUnit(buId)).thenReturn(Optional.empty());
        when(userState.getUserName()).thenReturn("theUserName");
        when(userState.hasBusinessUnitUserWithPermission(eq(buId), eq(FinesPermission.ACCOUNT_MAINTENANCE)))
            .thenReturn(true);

        when(defendantAccountPartyServiceProxy.addDefendantAccountParty(
            anyLong(), anyString(), anyString(), anyString(), anyString(), anyString(),
            any(AddPartyRequestDefendantAccount.class)))
            .thenReturn(expectedResponse);

        // Act
        PartyResponseDefendantAccount actual = defendantAccountPartyService.addDefendantAccountParty(
            defendantAccountId, ifMatch, businessUnitId, request
        );

        // Assert
        assertThat(actual).isSameAs(expectedResponse);

        ArgumentCaptor<String> postedByCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> postedByNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> buUserIdCaptor = ArgumentCaptor.forClass(String.class);

        verify(defendantAccountPartyServiceProxy).addDefendantAccountParty(
            eq(defendantAccountId),
            eq(businessUnitId),
            buUserIdCaptor.capture(),
            postedByCaptor.capture(),
            postedByNameCaptor.capture(),
            eq(ifMatch),
            eq(request)
        );

        assertThat(postedByCaptor.getValue()).isEqualTo("theUserName");
        assertThat(postedByNameCaptor.getValue()).isEqualTo("theUserName");
        // when no business unit user present, the helper returns empty string
        assertThat(buUserIdCaptor.getValue()).isEqualTo("");
    }


    @Test
    void replaceDefendantAccountParty_whenUserLacksPermission_throwsPermissionNotAllowedException() {
        // Arrange
        Long defendantAccountId = 100L;
        Long defendantAccountPartyId = 200L;
        String ifMatch = "W/\"X\"";
        Short businessUnitId = 3;
        String stringBusinessUnitId = String.valueOf(businessUnitId);
        DefendantAccountParty request = new DefendantAccountParty();

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.hasBusinessUnitUserWithPermission(businessUnitId, FinesPermission.ACCOUNT_MAINTENANCE))
            .thenReturn(false);

        // Act & Assert
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountPartyService.replaceDefendantAccountParty(
                defendantAccountId, defendantAccountPartyId, ifMatch, stringBusinessUnitId, request)
        );

        assertThat(ex.getPermission()).containsExactly(FinesPermission.ACCOUNT_MAINTENANCE);
        assertThat(ex.getBusinessUnitId()).isEqualTo(businessUnitId);

        verify(userStateService).getUserStateV1FromSecurityContext();
        verify(userState).hasBusinessUnitUserWithPermission(businessUnitId, FinesPermission.ACCOUNT_MAINTENANCE);
        verifyNoInteractions(defendantAccountPartyServiceProxy);
    }

    @Test
    void addDefendantAccountParty_whenUserLacksPermission_throwsPermissionNotAllowedException() {
        // Arrange
        Long defendantAccountId = 100L;
        Long defendantAccountPartyId = 200L;
        String ifMatch = "W/\"X\"";
        Short businessUnitId = 3;
        String stringBusinessUnitId = String.valueOf(businessUnitId);
        AddPartyRequestDefendantAccount request = new AddPartyRequestDefendantAccount();

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.hasBusinessUnitUserWithPermission(businessUnitId, FinesPermission.ACCOUNT_MAINTENANCE))
            .thenReturn(false);

        // Act & Assert
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountPartyService.addDefendantAccountParty(
                defendantAccountId, ifMatch, stringBusinessUnitId, request)
        );

        assertThat(ex.getPermission()).containsExactly(FinesPermission.ACCOUNT_MAINTENANCE);
        assertThat(ex.getBusinessUnitId()).isEqualTo(businessUnitId);

        verify(userStateService).getUserStateV1FromSecurityContext();
        verify(userState).hasBusinessUnitUserWithPermission(businessUnitId, FinesPermission.ACCOUNT_MAINTENANCE);
        verifyNoInteractions(defendantAccountPartyServiceProxy);
    }

    @Test
    void removeDefendantAccountParty_whenUserHasPermission_passesPostedByAndBusinessUnitUserIdToProxy() {
        // Arrange
        Long defendantAccountId = 33L;
        Long defendantAccountPartyId = 44L;
        short businessUnitId = 9;
        String ifMatch = "W/\"3\"";
        RemoveDefendantAccountPartyRequestDefendantAccount request =
            new RemoveDefendantAccountPartyRequestDefendantAccount();
        RemoveDefendantAccountPartyResponseDefendantAccount expectedResponse =
            mock(RemoveDefendantAccountPartyResponseDefendantAccount.class);

        BusinessUnitUser buUser = mock(BusinessUnitUser.class);
        when(buUser.getBusinessUnitUserId()).thenReturn("bu-user-id");
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.getBusinessUnitUserForBusinessUnit(businessUnitId)).thenReturn(Optional.of(buUser));
        when(userState.getUserName()).thenReturn("theUserName");
        when(userState.hasBusinessUnitUserWithPermission(businessUnitId, FinesPermission.ACCOUNT_MAINTENANCE))
            .thenReturn(true);
        when(defendantAccountPartyServiceProxy.removeDefendantAccountParty(
            defendantAccountId,
            defendantAccountPartyId,
            businessUnitId,
            "bu-user-id",
            "bu-user-id",
            "theUserName",
            ifMatch,
            request
        )).thenReturn(expectedResponse);

        // Act
        RemoveDefendantAccountPartyResponseDefendantAccount actual =
            defendantAccountPartyService.removeDefendantAccountParty(
                defendantAccountId, defendantAccountPartyId, businessUnitId, ifMatch, request);

        // Assert
        assertThat(actual).isSameAs(expectedResponse);

        ArgumentCaptor<String> buUserIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> postedByCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> postedByNameCaptor = ArgumentCaptor.forClass(String.class);

        verify(defendantAccountPartyServiceProxy).removeDefendantAccountParty(
            eq(defendantAccountId),
            eq(defendantAccountPartyId),
            eq(businessUnitId),
            buUserIdCaptor.capture(),
            postedByCaptor.capture(),
            postedByNameCaptor.capture(),
            eq(ifMatch),
            eq(request)
        );

        // When businessUnitUserId is not null, not blank, it should be the same as the value derived from the auth
        // token
        assertThat(buUserIdCaptor.getValue()).isEqualTo("bu-user-id");
        assertThat(postedByCaptor.getValue()).isEqualTo("bu-user-id");
        assertThat(postedByNameCaptor.getValue()).isEqualTo("theUserName");
    }

    @Test
    void removeDefendantAccountParty_whenBusinessUnitUserMissing_usesUserNameAndEmptyBusinessUnitUserId() {
        // Arrange
        Long defendantAccountId = 55L;
        Long defendantAccountPartyId = 66L;
        short businessUnitId = 11;
        String ifMatch = "W/\"4\"";
        RemoveDefendantAccountPartyRequestDefendantAccount request =
            new RemoveDefendantAccountPartyRequestDefendantAccount();
        RemoveDefendantAccountPartyResponseDefendantAccount expectedResponse =
            mock(RemoveDefendantAccountPartyResponseDefendantAccount.class);

        // No BusinessUnitUser present
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.getBusinessUnitUserForBusinessUnit(businessUnitId)).thenReturn(Optional.empty());
        when(userState.getUserName()).thenReturn("fallback-user");
        when(userState.hasBusinessUnitUserWithPermission(businessUnitId, FinesPermission.ACCOUNT_MAINTENANCE))
            .thenReturn(true);
        when(defendantAccountPartyServiceProxy.removeDefendantAccountParty(
            defendantAccountId,
            defendantAccountPartyId,
            businessUnitId,
            "",
            "fallback-user",
            "fallback-user",
            ifMatch,
            request
        )).thenReturn(expectedResponse);

        // Act
        RemoveDefendantAccountPartyResponseDefendantAccount actual =
            defendantAccountPartyService.removeDefendantAccountParty(
                defendantAccountId, defendantAccountPartyId, businessUnitId, ifMatch, request);

        // Assert
        assertThat(actual).isSameAs(expectedResponse);

        ArgumentCaptor<String> buUserIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> postedByCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> postedByNameCaptor = ArgumentCaptor.forClass(String.class);

        verify(defendantAccountPartyServiceProxy).removeDefendantAccountParty(
            eq(defendantAccountId),
            eq(defendantAccountPartyId),
            eq(businessUnitId),
            buUserIdCaptor.capture(),
            postedByCaptor.capture(),
            postedByNameCaptor.capture(),
            eq(ifMatch),
            eq(request)
        );

        // When BusinessUnitUser is not provided the helper returns an empty string
        assertThat(buUserIdCaptor.getValue()).isEmpty();
        assertThat(postedByCaptor.getValue()).isEqualTo("fallback-user");
        assertThat(postedByNameCaptor.getValue()).isEqualTo("fallback-user");
    }

    @Test
    void removeDefendantAccountParty_whenUserLacksPermission_throwsPermissionNotAllowedException() {
        // Arrange
        Long defendantAccountId = 77L;
        Long defendantAccountPartyId = 88L;
        short businessUnitId = 13;
        String ifMatch = "W/\"5\"";
        RemoveDefendantAccountPartyRequestDefendantAccount request =
            new RemoveDefendantAccountPartyRequestDefendantAccount();

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.hasBusinessUnitUserWithPermission(
            businessUnitId,
            FinesPermission.ACCOUNT_MAINTENANCE
        )).thenReturn(false);

        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class, () ->
                defendantAccountPartyService.removeDefendantAccountParty(
                    defendantAccountId, defendantAccountPartyId, businessUnitId, ifMatch, request)
        );

        // When the user does not have the correct permission, the call is not passed to the proxy
        assertThat(ex.getPermission()).containsExactly(FinesPermission.ACCOUNT_MAINTENANCE);
        assertThat(ex.getBusinessUnitId()).isNull();
        verify(userStateService).getUserStateV1FromSecurityContext();
        verify(userState).hasBusinessUnitUserWithPermission(businessUnitId, FinesPermission.ACCOUNT_MAINTENANCE);
        verifyNoInteractions(defendantAccountPartyServiceProxy);
    }
}
