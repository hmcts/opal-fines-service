package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
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
public class AddDefendantAccountPaymentTermsLegacyRequest {

    @JsonProperty("defendant_account_id")
    @NotBlank
    private String defendantAccountId;

    @JsonProperty("business_unit_id")
    @NotBlank
    private String businessUnitId;

    @JsonProperty("business_unit_user_id")
    @NotBlank
    private String businessUnitUserId;

    @JsonProperty("version")
    @NotNull
    private Integer version;

    @JsonProperty("payment_terms")
    @NotNull
    private LegacyPaymentTerms paymentTerms;

    @JsonProperty("request_payment_card")
    private Boolean requestPaymentCard;

    @JsonProperty("generate_payment_terms_change_letter")
    private Boolean generatePaymentTermsChangeLetter;
}
