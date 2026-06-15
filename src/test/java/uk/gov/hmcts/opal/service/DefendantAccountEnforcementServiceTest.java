package uk.gov.hmcts.opal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissionsFor;

import java.math.BigInteger;
import java.util.Optional;

import tools.jackson.core.JacksonException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountEnforcementServiceProxy;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldRequest;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldResponse;

@ExtendWith(MockitoExtension.class)
class DefendantAccountEnforcementServiceTest {

    @Mock
    private DefendantAccountEnforcementServiceProxy defendantAccountEnforcementServiceProxy;

    @Mock
    private UserStateService userStateService;

    @Mock
    private UserState userState;

    @InjectMocks
    private DefendantAccountEnforcementService defendantAccountEnforcementService;

    @Test
    void addEnforcement_whenUserHasPermission_callsProxyAndReturnsResult() throws JacksonException {
        // arrange
        Long defendantAccountId = 77L;
        Short businessUnitId = 10;
        String ifMatch = "3";
        String authHeader = "Bearer abc";
        AddDefendantAccountEnforcementRequest req = mock(AddDefendantAccountEnforcementRequest.class);

        AddEnforcementResponse proxyResponse = AddEnforcementResponse.builder()
            .enforcementId("ENF123")
            .defendantAccountId("77")
            .version(3)
            .build();

        when(userStateService.checkForAuthorisedUser()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.ENTER_ENFORCEMENT)).thenReturn(true);

        // business unit user lookup returns an Optional<BusinessUnitUser> with a non-blank ID
        BusinessUnitUser buUser = mock(BusinessUnitUser.class);
        when(buUser.getBusinessUnitUserId()).thenReturn("BU-USER-1");
        when(userState.getBusinessUnitUserForBusinessUnit((short)10))
            .thenReturn(java.util.Optional.of(buUser));

        when(defendantAccountEnforcementServiceProxy.addEnforcement(
            defendantAccountId, businessUnitId, "BU-USER-1", ifMatch, req))
            .thenReturn(proxyResponse);

        // act
        AddEnforcementResponse result =
            defendantAccountEnforcementService
                .addEnforcement(defendantAccountId, businessUnitId, ifMatch, req);

        // assert
        assertSame(proxyResponse, result, "Should return exactly the proxy response");

