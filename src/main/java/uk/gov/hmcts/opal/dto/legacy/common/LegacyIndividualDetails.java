package uk.gov.hmcts.opal.dto.legacy.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import uk.gov.hmcts.opal.dto.common.IndividualAlias;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.util.LocalDateAdapter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LegacyIndividualDetails {

    @XmlElement(name = "title")
    private String title;

    @XmlElement(name = "first_names")
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

    @XmlElement(name = "individual_aliases")
    private LegacyIndividualAlias[] individualAliases;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Jacksonized
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LegacyIndividualAlias {

        @XmlElement(name = "alias_id")
        private String aliasId;

        @XmlElement(name = "sequence_number")
        private Short sequenceNumber;

        @XmlElement(name = "surname")
        private String surname;

        @XmlElement(name = "forenames")
        private String forenames;

        public IndividualAlias toOpalDto() {
            return IndividualAlias.builder()
                .aliasId(this.getAliasId())
                .sequenceNumber(Integer.valueOf(this.getSequenceNumber()))
                .surname(this.getSurname())
                .forenames(this.getForenames())
                .build();
        }
    }

    public IndividualDetails toOpalDto() {
        return IndividualDetails.builder()
            .title(this.getTitle())
            .forenames(this.getFirstNames())
            .surname(this.getSurname())
            .dateOfBirth(String.valueOf(this.getDateOfBirth()))
            .age(this.getAge())
            .nationalInsuranceNumber(this.getNationalInsuranceNumber())
            .individualAliases(this.getIndividualAliases() == null
                ? Collections.emptyList()
                : Arrays.stream(this.getIndividualAliases())
                .map(LegacyIndividualAlias::toOpalDto)
                .toList())
            .build();
    }
}
