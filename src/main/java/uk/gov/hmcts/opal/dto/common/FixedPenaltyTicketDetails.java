package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.launchdarkly.shaded.org.jetbrains.annotations.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.ALWAYS)
public class FixedPenaltyTicketDetails {

    @NotNull
    @Size(min = 1)
    @JsonProperty("issuing_authority")
    private String issuingAuthority;

    @NotNull
    @Size(min = 1)
    @JsonProperty("ticket_number")
    private String ticketNumber;

    @NotNull
    @Size(min = 1)
    @JsonProperty("time_of_offence")
    private String timeOfOffence;

    @NotNull
    @Size(min = 1)
    @JsonProperty("place_of_offence")
    private String placeOfOffence;
}