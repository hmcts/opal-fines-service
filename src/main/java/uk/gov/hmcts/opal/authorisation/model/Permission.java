package uk.gov.hmcts.opal.authorisation.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class Permission {

    @NonNull
    Long permissionId;

    @NonNull
    String permissionName;
}
