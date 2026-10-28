package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationToken;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.client.mapper.UserStateMapper;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUserV2;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;

@ExtendWith(MockitoExtension.class)
class UserStateServiceTest {

    @Mock
    private AccessTokenService tokenService;

    @Mock
    private UserStateMapper userStateMapper;

    @Mock
    private UserStateClientService userStateClientService;

    @InjectMocks
    private UserStateService userStateService;

    @Test
    void checkForAuthorisedUser_returnsFinesSpecificV2State() {
        UserStateV2 userState = mock(UserStateV2.class);
        UserStateV2 finesUserState = mock(UserStateV2.class);
        when(userStateClientService.getUserStateByAuthenticatedUser()).thenReturn(Optional.of(userState));
        when(userStateMapper.toUserStateSpecific(userState, Domain.FINES)).thenReturn(finesUserState);

        assertSame(finesUserState, userStateService.checkForAuthorisedUser());
    }

    @Test
    void getUserStateFromSecurityContext_returnsTokenUserState() {
        OpalJwtAuthenticationToken authToken = mock(OpalJwtAuthenticationToken.class);
        UserStateV2 userState = mock(UserStateV2.class);
        setAuthentication(authToken);
        when(authToken.getUserState()).thenReturn(userState);

        assertSame(userState, userStateService.getUserStateFromSecurityContext());
    }

    @Test
    void getUserStateFromSecurityContext_rejectsUnexpectedTokenType() {
        setAuthentication(mock(Authentication.class));

        AccessDeniedException exception = assertThrows(
            AccessDeniedException.class, () -> userStateService.getUserStateFromSecurityContext());

        assertEquals("Unexpected token type", exception.getMessage());
        verifyNoInteractions(tokenService);
    }

    @Test
    void getUserStateFromSecurityContext_rejectsMissingUserState() {
        OpalJwtAuthenticationToken authToken = mock(OpalJwtAuthenticationToken.class);
        setAuthentication(authToken);
        when(authToken.getUserState()).thenReturn(null);

        AccessDeniedException exception = assertThrows(
            AccessDeniedException.class, () -> userStateService.getUserStateFromSecurityContext());

        assertEquals("User state not found in token", exception.getMessage());
    }

    @Test
    void getPermittedBusinessUnitIds_filtersUsingPermissionCode() {
        setUserState(userState(
            businessUnitUser((short) 10, FinesPermission.ACCOUNT_ENQUIRY),
            businessUnitUser((short) 20, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)));

        List<Short> permittedIds = userStateService.getPermittedBusinessUnitIds(
            List.of((short) 10, (short) 20), FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);

        assertEquals(List.of((short) 20), permittedIds);
    }

    @Test
    void getBusinessUnitIdsFor_returnsDistinctPermittedUnits() {
        setUserState(userState(
            businessUnitUser((short) 10, FinesPermission.ACCOUNT_ENQUIRY),
            businessUnitUser((short) 20, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS),
            businessUnitUser((short) 30, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)));

        List<Short> businessUnitIds = userStateService.getBusinessUnitIdsFor(
            FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);

        assertEquals(List.of((short) 20, (short) 30), businessUnitIds);
    }

    @Test
    void getPreferredUsername_delegatesToTokenService() {
        when(tokenService.extractPreferredUsername(any())).thenReturn("HMCTS User");

        assertEquals("HMCTS User", userStateService.getPreferredUsername(""));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void setUserState(UserStateV2 userState) {
        OpalJwtAuthenticationToken authToken = mock(OpalJwtAuthenticationToken.class);
        setAuthentication(authToken);
        when(authToken.getUserState()).thenReturn(userState);
    }

    private void setAuthentication(Authentication authentication) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private UserStateV2 userState(BusinessUnitUserV2... businessUnitUsers) {
        DomainBusinessUnitUsers finesUsers = DomainBusinessUnitUsers.builder()
            .businessUnitUsers(List.of(businessUnitUsers))
            .build();
        return UserStateV2.builder().userId(506L).username("username")
            .domains(Map.of(Domain.FINES, finesUsers)).build();
    }

    private BusinessUnitUserV2 businessUnitUser(Short businessUnitId, FinesPermission permission) {
        return BusinessUnitUserV2.builder()
            .businessUnitId(businessUnitId)
            .permissions(Set.of(permission.toCommonPermission()))
            .build();
    }
}
