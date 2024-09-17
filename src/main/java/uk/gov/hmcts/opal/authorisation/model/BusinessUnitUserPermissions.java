package uk.gov.hmcts.opal.authorisation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.Collections;
import java.util.Set;

@Builder
@Data
public class BusinessUnitUserPermissions {

    @NonNull
    String businessUserId;

    @NonNull
    Short businessUnitId;

    @EqualsAndHashCode.Exclude
    @NonNull
    Set<Permission> permissions;

    @JsonCreator
    public BusinessUnitUserPermissions(@JsonProperty("business_user_id") String businessUserId,
                                       @JsonProperty("business_unit_id") Short businessUnitId,
                                       @JsonProperty("permissions") Set<Permission> permissions) {

        this.businessUserId = businessUserId;
        this.businessUnitId = businessUnitId;
        this.permissions = permissions;
    }

    public boolean hasPermission(Permissions permission) {
        return permissions.stream().anyMatch(p -> p.matches(permission));
    }

    public boolean doesNotHavePermission(Permissions permission) {
        return !hasPermission(permission);
    }

    public boolean matchesBusinessUnitId(Short roleBusinessUnitId) {
        return businessUnitId.equals(roleBusinessUnitId);
    }

    public static class DeveloperRole extends BusinessUnitUserPermissions {
        DeveloperRole() {
            super("", Short.MAX_VALUE, Collections.emptySet());
        }

        @Override
        public boolean hasPermission(Permissions permission) {
            return true;
        }

        @Override
        public boolean matchesBusinessUnitId(Short roleBusinessUnitId) {
            return true;
        }
    }
}
