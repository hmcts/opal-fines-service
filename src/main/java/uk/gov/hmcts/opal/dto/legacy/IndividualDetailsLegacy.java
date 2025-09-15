package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class IndividualDetailsLegacy {

    @XmlElement(name = "title")
    private String title;

    @XmlElement(name = "forenames")
    private String forenames;

    @XmlElement(name = "surname")
    private String surname;

    @XmlElement(name = "date_of_birth")
    private String dateOfBirth; // yyyy-MM-dd

    @XmlElement(name = "age")
    private String age;

    @XmlElement(name = "national_insurance_number")
    private String nationalInsuranceNumber;
}
