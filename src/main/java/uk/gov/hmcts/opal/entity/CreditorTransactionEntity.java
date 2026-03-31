package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.gov.hmcts.opal.entity.converter.AssociatedRecordTypeConverter;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "creditor_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "creditor_transactionId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CreditorTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "creditor_transaction_id_seq_generator")
    @SequenceGenerator(name = "creditor_transaction_id_seq_generator", sequenceName = "creditor_transaction_id_seq",
        allocationSize = 1)
    @Column(name = "creditor_transaction_id", nullable = false)
    private Long creditorTransactionId;

    @Column(name = "creditor_account_id", nullable = false)
    private Long creditorAccountId;

    @Column(name = "posted_date", nullable = false)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime postedDate;

    @Column(name = "posted_by", length = 20)
    private String postedBy;

    @Column(name = "posted_by_name", nullable = false)
    private String postedByName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "transaction_type", length = 6, nullable = false,
        columnDefinition = "t_creditor_transaction_type_enum")
    private CreditorTransactionType transactionType;

    @Column(name = "transaction_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal transactionAmount;

    @Column(name = "imposition_result_id", length = 10)
    private String impositionResultId;

    @Column(name = "payment_processed")
    private boolean paymentProcessed;

    @Column(name = "payment_reference", length = 10)
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", length = 1, columnDefinition = "t_creditor_transaction_status_enum")
    private CreditorTransactionStatus status;

    @Column(name = "status_date")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime statusDate;

    @Convert(converter = AssociatedRecordTypeConverter.class)
    @ColumnTransformer(write = "?::t_associated_record_type_enum")
    @Column(name = "associated_record_type", length = 30, columnDefinition = "t_associated_record_type_enum")
    private AssociatedRecordType associatedRecordType;

    @Column(name = "associated_record_id", length = 30)
    private String associatedRecordId;
}
