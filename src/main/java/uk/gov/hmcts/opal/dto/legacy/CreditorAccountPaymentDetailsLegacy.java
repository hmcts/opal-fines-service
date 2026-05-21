package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class CreditorAccountPaymentDetailsLegacy {

    @JsonProperty("account_name")
    @XmlElement(name = "account_name")
    @Size(min = 1, max = 18)
    private String accountName;

    @JsonProperty("sort_code")
    @XmlElement(name = "sort_code")
    @Size(min = 1, max = 6)
    private String sortCode;

    @JsonProperty("account_number")
    @XmlElement(name = "account_number")
    @Size(min = 1, max = 10)
    private String accountNumber;

    @JsonProperty("account_reference")
    @XmlElement(name = "account_reference")
    @Size(min = 1, max = 18)
    private String accountReference;

    @JsonProperty("pay_by_bacs")
    @XmlElement(name = "pay_by_bacs")
    @NotNull
    private Boolean payByBacs;

    @JsonProperty("hold_payment")
    @XmlElement(name = "hold_payment")
    @NotNull
    private Boolean holdPayment;
}
