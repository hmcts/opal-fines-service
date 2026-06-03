package uk.gov.hmcts.opal.service;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationToken;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
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

    private final UserStateClientService userStateClientService;

    private final UserStateMapper userStateMapper;

    public UserState checkForAuthorisedUser() {
        return userStateClientService.getUserStateV1ByAuthenticatedUser()
            .map(userState -> {
                log.debug(":checkForAuthorisedUser: using authenticated user state from user service: userId={}, "
                        + "userName={}, businessUnits={}",
                    userState.getUserId(), userState.getUserName(), summariseBusinessUnits(userState));
                return userState;
            })
            .orElseGet(this::getUserStateFromSecurityContext);
    }

    public UserState checkForAuthorisedUser(String authorization) {
        return checkForAuthorisedUser();
    }

    // Stop gap solution until all permissions are resolved directly in service-layer auth checks.
    private UserState getUserStateFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OpalJwtAuthenticationToken authToken)) {
            throw new AccessDeniedException("Unexpected token type");
        }
        UserStateV2 userStateV2 = authToken.getUserState();
        if (userStateV2 == null) {
            throw new AccessDeniedException("User state not found in token");
        }
        UserState userState = userStateMapper.toUserState(userStateV2, Domain.FINES);
        log.debug(":checkForAuthorisedUser: using user state from security context token: userId={}, userName={}, "
                + "businessUnits={}",
            userState.getUserId(), userState.getUserName(), summariseBusinessUnits(userState));
        return userState;
    }

    public String getPreferredUsername(String authorization) {
        return extractPreferredUsername(authorization, tokenService);
    }

    private String summariseBusinessUnits(UserState userState) {
        if (userState.getBusinessUnitUser() == null || userState.getBusinessUnitUser().isEmpty()) {
            return "[]";
        }

        return userState.getBusinessUnitUser().stream()
            .map(businessUnitUser -> String.valueOf(businessUnitUser.getBusinessUnitId()))
            .sorted()
            .collect(Collectors.joining(",", "[", "]"));
    }
}
