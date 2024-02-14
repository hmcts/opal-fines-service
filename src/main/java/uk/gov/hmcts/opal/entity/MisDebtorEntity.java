package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name = "mis_debtors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MisDebtorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mis_debtor_id_seq_generator")
    @SequenceGenerator(name = "mis_debtor_id_seq_generator", sequenceName = "mis_debtor_id_seq", allocationSize = 1)
    @Column(name = "mis_debtor_id", nullable = false)
    private Long misDebtorId;

    @ManyToOne
    @JoinColumn(name = "business_unit_id", referencedColumnName = "business_unit_id", nullable = false)
    private BusinessUnitEntity businessUnit;

    @Column(name = "debtor_name", length = 100, nullable = false)
    private String debtorName;

    @Column(name = "account_category", length = 1)
    private String accountCategory;

    @Column(name = "arrears_category", length = 1)
    private String arrearsCategory;

    @Column(name = "account_number", length = 1, nullable = false)
    private String accountNumber;

    @Column(name = "account_start_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime accountStartDate;

    @Column(name = "terms_type", length = 1, nullable = false)
    private String termsType;

    @Column(name = "instalment_amount", precision = 18, scale = 2)
    private BigDecimal instalmentAmount;

    @Column(name = "lump_sum", precision = 18, scale = 2)
    private BigDecimal lumpSum;

    @Column(name = "terms_date")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime termsDate;

    @Column(name = "days_in_jail", nullable = false)
    private Short daysInJail;

    @Column(name = "date_last_movement")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime dateLastMovement;

    @Column(name = "last_enforcement", length = 6)
    private String lastEnforcement;

    @Column(name = "amount_imposed", precision = 18, scale = 2, nullable = false)
    private BigDecimal amountImposed;

    @Column(name = "amount_paid", precision = 18, scale = 2, nullable = false)
    private BigDecimal amountPaid;

    @Column(name = "amount_outstanding", precision = 18, scale = 2, nullable = false)
    private BigDecimal amountOutstanding;

}
