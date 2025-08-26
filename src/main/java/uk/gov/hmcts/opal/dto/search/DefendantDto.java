package uk.gov.hmcts.opal.dto.search;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.networknt.schema.utils.StringUtils;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefendantDto {

    @JsonProperty("include_aliases")
    @NotNull
    private Boolean includeAliases;

    @JsonProperty("organisation")
    @NotNull
    private Boolean organisation;

    @JsonProperty("address_line_1")
    private String addressLine1;

    @JsonProperty("postcode")
    private String postcode;

    @JsonProperty("organisation_name")
    @NotNull
    private String organisationName;

    @JsonProperty("exact_match_organisation_name")
    private Boolean exactMatchOrganisationName;

    @JsonProperty("surname")
    @NotNull
    private String surname;

    @JsonProperty("exact_match_surname")
    private Boolean exactMatchSurname;

    @JsonProperty("forenames")
    private String forenames;

    @JsonProperty("exact_match_forenames")
    private Boolean exactMatchForenames;

    @JsonProperty("birth_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @JsonProperty("national_insurance_number")
    private String nationalInsuranceNumber;

    public String getInitials() {
        StringBuilder initials = new StringBuilder();

        if (StringUtils.isNotBlank(forenames)) {
            initials.append(forenames.charAt(0));
        }

        if (StringUtils.isNotBlank(surname)) {
            initials.append(surname.charAt(0));
        }

        return initials.isEmpty() ? null : initials.toString().toUpperCase();
    }
}
