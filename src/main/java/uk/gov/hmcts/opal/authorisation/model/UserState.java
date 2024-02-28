package uk.gov.hmcts.opal.authorisation.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

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

}
