package uk.gov.hmcts.opal.service;

import static uk.gov.hmcts.opal.util.HttpUtil.extractPreferredUsername;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationToken;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUserV2;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.UserStateService")
public class UserStateService {

    private final AccessTokenService tokenService;

    private final UserStateClientService userStateClientService;

    public UserStateV2 checkForAuthorisedUser() {
        return userStateClientService.getUserStateByAuthenticatedUser()
            .map(userState -> {
                Map<Domain, DomainBusinessUnitUsers> domainBusinessUnitUsers = userState.getDomains();
                if (domainBusinessUnitUsers != null) {
                    userState.setDomains(new LinkedHashMap<>(domainBusinessUnitUsers));
                }
                return userState;
            })
            .orElseGet(this::getUserStateFromSecurityContext);
    }

    public UserStateV2 checkForAuthorisedUser(String authorization) {
        return checkForAuthorisedUser();
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

    public List<Short> getPermittedBusinessUnitIds(List<Short> requestedBusinessUnitIds, FinesPermission permission) {
        return getUserStateFromSecurityContext()
            .getDomainBusinessUnitUsers(Domain.FINES)
            .filterBusinessUnitsByBusinessUnitUsersWithAnyPermissions(
                Optional.ofNullable(requestedBusinessUnitIds), permission)
            .stream()
            .toList();
    }

    public List<Short> getBusinessUnitIdsFor(FinesPermission permission) {
        return Optional.ofNullable(getUserStateFromSecurityContext()
            .getDomainBusinessUnitUsers(Domain.FINES)
            .getBusinessUnitUsers())
            .stream()
            .flatMap(List::stream)
            .filter(businessUnitUser -> businessUnitUser.hasPermission(permission))
            .map(BusinessUnitUserV2::getBusinessUnitId)
            .distinct()
            .toList();
    }

    public String getPreferredUsername(String authorization) {
        return extractPreferredUsername(authorization, tokenService);
    }

}
