package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class AddressDetailsLegacy {

    @XmlElement(name = "address_line_1")
    private String addressLine1;

    @XmlElement(name = "address_line_2")
    private String addressLine2;

    @XmlElement(name = "address_line_3")
    private String addressLine3;

    @XmlElement(name = "address_line_4")
    private String addressLine4;

    @XmlElement(name = "address_line_5")
    private String addressLine5;

    @XmlElement(name = "postcode")
    private String postcode;
}
