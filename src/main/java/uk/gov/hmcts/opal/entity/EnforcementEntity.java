package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

import java.time.LocalDateTime;

@Entity
@Table(name = "enforcements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "enforcementId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class EnforcementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "enforcement_id_seq_generator")
    @SequenceGenerator(name = "enforcement_id_seq_generator", sequenceName = "enforcement_id_seq", allocationSize = 1)
    @Column(name = "enforcement_id", nullable = false)
    private Long enforcementId;

    @ManyToOne
    @JoinColumn(name = "defendant_account_id", referencedColumnName = "defendant_account_id", nullable = false)
    private DefendantAccountEntity defendantAccount;

    @Column(name = "posted_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime postedDate;

    @Column(name = "posted_by", length = 20)
    private String postedBy;

    @Column(name = "result_id", length = 10)
    private String resultId;

    @Column(name = "reason", length = 50)
    private String reason;

    @ManyToOne
    @JoinColumn(name = "enforcer_id", referencedColumnName = "enforcer_id")
    private EnforcerEntity enforcer;

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

    @ManyToOne
    @JoinColumn(name = "hearing_court_id", referencedColumnName = "court_id", nullable = false)
    private CourtEntity hearingCourt;

    @Column(name = "account_type", length = 20)
    private String accountType;

    @Column(name = "posted_by_user_id")
    private Long postedByUserId;

}
