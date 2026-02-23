package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @JsonProperty("is_bacs")
    private boolean isBacs;

    @JsonProperty("hold_payment")
    private boolean holdPayment;
}
