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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "impositions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "impositionId")
public class ImpositionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "imposition_id_seq_generator")
    @SequenceGenerator(name = "imposition_id_seq_generator", sequenceName = "imposition_id_seq", allocationSize = 1)
    @Column(name = "imposition_id", nullable = false)
    private Long impositionId;

    @ManyToOne
    @JoinColumn(name = "defendant_account_id", updatable = false)
    private DefendantAccountEntity defendantAccount;

    @Column(name = "posted_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime postedDate;

    @Column(name = "posted_by", length = 20)
    private String postedBy;

    @ManyToOne
    @JoinColumn(name = "posted_by_user_id", nullable = false)
    private UserEntity postedByUser;

    @Column(name = "original_posted_date")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime originalPostedDate;

    @Column(name = "result_id", length = 6, nullable = false)
    private String resultId;

    @ManyToOne
    @JoinColumn(name = "imposing_court_id", nullable = false)
    private CourtEntity imposingCourt;

    @Column(name = "imposed_date")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime imposedDate;

    @Column(name = "imposed_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal imposedAmount;

    @Column(name = "paid_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal paidAmount;

    @Column(name = "offence_id", nullable = false)
    private Short offenceId;

    @ManyToOne
    @JoinColumn(name = "creditor_account_id", updatable = false)
    private CreditorAccountEntity creditorAccount;

    @Column(name = "unit_fine_adjusted")
    private boolean unitFineAdjusted;

    @Column(name = "unit_fine_units")
    private Short unitFineUnits;

    @Column(name = "completed")
    private boolean completed;
}
