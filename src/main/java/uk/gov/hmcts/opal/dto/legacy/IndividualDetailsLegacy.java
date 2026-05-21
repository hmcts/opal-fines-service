package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("title")
    @XmlElement(name = "title")
    private String title;

    @JsonProperty("forenames")
    @XmlElement(name = "forenames")
    private String forenames;

    @JsonProperty("surname")
    @XmlElement(name = "surname")
    private String surname;

    @JsonProperty("date_of_birth")
    @XmlElement(name = "date_of_birth")
    private String dateOfBirth; // yyyy-MM-dd

    @JsonProperty("age")
    @XmlElement(name = "age")
    private String age;

    @JsonProperty("national_insurance_number")
    @XmlElement(name = "national_insurance_number")
    private String nationalInsuranceNumber;
}
