package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.util.LocalDateAdapter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentTermsDto {

    @JsonProperty("terms_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlElement(name = "terms_date")
    private LocalDate termsDate;

    @JsonProperty("terms_type_code")
    @XmlElement(name = "terms_type_code")
    private String termsTypeCode;

    @JsonProperty("instalment_amount")
    @XmlElement(name = "instalment_amount")
    private BigDecimal instalmentAmount;

    @JsonProperty("instalment_period")
    @XmlElement(name = "instalment_period")
    private String instalmentPeriod;

    @JsonProperty("instalment_lump_sum")
    @XmlElement(name = "instalment_lump_sum")
    private BigDecimal instalmentLumpSum;

    @JsonProperty("jail_days")
    @XmlElement(name = "jail_days")
    private Integer jailDays;

    @JsonProperty("wording")
    @XmlElement(name = "wording")
    private String wording;

}
