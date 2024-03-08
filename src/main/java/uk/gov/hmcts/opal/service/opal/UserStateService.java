package uk.gov.hmcts.opal.service.opal;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authorisation.model.UserState;

import static uk.gov.hmcts.opal.util.HttpUtil.extractPreferredUsername;

@Service
@RequiredArgsConstructor
public class UserStateService {

    private final AccessTokenService tokenService;

    private final UserService userService;

    private final UserEntitlementService userEntitlementService;

    public UserState getUserStateUsingServletRequest(HttpServletRequest request) {
        return getUserStateByUsername(getPreferredUsername(request));
    }


    public String getPreferredUsername(HttpServletRequest request) {
        return extractPreferredUsername(request, tokenService);
    }

    public UserState getUserStateByUsername(String username) {
        return userEntitlementService.getUserStateByUsername(username)
            .orElse(userService.getLimitedUserStateByUsername(username));
    }
}
