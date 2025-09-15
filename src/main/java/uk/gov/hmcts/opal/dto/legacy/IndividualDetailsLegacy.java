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
