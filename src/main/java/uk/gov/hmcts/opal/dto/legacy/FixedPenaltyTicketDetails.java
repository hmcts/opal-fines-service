package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class FixedPenaltyTicketDetails {

    @XmlElement(name = "issuing_authority")
    @JsonProperty("issuing_authority")
    private String issuingAuthority;

    @XmlElement(name = "ticket_number")
    @JsonProperty("ticket_number")
    private String ticketNumber;

    @XmlElement(name = "time_of_offence")
    @JsonProperty("time_of_offence")
    private String timeOfOffence;

    @XmlElement(name = "place_of_offence")
    @JsonProperty("place_of_offence")
    private String placeOfOffence;

}
