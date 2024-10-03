package uk.gov.hmcts.opal.authorisation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUserPermissions.DeveloperBusinessUnitUserPermissions;

import java.util.Collections;
import java.util.List;
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

    public boolean anyBusinessUnitUserHasPermission(Permissions permission) {
        return businessUnitUserPermissions.stream().anyMatch(r -> r.hasPermission(permission));
    }

    public boolean noBusinessUnitUserHasPermission(Permissions permission) {
        return !anyBusinessUnitUserHasPermission(permission);
    }

    public boolean anyBusinessUnitUserHasAnyPermission(Permissions... permission) {
        return businessUnitUserPermissions.stream().anyMatch(r -> r.hasAnyPermission(permission));
    }

    public UserBusinessUnits allBusinessUnitUsersWithPermission(Permissions permission) {
        return new UserBusinessUnitsImpl(
            businessUnitUserPermissions.stream().filter(r -> r.hasPermission(permission)).collect(Collectors.toSet()));
    }

    public boolean hasBusinessUnitUserWithPermission(short businessUnitId, Permissions permission) {
        return businessUnitUserPermissions.stream()
            .filter(r -> r.matchesBusinessUnitId(businessUnitId))
            .findAny()  // Should be either zero or one businessUnitUserPermissions that match the business unit id
            .stream()
            .anyMatch(r -> r.hasPermission(permission));
    }

    public boolean hasBusinessUnitUserWithAnyPermission(short businessUnitId, Permissions... permissions) {
        return businessUnitUserPermissions.stream()
            .filter(r -> r.matchesBusinessUnitId(businessUnitId))
            .findAny()  // Should be either zero or one businessUnitUserPermissions that match the business unit id
            .stream()
            .anyMatch(r -> r.hasAnyPermission(permissions));
    }

    public Set<Short> filterBusinessUnitsByBusinessUnitUsersWithAnyPermissions(
        Optional<List<Short>> businessUnitIds, Permissions... permissions) {

        return filterBusinessUnitsByBusinessUnitUsersWithAnyPermissions(
            businessUnitIds.orElse(Collections.emptyList()), permissions);
    }

    public Set<Short> filterBusinessUnitsByBusinessUnitUsersWithAnyPermissions(
        List<Short> businessUnitIds, Permissions... permissions) {

        return businessUnitIds.stream()
            .filter(buid -> hasBusinessUnitUserWithAnyPermission(buid, permissions))
            .collect(Collectors.toSet());
    }

    public Optional<BusinessUnitUserPermissions> getBusinessUnitUserForBusinessUnit(Short businessUnitId) {
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
        private static final Optional<BusinessUnitUserPermissions> DEV_BUSINESS_UNIT_USER_PERMISSIONS =
            Optional.of(new DeveloperBusinessUnitUserPermissions());

        public DeveloperUserState() {
            super(0L, "Developer_User", Collections.emptySet());
        }

        @Override
        public boolean anyBusinessUnitUserHasPermission(Permissions permission) {
            return true;
        }

        public boolean anyBusinessUnitUserHasAnyPermission(Permissions... permission) {
            return true;
        }

        @Override
        public boolean hasBusinessUnitUserWithAnyPermission(short businessUnitId, Permissions... permissions) {
            return true;
        }

        @Override
        public Optional<BusinessUnitUserPermissions> getBusinessUnitUserForBusinessUnit(Short businessUnitId) {
            return DEV_BUSINESS_UNIT_USER_PERMISSIONS;
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
