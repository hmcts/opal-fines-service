package uk.gov.hmcts.opal.controllers.shared.util;

import static java.util.Collections.emptySet;
import static uk.gov.hmcts.opal.common.user.authorisation.model.Domain.FINES;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationToken;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUserV2;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.common.user.authorisation.model.PermissionV2;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UserStateUtil {

    public static UserStateV2 noFinesPermissionUser() {
        return UserStateV2.builder()
            .userId(999L)
            .username("no-permissions@users.com")
            .name("No Permissions User")
            .domains(Map.of(FINES, domainBusinessUnitUsers(List.of())))
            .build();
    }

    public static UserStateV2 allFinesPermissionUser() {
        return new UserStateV2.DeveloperUserState();
    }

    public static UserStateV2 allPermissionsUser() {
        return allFinesPermissionUser();
    }

    public static UserStateV2 allFinesPermissionUserStateV2() {
        return allFinesPermissionsToken().getUserState();
    }

    public static UserStateV2 noPermissionsUser() {
        return noFinesPermissionUser();
    }

    public static UserStateV2 noFinesPermissionUserStateV2() {
        return noFinesPermissionsToken().getUserState();
    }

    public static UserStateV2 permissionUser(Short buid, FinesPermission... permissions) {
        return UserStateV2.builder()
            .userId(1L)
            .username("normal@users.com")
            .name("Normal User")
            .domains(Map.of(FINES,
                domainBusinessUnitUsers(List.of(permissions(buid, permissionsFor(permissions))))))
            .build();
    }

    public static UserStateV2 permissionUser(Short[] buids, FinesPermission... permissions) {
        return UserStateV2.builder()
            .userId(1L)
            .username("normal@users.com")
            .name("Normal User")
            .domains(Map.of(FINES, domainBusinessUnitUsers(
                Arrays
                    .stream(buids)
                    .map(buid -> permissions(buid, permissionsFor(permissions)))
                    .toList())))
            .build();
    }

    public static UserStateV2 permissionUser(Short buid, PermissionV2... permissions) {
        return UserStateV2.builder()
            .userId(1L)
            .username("normal@users.com")
            .name("Normal User")
            .domains(Map.of(FINES, domainBusinessUnitUsers(List.of(permissions(buid, permissions)))))
            .build();
    }

    public static UserStateV2 permissionUser(Set<BusinessUnitUserV2> permissions) {
        return UserStateV2.builder()
            .userId(1L)
            .username("normal@users.com")
            .name("Normal User")
            .domains(Map.of(FINES, domainBusinessUnitUsers(List.copyOf(permissions))))
            .build();
    }

    public static UserStateV2 permissionUserStateV2(Short buid, FinesPermission... permissions) {
        return permissionsToken(buid, permissions).getUserState();
    }

    public static BusinessUnitUserV2 permissions(Short buid, PermissionV2... permissions) {
        return permissions(buid, new HashSet<>(Arrays.asList(permissions)));
    }

    public static BusinessUnitUserV2 permissions(Short buid, Set<PermissionV2> permissions) {
        return BusinessUnitUserV2.builder()
            .businessUnitUserId("USER01")
            .businessUnitId(buid)
            .permissions(permissions)
            .build();
    }

    public static Set<PermissionV2> permissionsFor(FinesPermission... permissions) {
        return Arrays.stream(permissions)
            .map(UserStateUtil::permissionFor)
            .collect(Collectors.toSet());
    }

    public static PermissionV2 permissionFor(FinesPermission permission) {
        return permission.toCommonPermission();
    }

    public static OpalJwtAuthenticationToken allFinesPermissionsToken() {

        Map<Domain, DomainBusinessUnitUsers> domainsMap = new HashMap<>();
        BusinessUnitUserV2 businessUnitUser = BusinessUnitUserV2.builder()
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

    private static DomainBusinessUnitUsers domainBusinessUnitUsers(List<BusinessUnitUserV2> businessUnitUsers) {
        return DomainBusinessUnitUsers.builder().businessUnitUsers(businessUnitUsers).build();
    }
}
