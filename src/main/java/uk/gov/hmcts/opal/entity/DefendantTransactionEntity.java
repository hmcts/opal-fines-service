package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "defendant_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DefendantTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "defendant_transaction_id_seq_generator")
    @SequenceGenerator(name = "defendant_transaction_id_seq_generator", sequenceName = "defendant_transaction_id_seq",
        allocationSize = 1)
    @Column(name = "defendant_transaction_id", nullable = false)
    private Long defendantTransactionId;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "defendant_account_id", nullable = false)
    private DefendantAccountEntity defendantAccount;

    @Column(name = "posted_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate postedDate;

    @Column(name = "posted_by")
    private String postedBy;

    @Column(name = "transaction_type", length = 100)
    private String transactionType;

    @Column(name = "transaction_amount", precision = 18, scale = 2)
    private BigDecimal transactionAmount;

    @Column(name = "payment_method", length = 2)
    private String paymentMethod;

    @Column(name = "payment_reference", length = 10)
    private String paymentReference;

    @Column(name = "text", length = 50)
    private String text;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "status_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate statusDate;

    @Column(name = "status_amount", precision = 18, scale = 2)
    private BigDecimal statusAmount;

    @Column(name = "write_off_code", length = 6)
    private String writeOffCode;

    @Column(name = "associated_record_type", length = 30)
    private String associatedRecordType;

    @Column(name = "associated_record_id", length = 30)
    private String associatedRecordId;

    @Column(name = "imposed_amount", precision = 18, scale = 2)
    private BigDecimal imposedAmount;

    @Column(name = "posted_by_user_id")
    private Long postedByUserId;

}
