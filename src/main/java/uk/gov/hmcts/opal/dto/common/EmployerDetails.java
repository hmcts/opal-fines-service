package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerDetails {

    @JsonProperty("employer_name")
    private String employerName;

    @JsonProperty("employer_reference")
    private String employerReference;

    @JsonProperty("employer_email_address")
    private String employerEmailAddress;

    @JsonProperty("employer_telephone_number")
    private String employerTelephoneNumber;

    @JsonProperty("employer_address")
    private AddressDetails employerAddress;
}
