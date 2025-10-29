package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.common.FixedPenaltyTicketDetails;
import uk.gov.hmcts.opal.dto.common.VehicleFixedPenaltyDetails;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetDefendantAccountFixedPenaltyResponse {

    @JsonProperty("vehicle_fixed_penalty_flag")
    private boolean vehicleFixedPenaltyFlag;

    @JsonProperty("fixed_penalty_ticket_details")
    private FixedPenaltyTicketDetails fixedPenaltyTicketDetails;

    @JsonProperty("vehicle_fixed_penalty_details")
    private VehicleFixedPenaltyDetails vehicleFixedPenaltyDetails;

    @JsonIgnore
    private String version;
}