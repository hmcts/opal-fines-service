package uk.gov.hmcts.opal.dto.legacy.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import uk.gov.hmcts.opal.util.LocalDateAdapter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IndividualDetails {

    @XmlElement(name = "title")
    private String title;

    @XmlElement(name = "forenames")
    private String firstNames;

    @XmlElement(name = "surname")
    private String surname;

    @XmlElement(name = "date_of_birth")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate dateOfBirth;

    @XmlElement(name = "age")
    private String age;

    @XmlElement(name = "national_insurance_number")
    private String nationalInsuranceNumber;

    @XmlElementWrapper(name = "individual_aliases")
    @XmlElement(name = "individual_aliases_element")
    private IndividualAlias[] individualAliases;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Jacksonized
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class IndividualAlias {

        @XmlElement(name = "alias_id")
        private String aliasId;

        @XmlElement(name = "sequence_number")
        private Short sequenceNumber;

        @XmlElement(name = "surname")
        private String surname;

        @XmlElement(name = "forenames")
        private String forenames;
    }
}
