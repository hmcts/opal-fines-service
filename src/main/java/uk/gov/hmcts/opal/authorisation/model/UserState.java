package uk.gov.hmcts.opal.authorisation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUserPermissions.DeveloperRole;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
@Data
public class UserState {

    @NonNull
    Long userId;

    @NonNull
    String userName;

    @EqualsAndHashCode.Exclude
    Set<BusinessUnitUserPermissions> roles;

    @JsonCreator
    public UserState(
        @JsonProperty("user_id") Long userId,
        @JsonProperty("user_name") String userName,
        @JsonProperty("roles") Set<BusinessUnitUserPermissions> roles
    ) {
        this.userId = userId;
        this.userName = userName;
        this.roles = roles;
    }

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

    public Optional<BusinessUnitUserPermissions> getRoleForBusinessUnit(Short businessUnitId) {
        return roles.stream()
            .filter(r -> r.matchesBusinessUnitId(businessUnitId))
            .findFirst();
    }

    public static interface UserRoles {
        boolean containsBusinessUnit(Short businessUnitId);
    }

    public static class UserRolesImpl implements UserRoles {
        private final Set<BusinessUnitUserPermissions> roles;
        private final Set<Short> businessUnits;

        public UserRolesImpl(Set<BusinessUnitUserPermissions> roles) {
            this.roles = roles;
            businessUnits = roles.stream().map(r -> r.getBusinessUnitId()).collect(Collectors.toSet());
        }

        public boolean containsBusinessUnit(Short businessUnitId) {
            return businessUnits.contains(businessUnitId);
        }
    }

    public static class DeveloperUserState extends UserState {
        private static final Optional<BusinessUnitUserPermissions> DEV_ROLE = Optional.of(new DeveloperRole());

        public DeveloperUserState() {
            super(0L, "Developer_User", Collections.emptySet());
        }

        @Override
        public boolean anyRoleHasPermission(Permissions permission) {
            return true;
        }

        @Override
        public Optional<BusinessUnitUserPermissions> getRoleForBusinessUnit(Short businessUnitId) {
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
