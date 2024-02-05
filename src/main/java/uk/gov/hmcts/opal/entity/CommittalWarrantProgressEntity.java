package uk.gov.hmcts.opal.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "committal_warrant_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommittalWarrantProgressEntity {

    @Id
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "defendant_account_id", nullable = false)
    private DefendantAccountEntity defendantAccount;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "enforcement_id", nullable = false)
    private EnforcementEntity enforcement;

    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "body_receipt_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime bodyReceiptDate;

    @Column(name = "certificate_part_a_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime certificatePartADate;

    @Column(name = "certificate_part_b_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime certificatePartBDate;

    @ManyToOne
    @JoinColumn(name = "prison_id")
    private PrisonEntity prison;

}
