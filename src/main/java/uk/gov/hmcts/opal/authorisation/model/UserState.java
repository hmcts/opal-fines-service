package uk.gov.hmcts.opal.authorisation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

import java.util.Optional;
import java.util.Set;

@Value
@Builder
public class UserState {

    @NonNull
    String userId;

    @NonNull
    String userName;

    @EqualsAndHashCode.Exclude
    Set<Role> roles;

    @JsonIgnore
    public Optional<Role> getFirstRole() {
        return roles.stream().findFirst();
    }

    @JsonIgnore
    public Optional<String> getFirstRoleBusinessUserId() {
        return getFirstRole().map(Role::getBusinessUserId);
    }
}
