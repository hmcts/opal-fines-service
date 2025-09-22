package uk.gov.hmcts.opal.common.user.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessUnitUserDto {

    @JsonProperty("business_unit_user_id")
    private String businessUnitUserId;

    @JsonProperty("business_unit_id")
    private Short businessUnitId;

    @JsonProperty("permissions")
    private List<PermissionDto> permissions;
}
