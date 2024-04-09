package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Column(name = "defendant_account_id", nullable = false)
    private Long defendantAccountId;

    @Column(name = "posted_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime postedDate;

    @Column(name = "posted_by", length = 20)
    private String postedBy;

    @Column(name = "posted_by_user_id", nullable = false)
    private Long postedByUserId;

    @Column(name = "original_posted_date")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime originalPostedDate;

    @Column(name = "result_id", length = 6, nullable = false)
    private String resultId;

    @Column(name = "imposing_court_id")
    private Long imposingCourtId;

    @Column(name = "imposed_date")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime imposedDate;

    @Column(name = "imposed_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal imposedAmount;

    @Column(name = "paid_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal paidAmount;

    @Column(name = "offence_id", nullable = false)
    private Short offenceId;

    @Column(name = "creditor_account_id", nullable = false)
    private Long creditorAccountId;

    @Column(name = "unit_fine_adjusted")
    private boolean unitFineAdjusted;

    @Column(name = "unit_fine_units")
    private Short unitFineUnits;

    @Column(name = "completed")
    private boolean completed;
}
