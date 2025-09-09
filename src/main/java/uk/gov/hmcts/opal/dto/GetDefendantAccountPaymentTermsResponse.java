package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetDefendantAccountPaymentTermsResponse {

    @JsonIgnore
    private Integer version;

    @JsonProperty("payment_terms")
    private PaymentTerms paymentTerms;

    @JsonProperty("posted_details")
    private PostedDetails postedDetails;

    @JsonProperty("payment_card_last_requested")
    private LocalDate paymentCardLastRequested;

    @JsonProperty("date_last_amended")
    private LocalDate dateLastAmended;

    @JsonProperty("extension")
    private Boolean extension;

    @JsonProperty("last_enforcement")
    private String lastEnforcement;
}
