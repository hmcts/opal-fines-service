package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import java.math.BigInteger;
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
    void addEnforcement_whenUserHasPermission_callsProxyAndReturnsResult() {
        // arrange
        Long defendantAccountId = 77L;
        String businessUnitId = "10";
        String ifMatch = "\"3\"";
        String authHeader = "Bearer abc";
        AddDefendantAccountEnforcementRequest req = mock(AddDefendantAccountEnforcementRequest.class);

        AddEnforcementResponse proxyResponse = AddEnforcementResponse.builder()
            .enforcementId("ENF123")
            .defendantAccountId("77")
            .version(3)
            .build();

        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.ENTER_ENFORCEMENT)).thenReturn(true);

        // business unit user lookup returns an Optional<BusinessUnitUser> with a non-blank ID
        BusinessUnitUser buUser = mock(BusinessUnitUser.class);
        when(buUser.getBusinessUnitUserId()).thenReturn("BU-USER-1");
        when(userState.getBusinessUnitUserForBusinessUnit((short)10))
            .thenReturn(java.util.Optional.of(buUser));

        when(defendantAccountEnforcementServiceProxy.addEnforcement(
            defendantAccountId, businessUnitId, "BU-USER-1", ifMatch, authHeader, req))
            .thenReturn(proxyResponse);

        // act
        AddEnforcementResponse result =
            defendantAccountEnforcementService
                .addEnforcement(defendantAccountId, businessUnitId, ifMatch, authHeader, req);

        // assert
        assertSame(proxyResponse, result, "Should return exactly the proxy response");

        // verify interactions
        verify(userStateService).checkForAuthorisedUser(authHeader);
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.ENTER_ENFORCEMENT);
        verify(userState).getBusinessUnitUserForBusinessUnit((short)10);
        verify(defendantAccountEnforcementServiceProxy)
            .addEnforcement(defendantAccountId, businessUnitId, "BU-USER-1", ifMatch, authHeader, req);
        verifyNoMoreInteractions(userStateService, userState, defendantAccountEnforcementServiceProxy);
    }

    @Test
    void addEnforcement_whenUserLacksPermission_throwsPermissionNotAllowedException() {
        // arrange
        Long defendantAccountId = 77L;
        String businessUnitId = "10";
        String authHeader = "Bearer abc";

        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.ENTER_ENFORCEMENT))
            .thenReturn(false);

        // act + assert
        PermissionNotAllowedException ex = assertThrows(
            PermissionNotAllowedException.class,
            () -> defendantAccountEnforcementService
                .addEnforcement(defendantAccountId, businessUnitId, "\"3\"", authHeader, null)
        );

        assertTrue(
            ex.getMessage() == null || ex.getMessage().contains(FinesPermission.ENTER_ENFORCEMENT.name()),
            "Exception should mention ENTER_ENFORCEMENT"
        );

        verify(userStateService).checkForAuthorisedUser(authHeader);
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.ENTER_ENFORCEMENT);
        verifyNoInteractions(defendantAccountEnforcementServiceProxy);
    }

    @Test
    void addEnforcement_whenBusinessUnitUserIdBlank_usesNullInProxyCall() {
        // arrange
        Long defendantAccountId = 77L;
        String businessUnitId = "10";
        String authHeader = "Bearer abc";

        AddDefendantAccountEnforcementRequest req = mock(AddDefendantAccountEnforcementRequest.class);

        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
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
            eq("\"3\""),
            eq(authHeader),
            eq(req)
        )).thenReturn(proxyResult);

        // act
        AddEnforcementResponse out =
            defendantAccountEnforcementService
                .addEnforcement(defendantAccountId, businessUnitId, "\"3\"", authHeader, req);

        // assert
        assertNotNull(out);
        verify(defendantAccountEnforcementServiceProxy).addEnforcement(
            eq(defendantAccountId),
            eq(businessUnitId),
            isNull(),                   // verifies null is passed
            eq("\"3\""),
            eq(authHeader),
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
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());
        when(defendantAccountEnforcementServiceProxy.getEnforcementStatus(anyLong())).thenReturn(status);

        // Act
        EnforcementStatus response = defendantAccountEnforcementService
            .getEnforcementStatus(33L, "Bearer a_bearer_token");

        // Assert
        assertNotNull(response);
        assertTrue(response.getEmployerFlag());
        assertTrue(response.getIsHmrcCheckEligible());
        assertEquals(new BigInteger("1234567890123345678901234567890"), response.getVersion());

    }
}
