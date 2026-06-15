package uk.gov.hmcts.opal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationToken;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.client.mapper.UserStateMapper;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;

import static uk.gov.hmcts.opal.util.HttpUtil.extractPreferredUsername;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.UserStateService")
public class UserStateService {

    private final AccessTokenService tokenService;

    private final UserStateMapper userStateMapper;

    /**
0     * Returns the V1 user state from the current security context.
     *
     * @deprecated Use {@link #getUserStateFromSecurityContext()} for V2 user state.
     */
    // Stop gap solution until all permissions are resolved directly in service-layer auth checks.
    @SuppressWarnings({"java:S1874", "java:S1133"})
    @Deprecated(since = "2")
    public UserState checkForAuthorisedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OpalJwtAuthenticationToken authToken)) {
            throw new AccessDeniedException("Unexpected token type");
        }
        UserStateV2 userStateV2 = authToken.getUserState();
        if (userStateV2 == null) {
            throw new AccessDeniedException("User state not found in token");
        }
        return userStateMapper.toUserState(userStateV2, Domain.FINES);
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
}
