package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonRootName(value = "imposition")
@XmlAccessorType(XmlAccessType.FIELD)
public class ImpositionDto {
    @JsonProperty("imposition_id")
    @XmlElement(name = "imposition_id")
    private Integer impositionId;

    @JsonProperty("posted_date")
    @XmlElement(name = "posted_date")
    private String postedDate;

    @JsonProperty("result_id")
    @XmlElement(name = "result_id")
    private String resultId;

    @JsonProperty("imposed_date")
    @XmlElement(name = "imposed_date")
    private String imposedDate;

    @JsonProperty("imposing_court_code")
    @XmlElement(name = "imposing_court_code")
    private Integer imposingCourtCode;

    @JsonProperty("imposed_amount")
    @XmlElement(name = "imposed_amount")
    private Double imposedAmount;

    @JsonProperty("paid_amount")
    @XmlElement(name = "paid_amount")
    private Double paidAmount;

    @JsonProperty("offence_title")
    @XmlElement(name = "offence_title")
    private String offenceTitle;

    @JsonProperty("creditor_account_number")
    @XmlElement(name = "creditor_account_number")
    private String creditorAccountNumber;

    @JsonProperty("creditor_name")
    @XmlElement(name = "creditor_name")
    private String creditorName;

}
