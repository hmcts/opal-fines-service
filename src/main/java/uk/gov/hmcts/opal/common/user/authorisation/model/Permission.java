package uk.gov.hmcts.opal.common.user.authorisation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonCreator
    public Permission(@JsonProperty("permission_id") Long permissionId,
                      @JsonProperty("permission_name") String permissionName) {
        this.permissionId = permissionId;
        this.permissionName = permissionName;
    }

    boolean matchesPermissions(Permissions candidate) {
        return candidate.id == permissionId;
    }
}
