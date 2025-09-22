package uk.gov.hmcts.opal.common.user.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStateDto {

    @JsonProperty("user_id")
    private Long userId;

    //users.username
    @JsonProperty("username")
    private String username;

    //Obtained from the Access Token (via Spring Security) until stored in the Database under
    // TDIA: User Service - Matching Key and JIT Provisioning, and only applies when id is 0 (zero) until then.
    @JsonProperty("name")
    private String name;

    @JsonProperty("status")
    private String status;

    @JsonProperty("version")
    private Integer version;

    @JsonProperty("business_unit_users")
    private List<BusinessUnitUserDto> businessUnitUsers;
}
