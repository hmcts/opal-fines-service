package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class RemoveDefendantAccountPartyLegacyRequest {

    @JsonProperty("version")
    @NotNull
    private BigInteger version;

    @JsonProperty("defendant_account_id")
    private Long defendantAccountId;

    @JsonProperty("business_unit_id")
    private Short businessUnitId;

    @JsonProperty("business_unit_user_id")
    private String businessUnitUserId;

    @JsonProperty("defendant_account_party_id")
    private Long defendantAccountPartyId;

}

