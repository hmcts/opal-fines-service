package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "result_id_seq_generator")
    private String resultId;

    @Column(name = "result_title", length = 50, nullable = false)
    private String resultTitle;

    @Column(name = "result_title_cy", length = 50, nullable = false)
    private String resultTitleCy;

    @Column(name = "result_type", length = 10, nullable = false)
    private String resultType;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "imposition", nullable = false)
    private boolean imposition;

    @Column(name = "imposition_category", length = 30)
    private String impositionCategory;

    @Column(name = "imposition_allocation_priority")
    private Short impositionAllocationPriority;

    @Column(name = "imposition_accruing")
    private Boolean impositionAccruing;

    @Column(name = "imposition_creditor", length = 10)
    private String impositionCreditor;

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

    @Column(name = "user_entries")
    private String userEntries;

}
