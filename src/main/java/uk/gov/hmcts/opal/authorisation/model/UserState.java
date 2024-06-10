package uk.gov.hmcts.opal.authorisation.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import uk.gov.hmcts.opal.authorisation.model.Role.DeveloperRole;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
@EqualsAndHashCode
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ToString
public class UserState {

    @NonNull
    Long userId;

    @NonNull
    String userName;

    @EqualsAndHashCode.Exclude
    Set<Role> roles;

    public boolean anyRoleHasPermission(Permissions permission) {
        return roles.stream().anyMatch(r -> r.hasPermission(permission));
    }

    public boolean noRoleHasPermission(Permissions permission) {
        return !anyRoleHasPermission(permission);
    }

    public UserRoles allRolesWithPermission(Permissions permission) {
        return new UserRolesImpl(
            roles.stream().filter(r -> r.hasPermission(permission)).collect(Collectors.toSet()));
    }

    public boolean hasRoleWithPermission(short roleBusinessUnitId, Permissions permission) {
        return roles.stream()
            .filter(r -> r.matchesBusinessUnitId(roleBusinessUnitId))
            .findAny()  // Should be either zero or one roles that match the business unit id
            .stream()
            .anyMatch(r -> r.hasPermission(permission));
    }

    public Optional<Role> getRoleForBusinessUnit(Short businessUnitId) {
        return roles.stream()
            .filter(r -> r.matchesBusinessUnitId(businessUnitId))
            .findFirst();
    }

    public static interface UserRoles {
        boolean containsBusinessUnit(Short businessUnitId);
    }

    public static class UserRolesImpl implements UserRoles {
        private final Set<Role> roles;
        private final Set<Short> businessUnits;

        public UserRolesImpl(Set<Role> roles) {
            this.roles = roles;
            businessUnits = roles.stream().map(r -> r.getBusinessUnitId()).collect(Collectors.toSet());
        }

        public boolean containsBusinessUnit(Short businessUnitId) {
            return businessUnits.contains(businessUnitId);
        }
    }

    public static class DeveloperUserState extends UserState {
        private static final Optional<Role> DEV_ROLE = Optional.of(new DeveloperRole());

        public DeveloperUserState() {
            super(0L, "Developer_User", Collections.emptySet());
        }

        @Override
        public boolean anyRoleHasPermission(Permissions permission) {
            return true;
        }

        @Override
        public Optional<Role> getRoleForBusinessUnit(Short businessUnitId) {
            return DEV_ROLE;
        }

        @Override
        public UserRoles allRolesWithPermission(Permissions permission) {
            return new UserRoles() {
                @Override
                public boolean containsBusinessUnit(Short businessUnitId) {
                    return true;
                }
            };
        }
    }
}
