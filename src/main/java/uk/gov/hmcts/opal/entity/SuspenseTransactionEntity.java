package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
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
@Table(name = "suspense_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "suspenseTransactionId")
public class SuspenseTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suspense_transaction_id_seq_generator")
    @SequenceGenerator(name = "suspense_transaction_id_seq_generator", sequenceName = "suspense_transaction_id_seq",
        allocationSize = 1)
    @Column(name = "suspense_transaction_id", nullable = false)
    private Long suspenseTransactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suspense_item_id", nullable = false)
    private SuspenseItemEntity suspenseItem;

    @Column(name = "posted_date", nullable = false)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime postedDate;

    @Column(name = "posted_by", length = 20)
    private String postedBy;

    @Column(name = "posted_by_name", length = 100)
    private String postedByName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "transaction_type", length = 2, nullable = false,
        columnDefinition = "t_suspense_transaction_type_enum")
    private SuspenseTransactionType transactionType;

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Convert(converter = AssociatedRecordTypeConverter.class)
    @ColumnTransformer(write = "?::t_associated_record_type_enum")
    @Column(name = "associated_record_type", length = 30, columnDefinition = "t_associated_record_type_enum")
    private AssociatedRecordType associatedRecordType;

    @Column(name = "associated_record_id", length = 30)
    private String associatedRecordId;

    @Column(name = "text", columnDefinition = "text")
    private String text;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "reversed", length = 1, columnDefinition = "t_reversed_enum")
    private SuspenseTransactionReversalStatus reversed;
}
