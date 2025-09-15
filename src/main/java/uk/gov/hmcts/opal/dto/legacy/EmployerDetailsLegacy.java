package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.*;
import lombok.*;

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