        // verify interactions
        verify(userStateService).checkForAuthorisedUser();
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.ENTER_ENFORCEMENT);
        verify(userState).getBusinessUnitUserForBusinessUnit((short)10);
        verify(defendantAccountEnforcementServiceProxy)
            .addEnforcement(defendantAccountId, businessUnitId, "BU-USER-1", ifMatch, req);
        verifyNoMoreInteractions(userStateService, userState, defendantAccountEnforcementServiceProxy);
    }

    @Test
    void addEnforcement_whenUserLacksPermission_throwsPermissionNotAllowedException() {
        // arrange
        Long defendantAccountId = 77L;
        Short businessUnitId = 10;
        String authHeader = "Bearer abc";

        when(userStateService.checkForAuthorisedUser()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.ENTER_ENFORCEMENT))
            .thenReturn(false);

        // act + assert
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountEnforcementService
                .addEnforcement(defendantAccountId, businessUnitId, "3", null)
        );

        assertTrue(
            ex.getMessage() == null || ex.getMessage().contains(FinesPermission.ENTER_ENFORCEMENT.name()),
            "Exception should mention ENTER_ENFORCEMENT"
        );
        assertThat(ex.getPermission()).containsExactly(FinesPermission.ENTER_ENFORCEMENT);

        verify(userStateService).checkForAuthorisedUser();
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.ENTER_ENFORCEMENT);
        verifyNoInteractions(defendantAccountEnforcementServiceProxy);
    }

    @Test
    void addEnforcement_whenBusinessUnitUserIdBlank_usesNullInProxyCall() throws JacksonException {
        // arrange
        Long defendantAccountId = 77L;
        Short businessUnitId = 10;
        String authHeader = "Bearer abc";

        AddDefendantAccountEnforcementRequest req = mock(AddDefendantAccountEnforcementRequest.class);

        when(userStateService.checkForAuthorisedUser()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.ENTER_ENFORCEMENT)).thenReturn(true);

        // return Optional<BusinessUnitUser> but with blank ID -> results in null
        BusinessUnitUser buUser = mock(BusinessUnitUser.class);
        when(buUser.getBusinessUnitUserId()).thenReturn("   "); // blank
        when(userState.getBusinessUnitUserForBusinessUnit((short)10))
            .thenReturn(java.util.Optional.of(buUser));

        AddEnforcementResponse proxyResult = AddEnforcementResponse.builder()
            .enforcementId("X")
            .build();

        when(defendantAccountEnforcementServiceProxy.addEnforcement(
            eq(defendantAccountId),
            eq(businessUnitId),
            isNull(),                   // IMPORTANT: businessUnitUserId expected to be null
            eq("3"),
            eq(req)
        )).thenReturn(proxyResult);

        // act
        AddEnforcementResponse out =
            defendantAccountEnforcementService
                .addEnforcement(defendantAccountId, businessUnitId, "3", req);

        // assert
        assertNotNull(out);
        verify(defendantAccountEnforcementServiceProxy).addEnforcement(
            eq(defendantAccountId),
            eq(businessUnitId),
            isNull(),                   // verifies null is passed
            eq("3"),
            eq(req)
        );
    }


    @Test
    void testGetEnforcementStatus() {
        // Arrange
        EnforcementStatus status = EnforcementStatus.builder()
            .employerFlag(true)
            .isHmrcCheckEligible(true)
            .version(new BigInteger("1234567890123345678901234567890"))
            .build();
        when(userStateService.checkForAuthorisedUser()).thenReturn(allPermissionsUser());
        when(defendantAccountEnforcementServiceProxy.getEnforcementStatus(anyLong())).thenReturn(status);

        // Act
        EnforcementStatus response = defendantAccountEnforcementService
            .getEnforcementStatus(33L);

        // Assert
        assertNotNull(response);
        assertTrue(response.getEmployerFlag());
        assertTrue(response.getIsHmrcCheckEligible());
        assertEquals(new BigInteger("1234567890123345678901234567890"), response.getVersion());

    }

    @Test
    void removeEnforcementHold_whenUserHasPermission_callsProxyAndReturnsResult() {
        // arrange
        Long defendantAccountId = 77L;
        Short businessUnitId = 10;
        String ifMatch = "\"7\"";
        String authHeader = "Bearer abc";

        RemoveDefendantAccountEnforcementHoldRequest request =
            RemoveDefendantAccountEnforcementHoldRequest.builder()
                .reason("remove hold reason")
                .build();

        RemoveDefendantAccountEnforcementHoldResponse proxyResponse =
            RemoveDefendantAccountEnforcementHoldResponse.builder().build();

        UserState userState = allPermissionsUser();

        String businessUnitUserId = userState.getBusinessUnitUserForBusinessUnit(businessUnitId)
            .map(BusinessUnitUser::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElse(userState.getUserName());

        when(userStateService.checkForAuthorisedUser()).thenReturn(userState);

        when(defendantAccountEnforcementServiceProxy.removeEnforcementHold(
            eq(defendantAccountId),
            eq(businessUnitId),
            eq(businessUnitUserId),
            eq(ifMatch),
            eq(request)
        )).thenReturn(proxyResponse);

        // act
        RemoveDefendantAccountEnforcementHoldResponse result =
            defendantAccountEnforcementService.removeEnforcementHold(
                defendantAccountId,
                businessUnitId,
                ifMatch,
                request
            );

        // assert
        assertSame(proxyResponse, result);

        verify(userStateService).checkForAuthorisedUser();
        verify(defendantAccountEnforcementServiceProxy).removeEnforcementHold(
            eq(defendantAccountId),
            eq(businessUnitId),
            eq(businessUnitUserId),
            eq(ifMatch),
            eq(request)
        );
        verifyNoMoreInteractions(defendantAccountEnforcementServiceProxy);
    }

    @Test
    void removeEnforcementHold_whenUserLacksPermission_throwsPermissionNotAllowedException() {
        // arrange
        Long defendantAccountId = 77L;
        Short businessUnitId = 10;
        String ifMatch = "\"7\"";
        String authHeader = "Bearer abc";

        RemoveDefendantAccountEnforcementHoldRequest request =
            RemoveDefendantAccountEnforcementHoldRequest.builder()
                .reason("remove hold reason")
                .build();

        when(userStateService.checkForAuthorisedUser()).thenReturn(userState);
        when(userState.getBusinessUnitUserForBusinessUnit((short) 10)).thenReturn(Optional.empty());
        when(userState.getUserName()).thenReturn("user-1");
        when(userState.hasBusinessUnitUserWithPermission((short) 10, FinesPermission.ENTER_ENFORCEMENT))
            .thenReturn(false);

        // act
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountEnforcementService.removeEnforcementHold(
                defendantAccountId,
                businessUnitId,
                ifMatch,
                request
            )
        );

        // assert
        assertThat(ex.getPermission()).containsExactly(FinesPermission.ENTER_ENFORCEMENT);

        verify(userStateService).checkForAuthorisedUser();
        verify(userState).getBusinessUnitUserForBusinessUnit((short) 10);
        verify(userState).getUserName();
        verify(userState).hasBusinessUnitUserWithPermission((short) 10, FinesPermission.ENTER_ENFORCEMENT);
        verifyNoInteractions(defendantAccountEnforcementServiceProxy);
    }

    @Test
    void removeEnforcementHold_whenBusinessUnitUserIdBlank_usesUserNameInProxyCall() {
        // arrange
        Long defendantAccountId = 77L;
        Short businessUnitId = 10;
        String ifMatch = "\"7\"";
        String authHeader = "Bearer abc";

        RemoveDefendantAccountEnforcementHoldRequest request =
            RemoveDefendantAccountEnforcementHoldRequest.builder()
                .reason("remove hold reason")
                .build();

        RemoveDefendantAccountEnforcementHoldResponse proxyResponse =
            RemoveDefendantAccountEnforcementHoldResponse.builder().build();

        UserState userState = UserState.builder()
            .userId(1L)
            .userName("user-1")
            .businessUnitUser(Set.of(BusinessUnitUser.builder()
                .businessUnitId((short) 10)
                .businessUnitUserId("   ")
                .permissions(permissionsFor(FinesPermission.ENTER_ENFORCEMENT))
                .build()))
            .build();

        when(userStateService.checkForAuthorisedUser()).thenReturn(userState);

        when(defendantAccountEnforcementServiceProxy.removeEnforcementHold(
            eq(defendantAccountId),
            eq(businessUnitId),
            eq("user-1"),
            eq(ifMatch),
            eq(request)
        )).thenReturn(proxyResponse);

        // act
        RemoveDefendantAccountEnforcementHoldResponse result =
            defendantAccountEnforcementService.removeEnforcementHold(
                defendantAccountId,
                businessUnitId,
                ifMatch,
                request
            );

        // assert
        assertSame(proxyResponse, result);

        verify(userStateService).checkForAuthorisedUser();
        verify(defendantAccountEnforcementServiceProxy).removeEnforcementHold(
            eq(defendantAccountId),
            eq(businessUnitId),
            eq("user-1"),
            eq(ifMatch),
            eq(request)
        );
        verifyNoMoreInteractions(defendantAccountEnforcementServiceProxy);
    }

    @Test
    void removeEnforcementHold_whenNoBusinessUnitUser_usesUserNameInProxyCall() {
        // arrange
        Long defendantAccountId = 77L;
        Short businessUnitId = 10;
        String ifMatch = "\"7\"";
        String authHeader = "Bearer abc";

        RemoveDefendantAccountEnforcementHoldRequest request =
            RemoveDefendantAccountEnforcementHoldRequest.builder()
                .reason("remove hold reason")
                .build();

        RemoveDefendantAccountEnforcementHoldResponse proxyResponse =
            RemoveDefendantAccountEnforcementHoldResponse.builder()
                .defendantAccountId("77")
                .version(BigInteger.valueOf(7))
                .build();

        UserState userState = mock(UserState.class);

        when(userStateService.checkForAuthorisedUser()).thenReturn(userState);
        when(userState.hasBusinessUnitUserWithPermission((short) 10, FinesPermission.ENTER_ENFORCEMENT))
            .thenReturn(true);
        when(userState.getBusinessUnitUserForBusinessUnit((short) 10))
            .thenReturn(Optional.empty());
        when(userState.getUserName()).thenReturn("user-1");

        when(defendantAccountEnforcementServiceProxy.removeEnforcementHold(
            eq(defendantAccountId),
            eq(businessUnitId),
            eq("user-1"),
            eq(ifMatch),
            eq(request)
        )).thenReturn(proxyResponse);

        // act
        RemoveDefendantAccountEnforcementHoldResponse result =
            defendantAccountEnforcementService.removeEnforcementHold(
                defendantAccountId,
                businessUnitId,
                ifMatch,
                request
            );

        // assert
        assertSame(proxyResponse, result);

        verify(userStateService).checkForAuthorisedUser();
        verify(userState).hasBusinessUnitUserWithPermission((short) 10, FinesPermission.ENTER_ENFORCEMENT);
        verify(userState).getBusinessUnitUserForBusinessUnit((short) 10);
        verify(userState).getUserName();
        verify(defendantAccountEnforcementServiceProxy).removeEnforcementHold(
            eq(defendantAccountId),
            eq(businessUnitId),
            eq("user-1"),
            eq(ifMatch),
            eq(request)
        );
        verifyNoMoreInteractions(defendantAccountEnforcementServiceProxy);
    }

    @Test
    void removeEnforcementHold_whenIfMatchIsNull_passesNullToProxy() {
        Long defendantAccountId = 77L;
        Short businessUnitId = 10;
        String authHeader = "Bearer abc";

        RemoveDefendantAccountEnforcementHoldRequest request =
            RemoveDefendantAccountEnforcementHoldRequest.builder()
                .reason("remove hold reason")
                .build();

        RemoveDefendantAccountEnforcementHoldResponse proxyResponse =
            RemoveDefendantAccountEnforcementHoldResponse.builder()
                .defendantAccountId("77")
                .version(BigInteger.valueOf(7))
                .build();

        UserState userState = allPermissionsUser();

        String businessUnitUserId = userState.getBusinessUnitUserForBusinessUnit(businessUnitId)
            .map(BusinessUnitUser::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElse(userState.getUserName());

        when(userStateService.checkForAuthorisedUser()).thenReturn(userState);

        when(defendantAccountEnforcementServiceProxy.removeEnforcementHold(
            eq(defendantAccountId),
            eq(businessUnitId),
            eq(businessUnitUserId),
            isNull(), // key assertion
            eq(request)
        )).thenReturn(proxyResponse);

        RemoveDefendantAccountEnforcementHoldResponse result =
            defendantAccountEnforcementService.removeEnforcementHold(
                defendantAccountId,
                businessUnitId,
                null, // key input
                request
            );

        assertSame(proxyResponse, result);

        verify(defendantAccountEnforcementServiceProxy).removeEnforcementHold(
            eq(defendantAccountId),
            eq(businessUnitId),
            eq(businessUnitUserId),
            isNull(),
            eq(request)
        );
    }
}
