package uk.gov.hmcts.opal.dto.legacy.common;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AddressDetails {

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
