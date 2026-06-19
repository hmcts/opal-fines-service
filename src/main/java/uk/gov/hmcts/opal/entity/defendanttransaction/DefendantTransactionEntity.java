package uk.gov.hmcts.opal.entity.defendanttransaction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.converter.AssociatedRecordTypeConverter;
import uk.gov.hmcts.opal.entity.converter.DefendantTransactionTypeConverter;
import uk.gov.hmcts.opal.entity.converter.DefendantTransactionWriteOffCodeConverter;
import uk.gov.hmcts.opal.util.LocalDateAdapter;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

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

    @Column(name = "defendant_account_id", nullable = false)
    private Long defendantAccountId;

    @Column(name = "posted_date", nullable = false)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate postedDate;

    @Column(name = "posted_by")
    private String postedBy;

    @Column(name = "transaction_type", length = 100)
    @ColumnTransformer(read = "transaction_type::text", write = "?::t_defendant_transaction_type_enum")
    @Convert(converter = DefendantTransactionTypeConverter.class)
    private DefendantTransactionType transactionType;

    @Column(name = "transaction_amount", precision = 18, scale = 2)
    private BigDecimal transactionAmount;

    @Column(name = "payment_method", length = 2)
    @Enumerated(EnumType.STRING)
    private DefendantTransactionPaymentMethod paymentMethod;

    @Column(name = "payment_reference", length = 10)
    private String paymentReference;

    @Column(name = "text", length = 50)
    private String text;

    @Column(name = "status", length = 1)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private DefendantTransactionStatus status;

    @Column(name = "status_date", nullable = false)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime statusDate;

    @Column(name = "status_amount", precision = 18, scale = 2)
    private BigDecimal statusAmount;

    @Column(name = "write_off_code", length = 6)
    @Convert(converter = DefendantTransactionWriteOffCodeConverter.class)
    private DefendantTransactionWriteOffCode writeOffCode;

    @Convert(converter = AssociatedRecordTypeConverter.class)
    @ColumnTransformer(read = "associated_record_type::text", write = "?::t_associated_record_type_enum")
    @Column(name = "associated_record_type", length = 30, columnDefinition = "t_associated_record_type_enum")
    private AssociatedRecordType associatedRecordType;

    @Column(name = "associated_record_id", length = 30)
    private String associatedRecordId;

    @Column(name = "imposed_amount", precision = 18, scale = 2)
    private BigDecimal imposedAmount;

    @Column(name = "posted_by_name", length = 100)
    private String postedByUsername;

}
