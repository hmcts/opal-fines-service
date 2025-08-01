package uk.gov.hmcts.opal.dto.legacy.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class IndividualDetails {

    @JsonProperty("title")
    private String title;

    @JsonProperty("first_names")
    private String firstNames;

    @JsonProperty("surname")
    private String surname;

    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;

    @JsonProperty("age")
    private String age;

    @JsonProperty("national_insurance_number")
    private String nationalInsuranceNumber;

    @JsonProperty("individual_aliases")
    private IndividualAlias[] individualAliases;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Jacksonized
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class IndividualAlias {

        @JsonProperty("alias_id")
        private String aliasId;

        @JsonProperty("sequence_number")
        private Short sequenceNumber;

        @JsonProperty("surname")
        private String surname;

        @JsonProperty("forenames")
        private String forenames;
    }
}
