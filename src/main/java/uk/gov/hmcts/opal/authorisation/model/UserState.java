package uk.gov.hmcts.opal.authorisation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUserPermissions.DeveloperBusinessUnitUserPermissions;

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
    Set<BusinessUnitUserPermissions> businessUnitUserPermissions;

    @JsonCreator
    public UserState(
        @JsonProperty("user_id") Long userId,
        @JsonProperty("user_name") String userName,
        @JsonProperty("business_unit_user_permissions") Set<BusinessUnitUserPermissions> businessUnitUserPermissions
    ) {
        this.userId = userId;
        this.userName = userName;
        this.businessUnitUserPermissions = businessUnitUserPermissions;
    }

    public boolean anyRoleHasPermission(Permissions permission) {
        return businessUnitUserPermissions.stream().anyMatch(r -> r.hasPermission(permission));
    }

    public boolean noRoleHasPermission(Permissions permission) {
        return !anyRoleHasPermission(permission);
    }

    public UserBusinessUnits allBusinessUnitUsersWithPermission(Permissions permission) {
        return new UserBusinessUnitsImpl(
            businessUnitUserPermissions.stream().filter(r -> r.hasPermission(permission)).collect(Collectors.toSet()));
    }

    public boolean hasRoleWithPermission(short roleBusinessUnitId, Permissions permission) {
        return businessUnitUserPermissions.stream()
            .filter(r -> r.matchesBusinessUnitId(roleBusinessUnitId))
            .findAny()  // Should be either zero or one businessUnitUserPermissions that match the business unit id
            .stream()
            .anyMatch(r -> r.hasPermission(permission));
    }

    public Optional<BusinessUnitUserPermissions> getRoleForBusinessUnit(Short businessUnitId) {
        return businessUnitUserPermissions.stream()
            .filter(r -> r.matchesBusinessUnitId(businessUnitId))
            .findFirst();
    }

    public static interface UserBusinessUnits {
        boolean containsBusinessUnit(Short businessUnitId);
    }

    public static class UserBusinessUnitsImpl implements UserBusinessUnits {
        private final Set<BusinessUnitUserPermissions> businessUnitUserPermissions;
        private final Set<Short> businessUnits;

        public UserBusinessUnitsImpl(Set<BusinessUnitUserPermissions> businessUnitUserPermissions) {
            this.businessUnitUserPermissions = businessUnitUserPermissions;
            businessUnits = businessUnitUserPermissions.stream().map(r -> r.getBusinessUnitId())
                .collect(Collectors.toSet());
        }

        public boolean containsBusinessUnit(Short businessUnitId) {
            return businessUnits.contains(businessUnitId);
        }
    }

    public static class DeveloperUserState extends UserState {
        private static final Optional<BusinessUnitUserPermissions> DEV_ROLE =
            Optional.of(new DeveloperBusinessUnitUserPermissions());

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
        public UserBusinessUnits allBusinessUnitUsersWithPermission(Permissions permission) {
            return new UserBusinessUnits() {
                @Override
                public boolean containsBusinessUnit(Short businessUnitId) {
                    return true;
                }
            };
        }
    }
}
