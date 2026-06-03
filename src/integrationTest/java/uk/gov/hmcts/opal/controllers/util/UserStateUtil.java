package uk.gov.hmcts.opal.controllers.util;

import static java.util.Collections.emptySet;
import static uk.gov.hmcts.opal.common.user.authorisation.model.Domain.FINES;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationToken;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.common.user.authorisation.model.Permission;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;

public class UserStateUtil {

    public static final UserState noFinesPermissionUser() {
        return UserState.builder()
            .userId(999L)
            .userName("no-permissions@users.com")
            .name("No Permissions User")
            .businessUnitUser(emptySet())
            .build();
    }

    public static UserState allFinesPermissionUser() {
        return new UserState.DeveloperUserState();
    }

    public static UserState allPermissionsUser() {
        return allFinesPermissionUser();
    }

    public static UserState noPermissionsUser() {
        return noFinesPermissionUser();
    }

    public static UserState permissionUser(Short buid, FinesPermission... permissions) {
        return UserState.builder()
            .userId(1L)
            .userName("normal@users.com")
            .name("Normal User")
            .businessUnitUser(Set.of(permissions(buid, permissionsFor(permissions))))
            .build();
    }

    public static UserState permissionUser(Short[] buids, FinesPermission... permissions) {
        return UserState.builder()
            .userId(1L)
            .userName("normal@users.com")
            .name("Normal User")
            .businessUnitUser(
                Arrays
                    .stream(buids)
                    .map(buid -> permissions(buid, permissionsFor(permissions)))
                    .collect(Collectors.toSet()))
            .build();
    }

    public static UserState permissionUser(Short buid, Permission... permissions) {
        return UserState.builder()
            .userId(1L)
            .userName("normal@users.com")
            .name("Normal User")
            .businessUnitUser(Set.of(permissions(buid, permissions)))
            .build();
    }

    public static UserState permissionUser(Set<BusinessUnitUser> permissions) {
        return UserState.builder()
            .userId(1L)
            .userName("normal@users.com")
            .name("Normal User")
            .businessUnitUser(permissions)
            .build();
    }

    public static BusinessUnitUser permissions(Short buid, Permission... permissions) {
        return permissions(buid, new HashSet<>(Arrays.asList(permissions)));
    }

    public static BusinessUnitUser permissions(Short buid, Set<Permission> permissions) {
        return BusinessUnitUser.builder()
            .businessUnitUserId("USER01")
            .businessUnitId(buid)
            .permissions(permissions)
            .build();
    }

    public static Set<Permission> permissionsFor(FinesPermission... permissions) {
        return Arrays.stream(permissions)
            .map(UserStateUtil::permissionFor)
            .collect(Collectors.toSet());
    }

    public static Permission permissionFor(FinesPermission permission) {
        return Permission.builder()
            .permissionId(permission.getId())
            .permissionName(permission.getDescription())
            .build();
    }

    public static OpalJwtAuthenticationToken allFinesPermissionsToken() {

        Map<Domain, DomainBusinessUnitUsers> domainsMap = new HashMap<>();
        BusinessUnitUser businessUnitUser = BusinessUnitUser.builder()
            .businessUnitId((short)78)
            .businessUnitUserId("s")
            .permissions(permissionsFor(FinesPermission.values()))
            .build();
        DomainBusinessUnitUsers domainBusinessUnitUsers = DomainBusinessUnitUsers.builder()
            .businessUnitUsers(List.of(businessUnitUser)).build();
        domainsMap.put(FINES, domainBusinessUnitUsers);

        UserStateV2 userState = getUserStateV2(domainsMap);

        return new OpalJwtAuthenticationToken(userState, FINES, getJwt(), emptySet(), null);
    }

    public static OpalJwtAuthenticationToken noFinesPermissionsToken() {

        Map<Domain, DomainBusinessUnitUsers> domainsMap = new HashMap<>();

        DomainBusinessUnitUsers domainBusinessUnitUsers = DomainBusinessUnitUsers.builder()
            .businessUnitUsers(Collections.emptyList()).build();
        domainsMap.put(FINES, domainBusinessUnitUsers);

        UserStateV2 userState = getUserStateV2(domainsMap);

        return new OpalJwtAuthenticationToken(userState, FINES, getJwt(), emptySet(), null);
    }

    public static OpalJwtAuthenticationToken permissionsToken(Short buid, FinesPermission... permissions) {
        Map<Domain, DomainBusinessUnitUsers> domainsMap = new HashMap<>();
        DomainBusinessUnitUsers domainBusinessUnitUsers = DomainBusinessUnitUsers.builder()
            .businessUnitUsers(List.of(permissions(buid, permissionsFor(permissions)))).build();
        domainsMap.put(FINES, domainBusinessUnitUsers);

        UserStateV2 userState = getUserStateV2(domainsMap);

        return new OpalJwtAuthenticationToken(userState, FINES, getJwt(), emptySet(), null);
    }

    private static Jwt getJwt() {
        Instant now = Instant.now();
        return Jwt.withTokenValue("dummy-token")
            .header("alg", "none")
            .header("typ", "JWT")
            .claim("sub", "opal-test@hmcts.net")
            .claim("iss", "https://issuer.example")
            .claim("scope", "read write")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(3600))
            .build();
    }

    private static UserStateV2 getUserStateV2(Map<Domain, DomainBusinessUnitUsers> domainsMap) {
        return UserStateV2.builder()
            .username("username111")
            .userId(123L)
            .name("name2222")
            .domains(domainsMap)
            .build();
    }
}
