package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddPaymentCardRequestResponse {

    @JsonProperty("defendant_account_id")
    private final Long defendantAccountId;

    public AddPaymentCardRequestResponse(Long defendantAccountId) {
        this.defendantAccountId = defendantAccountId;
    }

    public Long getDefendantAccountId() {
        return defendantAccountId;
    }
}
