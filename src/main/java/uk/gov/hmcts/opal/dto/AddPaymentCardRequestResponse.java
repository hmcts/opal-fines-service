package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AddPaymentCardRequestResponse {

    @JsonProperty("defendant_account_id")
    private final Long defendantAccountId;
}
