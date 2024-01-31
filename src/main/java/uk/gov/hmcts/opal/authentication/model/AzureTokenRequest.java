package uk.gov.hmcts.opal.authentication.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AzureTokenRequest {

    @JsonProperty("grant_type")
    String grantType;

    @JsonProperty("client_id")
    String clientId;

    @JsonProperty("client_secret")
    String clientSecret;

    String scope;

    String username;

    private String password;
}
