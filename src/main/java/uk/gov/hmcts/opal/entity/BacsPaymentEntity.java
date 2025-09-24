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
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bacs_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "bacsPaymentId")
public class BacsPaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bacs_payment_id_seq_generator")
    @SequenceGenerator(name = "bacs_payment_id_seq_generator", sequenceName = "bacs_payment_id_seq", allocationSize = 1)
    @Column(name = "bacs_payment_id", nullable = false)
    private Long bacsPaymentId;

    @ManyToOne
    @JoinColumn(name = "business_unit_id", nullable = false)
    private BusinessUnitFullEntity businessUnit;

    @Column(name = "bacs_number", nullable = false)
    private Long bacsNumber;

    @Column(name = "issue_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate issueDate;

    @Column(name = "creditor_transaction_id")
    private Long creditorTransactionId;

    @Column(name = "defendant_transaction_id")
    private Long defendantTransactionId;

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "status", length = 10, nullable = false)
    private String status;

}
