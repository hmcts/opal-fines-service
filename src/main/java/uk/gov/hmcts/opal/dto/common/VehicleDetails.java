package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDetails {

    @JsonProperty("vehicle_make_and_model")
    private String vehicleMakeAndModel;

    @JsonProperty("vehicle_registration")
    private String vehicleRegistration;
}
