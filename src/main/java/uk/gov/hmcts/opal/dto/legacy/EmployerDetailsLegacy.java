package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class EmployerDetailsLegacy {

    @XmlElement(name = "employer_name")
    private String employerName;

    @XmlElement(name = "employer_reference")
    private String employerReference;

    @XmlElement(name = "employer_email_address")
    private String employerEmailAddress;

    @XmlElement(name = "employer_telephone_number")
    private String employerTelephoneNumber;

    @XmlElement(name = "employer_address")
    private AddressDetailsLegacy employerAddress;
}
