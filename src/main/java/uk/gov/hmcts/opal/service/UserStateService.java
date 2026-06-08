package uk.gov.hmcts.opal.service;

import static uk.gov.hmcts.opal.util.HttpUtil.extractPreferredUsername;

import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationToken;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.client.mapper.UserStateMapper;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.UserStateService")
public class UserStateService {

    private final AccessTokenService tokenService;

    private final UserStateMapper userStateMapper;

    private final UserStateClientService userStateClientService;

    /**
     * Returns the authorised user state from the current authenticated user service context.
     *
     * @deprecated Use {@link #getUserStateFromSecurityContext()} for V2 user state.
     */
    @Deprecated(since = "2")
    @SuppressWarnings("java:S1133")
    public UserState checkForAuthorisedUser() {
        Optional<?> authenticatedUserState = userStateClientService.getUserStateByAuthenticatedUser();

        return authenticatedUserState
            .filter(UserStateV2.class::isInstance)
            .map(UserStateV2.class::cast)
            .map(this::mapAuthenticatedUserState)
            .orElseGet(this::getUserStateV1FromSecurityContext);
    }

    /**
     * Returns the authorised user state from the current authenticated user service context.
     *
     * @deprecated Use {@link #getUserStateFromSecurityContext()} for V2 user state.
     */
    @Deprecated(since = "2")
    @SuppressWarnings("java:S1133")
    public UserState checkForAuthorisedUser(String authorization) {
        return checkForAuthorisedUser();
    }

    /**
     * Returns the V1 user state from the current security context.
     *
     * @deprecated Use {@link #getUserStateFromSecurityContext()} for V2 user state.
     */
    // Stop gap solution until all permissions are resolved directly in service-layer auth checks.
    @SuppressWarnings({"java:S1874", "java:S1133"})
    @Deprecated(since = "2")
    public UserState getUserStateV1FromSecurityContext() {
        UserState userState = userStateMapper.toUserState(getUserStateFromSecurityContext(), Domain.FINES);
        log.debug(":getUserStateV1FromSecurityContext: using authenticated user state from token: userId={}, "
                + "userName={}, businessUnits={}",
            userState.getUserId(), userState.getUserName(), summariseBusinessUnits(userState));
        return userState;
    }

    public UserStateV2 getUserStateFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OpalJwtAuthenticationToken authToken)) {
            throw new AccessDeniedException("Unexpected token type");
        }
        UserStateV2 userState = authToken.getUserState();
        if (userState == null) {
            throw new AccessDeniedException("User state not found in token");
        }
        return userState;
    }

    public String getPreferredUsername(String authorization) {
        return extractPreferredUsername(authorization, tokenService);
    }

    private UserState mapAuthenticatedUserState(UserStateV2 userStateV2) {
        UserState userState = userStateMapper.toUserState(userStateV2, Domain.FINES);
        log.debug(":checkForAuthorisedUser: using authenticated user state from user service: userId={}, "
                + "userName={}, businessUnits={}",
            userState.getUserId(), userState.getUserName(), summariseBusinessUnits(userState));
        return userState;
    }

    private String summariseBusinessUnits(UserState userState) {
        if (userState.getBusinessUnitUser() == null || userState.getBusinessUnitUser().isEmpty()) {
            return "[]";
        }
        return userState.getBusinessUnitUser().stream()
            .map(businessUnitUser -> String.valueOf(businessUnitUser.getBusinessUnitId()))
            .collect(Collectors.joining(",", "[", "]"));
    }
}
