package uk.gov.hmcts.opal.entity.result;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "results")
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@Setter
@Getter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "resultId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ResultEntityFull extends AbstractResultEntity {

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

}
