package uk.gov.hmcts.opal.entity.enforcement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.dto.EnforcementAccountType;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@ToString(callSuper = true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class EnforcementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "enforcement_id_seq_generator")
    @SequenceGenerator(name = "enforcement_id_seq_generator", sequenceName = "enforcement_id_seq", allocationSize = 1)
    @Column(name = "enforcement_id", nullable = false)
    private Long enforcementId;

    @Column(name = "defendant_account_id", insertable = false, updatable = false)
    private Long defendantAccountId;

    @Column(name = "posted_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime postedDate;

    @Column(name = "posted_by", length = 20)
    private String postedBy;

    @Column(name = "result_id", length = 10, insertable = false, updatable = false)
    private String resultId;

    @ManyToOne(fetch = FetchType.EAGER)
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

    @Column(name = "case_reference", length = 20)
    private String caseReference;

    @Column(name = "hearing_date")
    @Temporal(TemporalType.TIMESTAMP)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime hearingDate;

    @Column(name = "hearing_court_id", insertable = false, updatable = false)
    private Long hearingCourtId;

    @Column(name = "enforcement_account_type", length = 20)
    private EnforcementAccountType enforcementAccountType;

    @Column(name = "posted_by_name", length = 100)
    private String postedByUsername;

    @Column(name = "result_responses")
    private String resultResponses;

    @Entity
    @Getter
    @EqualsAndHashCode(callSuper = true)
    @Table(name = "enforcements")
    @SuperBuilder
    @NoArgsConstructor
    public static class Lite extends EnforcementEntity {
    }
}
