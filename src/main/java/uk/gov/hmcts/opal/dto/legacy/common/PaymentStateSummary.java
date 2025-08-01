package uk.gov.hmcts.opal.dto.legacy.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentStateSummary {

    @JsonProperty("imposed_amount")
    private String imposedAmount;

    @JsonProperty("arrears_amount")
    private String arrearsAmount;

    @JsonProperty("paid_amount")
    private String paidAmount;

    @JsonProperty("account_balance")
    private String accountBalance;
}
