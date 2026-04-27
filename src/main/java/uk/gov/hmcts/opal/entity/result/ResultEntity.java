package uk.gov.hmcts.opal.entity.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "results")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NamedEntityGraph(name = ResultEntity.ENTITY_GRAPH_LITE)
@NamedEntityGraph(
    name = ResultEntity.ENTITY_GRAPH_FULL,
    attributeNodes = {
        @NamedAttributeNode("enforcements")
    }
)
public class ResultEntity {

    public static final String ENTITY_GRAPH_LITE = "ResultEntity.lite";
    public static final String ENTITY_GRAPH_FULL = "ResultEntity.full";

    @Id
    @Column(name = "result_id")
    private String resultId;

    @Column(name = "result_title", length = 50, nullable = false)
    private String resultTitle;

    @Column(name = "result_title_cy", length = 50, nullable = false)
    private String resultTitleCy;

    @Column(name = "result_type", length = 10, nullable = false)
    private String resultType;

    @Column(name = "active", nullable = false)
    private boolean active;

    // maps to imposition_allocation_order in the DTO
    @Column(name = "imposition_allocation_priority")
    private Short impositionAllocationPriority;

    @Column(name = "imposition_creditor", length = 10)
    private String impositionCreditor;

    @Column(name = "imposition", nullable = false)
    private boolean imposition;

    @Column(name = "imposition_category", length = 30)
    private String impositionCategory;

    @Column(name = "imposition_accruing")
    private Boolean impositionAccruing;

    @Column(name = "enforcement", nullable = false)
    private boolean enforcement;

    @Column(name = "enforcement_override", nullable = false)
    private boolean enforcementOverride;

    @Column(name = "further_enforcement_warn", nullable = false)
    private boolean furtherEnforcementWarn;

    @Column(name = "further_enforcement_disallow", nullable = false)
    private boolean furtherEnforcementDisallow;

    @Column(name = "enforcement_hold", nullable = false)
    private boolean enforcementHold;

    @Column(name = "requires_enforcer", nullable = false)
    private boolean requiresEnforcer;

    @Column(name = "generates_hearing", nullable = false)
    private boolean generatesHearing;

    @Column(name = "collection_order", nullable = false)
    private boolean collectionOrder;

    @Column(name = "extend_ttp_disallow", nullable = false)
    private boolean extendTtpDisallow;

    @Column(name = "extend_ttp_preserve_last_enf", nullable = false)
    private boolean extendTtpPreserveLastEnf;

    @Column(name = "prevent_payment_card", nullable = false)
    private boolean preventPaymentCard;

    @Column(name = "lists_monies", nullable = false)
    private boolean listsMonies;

    @Column(name = "result_parameters")
    private String resultParameters;

    @Column(name = "manual_enforcement", nullable = false)
    private boolean manualEnforcement;

    @Column(name = "enf_next_permitted_actions")
    private String enfNextPermittedActions;

    @OneToMany(mappedBy = "result", fetch = FetchType.LAZY)
    private List<EnforcementEntity> enforcements;
}
