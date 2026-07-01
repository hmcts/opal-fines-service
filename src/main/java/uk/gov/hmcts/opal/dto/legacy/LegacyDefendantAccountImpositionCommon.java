package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.util.LocalDateAdapter;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyDefendantAccountImpositionCommon {

    @JsonProperty("date_added")
    @XmlElement(name = "date_added")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate dateAdded;

    @JsonProperty("date_imposed")
    @XmlElement(name = "date_imposed")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate dateImposed;

    @JsonProperty("imposition")
    @XmlElement(name = "imposition")
    private LegacyResultReferenceCommon imposition;

    @JsonProperty("creditor")
    @XmlElement(name = "creditor")
    private LegacyImpositionCreditorReferenceCommon creditor;

    @JsonProperty("imposed_amount")
    @XmlElement(name = "imposed_amount")
    private BigDecimal imposedAmount;

    @JsonProperty("paid_amount")
    @XmlElement(name = "paid_amount")
    private BigDecimal paidAmount;

    @JsonProperty("balance")
    @XmlElement(name = "balance")
    private BigDecimal balance;

    @JsonProperty("offence")
    @XmlElement(name = "offence")
    private LegacyOffenceReferenceCommon offence;

    @JsonProperty("imposed_by")
    @XmlElement(name = "imposed_by")
    private LegacyCourtReferenceCommon imposedBy;

    @JsonProperty("imposition_id")
    @XmlElement(name = "imposition_id")
    private Long impositionId;
}
