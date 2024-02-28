package uk.gov.hmcts.opal.authorisation.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

import java.util.Set;

@Value
@Builder
public class Role {

    @NonNull
    String businessUserId;

    @NonNull
    String businessUnit;

    @EqualsAndHashCode.Exclude
    @NonNull
    Set<Permission> permissions;
}
