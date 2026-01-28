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
import uk.gov.hmcts.opal.dto.ToXmlString;
import uk.gov.hmcts.opal.dto.common.AddressDetails;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyAddressDetails implements ToXmlString {

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

    public  AddressDetails toOpalDto() {
        return AddressDetails.builder()
            .addressLine1(this.getAddressLine1())
            .addressLine2(this.getAddressLine2())
            .addressLine3(this.getAddressLine3())
            .addressLine4(this.getAddressLine4())
            .addressLine5(this.getAddressLine5())
            .postcode(this.getPostcode())
            .build();
    }

}
