package uk.gov.hmcts.opal.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.PaymentTerms;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddDefendantAccountPaymentTermsRequest {
    @JsonProperty("payment_terms")
    private PaymentTerms paymentTerms;

    @JsonProperty("request_payment_card")
    private Boolean requestPaymentCard;

    @JsonProperty("generate_payment_terms_change_letter")
    private Boolean generatePaymentTermsChangeLetter;
}
