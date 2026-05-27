package uk.gov.hmcts.opal.dto.legacy.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
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
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LegacyCreditorAccountPaymentDetails {

    @JsonProperty("account_name")
    @XmlElement(name = "account_name")
    private String accountName;

    @JsonProperty("sort_code")
    @XmlElement(name = "sort_code")
    private String sortCode;

    @JsonProperty("account_number")
    @XmlElement(name = "account_number")
    private String accountNumber;

    @JsonProperty("account_reference")
    @XmlElement(name = "account_reference")
    private String accountReference;

    @JsonProperty("pay_by_bacs")
    @XmlElement(name = "pay_by_bacs", required = true)
    private Boolean payByBacs;

    @JsonProperty("hold_payment")
    @XmlElement(name = "hold_payment", required = true)
    private Boolean holdPayment;
}
