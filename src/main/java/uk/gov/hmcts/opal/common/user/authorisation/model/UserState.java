package uk.gov.hmcts.opal.common.user.authorisation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser.DeveloperBusinessUnitUser;

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
    Set<BusinessUnitUser> businessUnitUser;

    @JsonCreator
    public UserState(
        @JsonProperty("user_id") Long userId,
        @JsonProperty("user_name") String userName,
        @JsonProperty("business_unit_user") Set<BusinessUnitUser> businessUnitUser
    ) {
        this.userId = userId;
        this.userName = userName;
        this.businessUnitUser = businessUnitUser;
    }

    public boolean anyBusinessUnitUserHasPermission(Permissions permission) {
        return businessUnitUser.stream().anyMatch(r -> r.hasPermission(permission));
    }

    public boolean noBusinessUnitUserHasPermission(Permissions permission) {
        return !anyBusinessUnitUserHasPermission(permission);
    }

    public boolean anyBusinessUnitUserHasAnyPermission(Permissions... permission) {
        return businessUnitUser.stream().anyMatch(r -> r.hasAnyPermission(permission));
    }

    public UserBusinessUnits allBusinessUnitUsersWithPermission(Permissions permission) {
        return new UserBusinessUnitsImpl(
            businessUnitUser.stream().filter(r -> r.hasPermission(permission)).collect(Collectors.toSet()));
    }

    public boolean hasBusinessUnitUserWithPermission(short businessUnitId, Permissions permission) {
        return userHasPermission(getBusinessUnitUserForBusinessUnit(businessUnitId), permission);
    }

    public static boolean userHasPermission(Optional<BusinessUnitUser> user, Permissions permission) {
        return user.stream().anyMatch(r -> r.hasPermission(permission));
    }

    public boolean hasBusinessUnitUserWithAnyPermission(short businessUnitId, Permissions... permissions) {
        return userHasAnyPermission(getBusinessUnitUserForBusinessUnit(businessUnitId), permissions);
    }

    public static boolean userHasAnyPermission(Optional<BusinessUnitUser> user, Permissions... permissions) {
        return user.stream().anyMatch(r -> r.hasAnyPermission(permissions));
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

    public Optional<BusinessUnitUser> getBusinessUnitUserForBusinessUnit(short businessUnitId) {
        return businessUnitUser.stream()
            .filter(r -> r.matchesBusinessUnitId(businessUnitId))
            .findFirst();
    }

    public static interface UserBusinessUnits {
        boolean containsBusinessUnit(Short businessUnitId);
    }

    public static class UserBusinessUnitsImpl implements UserBusinessUnits {
        private final Set<BusinessUnitUser> businessUnitUser;
        private final Set<Short> businessUnits;

        public UserBusinessUnitsImpl(Set<BusinessUnitUser> businessUnitUser) {
            this.businessUnitUser = businessUnitUser;
            businessUnits = businessUnitUser.stream().map(r -> r.getBusinessUnitId())
                .collect(Collectors.toSet());
        }

        public boolean containsBusinessUnit(Short businessUnitId) {
            return businessUnits.contains(businessUnitId);
        }
    }

    public static class DeveloperUserState extends UserState {
        private static final Optional<BusinessUnitUser> DEV_BUSINESS_UNIT_USER =
            Optional.of(new DeveloperBusinessUnitUser());

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
        public Optional<BusinessUnitUser> getBusinessUnitUserForBusinessUnit(short businessUnitId) {
            return DEV_BUSINESS_UNIT_USER;
        }

        @Override
        public boolean hasBusinessUnitUserWithPermission(short businessUnitId, Permissions permission) {
            return true;
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
