package uk.gov.hmcts.opal.common.user.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDto {

    @JsonProperty("permission_id")
    private Long permissionId;

    @JsonProperty("permission_name")
    private String permissionName;
}
