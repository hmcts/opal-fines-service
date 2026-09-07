package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class LegacyDefendantAccountGetFixedPenaltyResponse {

    @JsonProperty("version")
    private BigInteger version;

    @JsonProperty("vehicle_fixed_penalty_flag")
    private boolean vehicleFixedPenaltyFlag;

    @JsonProperty("fixed_penalty_ticket_details")
    private FixedPenaltyTicketDetails fixedPenaltyDetails;

    @JsonProperty("vehicle_fixed_penalty_details")
    private VehicleFixedPenaltyDetails vehicleFixedPenaltyDetails;

}
