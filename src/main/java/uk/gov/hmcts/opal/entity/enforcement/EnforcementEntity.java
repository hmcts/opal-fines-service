package uk.gov.hmcts.opal.entity.enforcement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.dto.EnforcementAccountType;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"result", "defendantAccount", "enforcer", "hearingCourt"})
@ToString(exclude = {"result", "defendantAccount", "enforcer", "hearingCourt"})
@Table(name = "enforcements")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NamedEntityGraph(
    name = EnforcementEntity.ENTITY_GRAPH_LITE,
    attributeNodes = {
        @NamedAttributeNode("result")
    }
)
@NamedEntityGraph(
    name = EnforcementEntity.ENTITY_GRAPH_FULL,
    attributeNodes = {
        @NamedAttributeNode("result"),
        @NamedAttributeNode("defendantAccount"),
        @NamedAttributeNode("enforcer"),
        @NamedAttributeNode("hearingCourt")
    }
)
public class EnforcementEntity {

    public static final String ENTITY_GRAPH_LITE = "EnforcementEntity.lite";
    public static final String ENTITY_GRAPH_FULL = "EnforcementEntity.full";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "enforcement_id_seq_generator")
    @SequenceGenerator(name = "enforcement_id_seq_generator", sequenceName = "enforcement_id_seq", allocationSize = 1)
    @Column(name = "enforcement_id", nullable = false)
    private Long enforcementId;

    @Column(name = "defendant_account_id", insertable = false, updatable = false)
    private Long defendantAccountId;

    @Column(name = "posted_date", nullable = false)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime postedDate;

    @Column(name = "posted_by", length = 20)
    private String postedBy;

    @Column(name = "result_id", length = 10, insertable = false, updatable = false)
    private String resultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", insertable = false, updatable = false)
    private ResultEntity.Lite result;

    @Column(name = "reason", length = 50)
    private String reason;

    @Column(name = "enforcer_id", insertable = false, updatable = false)
    private Long enforcerId;

    @Column(name = "jail_days")
    private Integer jailDays;

    @Column(name = "warrant_reference", length = 20)
    private String warrantReference;

    @Column(name = "case_reference", length = 40)
    private String caseReference;

    @Column(name = "hearing_date")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime hearingDate;

    @Column(name = "earliest_release_date")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime earliestReleaseDate;

    @Column(name = "hearing_court_id", insertable = false, updatable = false)
    private Long hearingCourtId;

    @Enumerated(EnumType.STRING)
    @Column(name = "enforcement_account_type", length = 20)
    private EnforcementAccountType enforcementAccountType;

    @Column(name = "posted_by_name", length = 100)
    private String postedByUsername;

    @Column(name = "result_responses")
    private String resultResponses;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defendant_account_id", nullable = false, insertable = false, updatable = false)
    private DefendantAccountEntity defendantAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enforcer_id", insertable = false, updatable = false)
    private EnforcerEntity enforcer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hearing_court_id", nullable = false, insertable = false, updatable = false)
    private CourtEntity.Lite hearingCourt;
}
