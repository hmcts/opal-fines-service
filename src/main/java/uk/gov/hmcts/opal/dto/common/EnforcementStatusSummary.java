package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnforcementStatusSummary implements ToJsonString {

    @JsonProperty("last_enforcement_action")
    private LastEnforcementAction lastEnforcementAction;

    @JsonProperty("collection_order_made")
    private Boolean collectionOrderMade;

    @JsonProperty("default_days_in_jail")
    private Integer defaultDaysInJail;

    @JsonProperty("enforcement_override")
    private EnforcementOverride enforcementOverride;

    @JsonProperty("last_movement_date")
    private LocalDateTime lastMovementDate;
}
