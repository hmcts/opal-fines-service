package uk.gov.hmcts.opal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.config.properties.BeDeveloperConfiguration;

import static uk.gov.hmcts.opal.util.HttpUtil.extractPreferredUsername;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.UserStateService")
public class UserStateService {

    protected static final String DEVELOPER_PERMISSIONS = "Dev-Role-Permissions";

    private final AccessTokenService tokenService;

    private final UserStateClientService userStateClientService;

    private final BeDeveloperConfiguration developerConfiguration;

    // TODO: authorization String no longer required.
    public UserState checkForAuthorisedUser(String authorization) {

        return userStateClientService.getUserStateByAuthenticatedUser(userStateClientService)
            .orElseGet(() -> getDeveloperUserStateOrError((getPreferredUsername(authorization))));
    }

    public String getPreferredUsername(String authorization) {
        return extractPreferredUsername(authorization, tokenService);
    }

    private UserState getDeveloperUserStateOrError(String username) {
        if (DEVELOPER_PERMISSIONS.equals(developerConfiguration.getUserRolePermissions())) {
            return new UserState.DeveloperUserState();
        } else {
            throw new AccessDeniedException("No authorised user with username '" + username + "' found");
        }
    }

}
