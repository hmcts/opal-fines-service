package uk.gov.hmcts.opal.authorisation.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Collections;
import java.util.Set;

@Builder
@EqualsAndHashCode
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ToString
public class Role {

    @NonNull
    String businessUserId;

    @NonNull
    Short businessUnitId;

    @EqualsAndHashCode.Exclude
    @NonNull
    Set<Permission> permissions;

    public boolean hasPermission(Permissions permission) {
        return permissions.stream().anyMatch(p -> p.matches(permission));
    }

    public boolean doesNotHavePermission(Permissions permission) {
        return !hasPermission(permission);
    }

    public boolean matchesBusinessUnitId(short roleBusinessUnitId) {
        return roleBusinessUnitId == businessUnitId;
    }

    public static class DeveloperRole extends Role {
        DeveloperRole() {
            super("", Short.MAX_VALUE, Collections.emptySet());
        }

        @Override
        public boolean hasPermission(Permissions permission) {
            return true;
        }

        @Override
        public boolean matchesBusinessUnitId(short roleBusinessUnitId) {
            return true;
        }
    }
}
