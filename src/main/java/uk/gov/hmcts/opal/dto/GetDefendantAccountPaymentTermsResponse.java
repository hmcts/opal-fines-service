package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.util.Versioned;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetDefendantAccountPaymentTermsResponse implements ToJsonString, Versioned {

    @JsonIgnore
    private BigInteger version; // internal (for ETag)

    @JsonProperty("payment_terms")
    private PaymentTerms paymentTerms;

    @JsonProperty("payment_card_last_requested")
    private LocalDate paymentCardLastRequested;

    @JsonProperty("last_enforcement")
    private String lastEnforcement;
}
