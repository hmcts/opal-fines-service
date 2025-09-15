package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class VehicleDetailsLegacy {

    @XmlElement(name = "vehicle_make_and_model")
    private String vehicleMakeAndModel;

    @XmlElement(name = "vehicle_registration")
    private String vehicleRegistration;
}
