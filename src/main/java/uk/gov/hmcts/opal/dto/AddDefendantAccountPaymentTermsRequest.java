package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddDefendantAccountPaymentTermsRequest implements ToJsonString {

    @NotNull
    @JsonProperty("payment_terms")
    private PaymentTerms paymentTerms;

    // Nullable booleans -> use wrapper type Boolean
    @NotNull
    @JsonProperty("request_payment_card")
    private Boolean requestPaymentCard;

    @NotNull
    @JsonProperty("generate_payment_terms_change_letter")
    private Boolean generatePaymentTermsChangeLetter;
}
