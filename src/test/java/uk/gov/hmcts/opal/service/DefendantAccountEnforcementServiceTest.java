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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import tools.jackson.core.JacksonException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUserV2;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountEnforcementServiceProxy;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldRequest;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldResponse;
import uk.gov.hmcts.opal.generated.model.AddEnforcementRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.AddEnforcementResponseDefendantAccount;

@ExtendWith(MockitoExtension.class)
class DefendantAccountEnforcementServiceTest {

    @Mock
    private DefendantAccountEnforcementServiceProxy defendantAccountEnforcementServiceProxy;

    @Mock
    private UserStateService userStateService;

    @Mock
    private UserStateV2 userState;

    @InjectMocks
    private DefendantAccountEnforcementService defendantAccountEnforcementService;

    @Test
    void addEnforcement_whenUserHasPermission_callsProxyAndReturnsResult() throws JacksonException {
        // arrange
        Long defendantAccountId = 77L;
        Short businessUnitId = 10;
        String ifMatch = "3";
        AddEnforcementRequestDefendantAccount req = mock(AddEnforcementRequestDefendantAccount.class);

        AddEnforcementResponseDefendantAccount proxyResponse = AddEnforcementResponseDefendantAccount.builder()
            .enforcementId("ENF123")
            .defendantAccountId("77")
            .version(3)
            .build();

        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.ENTER_ENFORCEMENT)).thenReturn(true);

        // business unit user lookup returns an Optional<BusinessUnitUser> with a non-blank ID
        BusinessUnitUserV2 buUser = mock(BusinessUnitUserV2.class);
        when(buUser.getBusinessUnitUserId()).thenReturn("BU-USER-1");
        when(userState.getBusinessUnitUserForBusinessUnit((short)10))
            .thenReturn(java.util.Optional.of(buUser));

        when(defendantAccountEnforcementServiceProxy.addEnforcement(
            defendantAccountId, businessUnitId, "BU-USER-1", ifMatch, req))
            .thenReturn(proxyResponse);

        // act
        AddEnforcementResponseDefendantAccount result =
            defendantAccountEnforcementService
                .addEnforcement(defendantAccountId, businessUnitId, ifMatch, req);

        // assert
        assertSame(proxyResponse, result, "Should return exactly the proxy response");

        // verify interactions
        verify(userStateService).getUserStateFromSecurityContext();
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

        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
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

        verify(userStateService).getUserStateFromSecurityContext();
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.ENTER_ENFORCEMENT);
        verifyNoInteractions(defendantAccountEnforcementServiceProxy);
    }

    @Test
    void addEnforcement_whenBusinessUnitUserIdBlank_usesNullInProxyCall() throws JacksonException {
        // arrange
        Long defendantAccountId = 77L;
        Short businessUnitId = 10;

        AddEnforcementRequestDefendantAccount req = mock(AddEnforcementRequestDefendantAccount.class);

        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.ENTER_ENFORCEMENT)).thenReturn(true);

        // return Optional<BusinessUnitUser> but with blank ID -> results in null
        BusinessUnitUserV2 buUser = mock(BusinessUnitUserV2.class);
        when(buUser.getBusinessUnitUserId()).thenReturn("   "); // blank
        when(userState.getBusinessUnitUserForBusinessUnit((short)10))
            .thenReturn(java.util.Optional.of(buUser));

        AddEnforcementResponseDefendantAccount proxyResult = AddEnforcementResponseDefendantAccount.builder()
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
        AddEnforcementResponseDefendantAccount out =
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
        when(userStateService.getUserStateFromSecurityContext()).thenReturn(allPermissionsUser());
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

        RemoveDefendantAccountEnforcementHoldRequest request =
            RemoveDefendantAccountEnforcementHoldRequest.builder()
                .reason("remove hold reason")
                .build();

        RemoveDefendantAccountEnforcementHoldResponse proxyResponse =
            RemoveDefendantAccountEnforcementHoldResponse.builder().build();

        UserStateV2 userState = allPermissionsUser();

        String businessUnitUserId = userState.getBusinessUnitUserForBusinessUnit(businessUnitId)
            .map(BusinessUnitUserV2::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElse(userState.getUsername());

        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);

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

        verify(userStateService).getUserStateFromSecurityContext();
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

        RemoveDefendantAccountEnforcementHoldRequest request =
            RemoveDefendantAccountEnforcementHoldRequest.builder()
                .reason("remove hold reason")
                .build();

        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
        when(userState.getBusinessUnitUserForBusinessUnit((short) 10)).thenReturn(Optional.empty());
        when(userState.getUsername()).thenReturn("user-1");
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

        verify(userStateService).getUserStateFromSecurityContext();
        verify(userState).getBusinessUnitUserForBusinessUnit((short) 10);
        verify(userState).getUsername();
        verify(userState).hasBusinessUnitUserWithPermission((short) 10, FinesPermission.ENTER_ENFORCEMENT);
        verifyNoInteractions(defendantAccountEnforcementServiceProxy);
    }

    @Test
    void removeEnforcementHold_whenBusinessUnitUserIdBlank_usesUserNameInProxyCall() {
        // arrange
        Long defendantAccountId = 77L;
        Short businessUnitId = 10;
        String ifMatch = "\"7\"";

        RemoveDefendantAccountEnforcementHoldRequest request =
            RemoveDefendantAccountEnforcementHoldRequest.builder()
                .reason("remove hold reason")
                .build();

        RemoveDefendantAccountEnforcementHoldResponse proxyResponse =
            RemoveDefendantAccountEnforcementHoldResponse.builder().build();

        UserStateV2 userState = UserStateV2.builder()
            .userId(1L)
            .username("user-1")
            .domains(new HashMap<>() {{
                    put(Domain.CONFISCATION, DomainBusinessUnitUsers
                        .builder()
                        .businessUnitUsers(new ArrayList<>() {{
                                    add(BusinessUnitUserV2
                                        .builder()
                                        .businessUnitId((short) 10)
                                        .businessUnitUserId("   ")
                                        .permissions(permissionsFor(FinesPermission.ENTER_ENFORCEMENT))
                                        .build());
                            }})
                        .build());
                }})
            .build();

        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);

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

        verify(userStateService).getUserStateFromSecurityContext();
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

        RemoveDefendantAccountEnforcementHoldRequest request =
            RemoveDefendantAccountEnforcementHoldRequest.builder()
                .reason("remove hold reason")
                .build();

        RemoveDefendantAccountEnforcementHoldResponse proxyResponse =
            RemoveDefendantAccountEnforcementHoldResponse.builder()
                .defendantAccountId("77")
                .version(BigInteger.valueOf(7))
                .build();

        UserStateV2 userState = mock(UserStateV2.class);

        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
        when(userState.hasBusinessUnitUserWithPermission((short) 10, FinesPermission.ENTER_ENFORCEMENT))
            .thenReturn(true);
        when(userState.getBusinessUnitUserForBusinessUnit((short) 10))
            .thenReturn(Optional.empty());
        when(userState.getUsername()).thenReturn("user-1");

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

        verify(userStateService).getUserStateFromSecurityContext();
        verify(userState).hasBusinessUnitUserWithPermission((short) 10, FinesPermission.ENTER_ENFORCEMENT);
        verify(userState).getBusinessUnitUserForBusinessUnit((short) 10);
        verify(userState).getUsername();
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

        RemoveDefendantAccountEnforcementHoldRequest request =
            RemoveDefendantAccountEnforcementHoldRequest.builder()
                .reason("remove hold reason")
                .build();

        RemoveDefendantAccountEnforcementHoldResponse proxyResponse =
            RemoveDefendantAccountEnforcementHoldResponse.builder()
                .defendantAccountId("77")
                .version(BigInteger.valueOf(7))
                .build();

        UserStateV2 userState = allPermissionsUser();

        String businessUnitUserId = userState.getBusinessUnitUserForBusinessUnit(businessUnitId)
            .map(BusinessUnitUserV2::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElse(userState.getUsername());

        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);

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
