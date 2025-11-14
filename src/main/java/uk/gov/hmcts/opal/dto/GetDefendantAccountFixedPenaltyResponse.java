package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.launchdarkly.shaded.org.jetbrains.annotations.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.common.FixedPenaltyTicketDetails;
import uk.gov.hmcts.opal.dto.common.VehicleFixedPenaltyDetails;
import uk.gov.hmcts.opal.util.Versioned;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class GetDefendantAccountFixedPenaltyResponse implements Versioned, ToJsonString {

    @NotNull
    @JsonProperty("vehicle_fixed_penalty_flag")
    private boolean vehicleFixedPenaltyFlag;

    @NotNull
    @JsonProperty("fixed_penalty_ticket_details")
    private FixedPenaltyTicketDetails fixedPenaltyTicketDetails;

    @NotNull
    @JsonProperty("vehicle_fixed_penalty_details")
    private VehicleFixedPenaltyDetails vehicleFixedPenaltyDetails;

    @JsonIgnore
    private Long version; // optional
}