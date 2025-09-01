package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IndividualDetails {

    @JsonProperty("title")
    private String title;

    @JsonProperty("forenames")
    private String forenames;

    @JsonProperty("surname")
    private String surname;

    @JsonProperty("date_of_birth")
    private String dateOfBirth;

    @JsonProperty("age")
    private String age;

    @JsonProperty("national_insurance_number")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String nationalInsuranceNumber;

    @JsonProperty("individual_aliases")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private List<IndividualAlias> individualAliases;
}
