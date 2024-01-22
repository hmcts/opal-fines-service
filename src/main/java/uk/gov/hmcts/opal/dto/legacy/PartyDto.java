package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonRootName(value = "party")
public class PartyDto {

    @JsonProperty("party_id")
    private Integer partyId;

    @JsonProperty("association_type")
    private String associationType;

    @JsonProperty("debtor")
    private Boolean debtor;

    @JsonProperty("organisation")
    private Boolean organisation;

    @JsonProperty("organisation_name")
    private String organisationName;

    @JsonProperty("surname")
    private String surname;

    @JsonProperty("forenames")
    private String forenames;

    @JsonProperty("initials")
    private String initials;

    @JsonProperty("title")
    private String title;

    @JsonProperty("birth_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @JsonProperty("age")
    private Integer age;

    @JsonProperty("national_insurance_number")
    private String nationalInsuranceNumber;

    @JsonProperty("last_changed_date")
    private String lastChangedDate;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("address_line_1")
    private String addressLine1;

    @JsonProperty("address_line_2")
    private String addressLine2;

    @JsonProperty("address_line_3")
    private String addressLine3;

    @JsonProperty("address_line_4")
    private String addressLine4;

    @JsonProperty("address_line_5")
    private String addressLine5;

    @JsonProperty("postcode")
    private String postcode;

}
