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
public class ContactDetails {

    @JsonProperty("primary_email_address")
    private String primaryEmailAddress;

    @JsonProperty("secondary_email_address")
    private String secondaryEmailAddress;

    @JsonProperty("mobile_telephone_number")
    private String mobileTelephoneNumber;

    @JsonProperty("home_telephone_number")
    private String homeTelephoneNumber;

    @JsonProperty("work_telephone_number")
    private String workTelephoneNumber;
}
