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
public class FixedPenaltyTicketDetails {

    @JsonProperty("issuing_authority")
    private String issuingAuthority;

    @JsonProperty("ticket_number")
    private String ticketNumber;

    @JsonProperty("time_of_offence")
    private String timeOfOffence;

    @JsonProperty("place_of_offence")
    private String placeOfOffence;
}