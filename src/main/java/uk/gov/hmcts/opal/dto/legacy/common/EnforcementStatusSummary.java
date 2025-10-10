package uk.gov.hmcts.opal.dto.legacy.common;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.common.EnforcementOverride;
import uk.gov.hmcts.opal.dto.common.LastEnforcementAction;
import uk.gov.hmcts.opal.util.LocalDateAdapter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@XmlAccessorType(XmlAccessType.FIELD)
public class EnforcementStatusSummary {

    @XmlElement(name = "last_enforcement_action")
    private LastEnforcementAction lastEnforcementAction;

    @XmlElement(name = "collection_order_made")
    private Boolean collectionOrderMade;

    @XmlElement(name = "default_days_in_jail")
    private Integer defaultDaysInJail;

    @XmlElement(name = "enforcement_override")
    private EnforcementOverride enforcementOverride;

    @XmlElement(name = "last_movement_date")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate lastMovementDate;
}
