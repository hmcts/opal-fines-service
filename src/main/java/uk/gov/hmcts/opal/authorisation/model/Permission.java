package uk.gov.hmcts.opal.authorisation.model;

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
    public Permission(@JsonProperty("permissionId") Long permissionId,
                      @JsonProperty("permissionName") String permissionName) {
        this.permissionId = permissionId;
        this.permissionName = permissionName;
    }

    boolean matches(Permissions candidate) {
        return candidate.id == permissionId;
    }
}
