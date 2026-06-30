package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.converter.ChequeAllocationTypeConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import uk.gov.hmcts.opal.entity.converter.ChequeStatusTypeConverter;

@Entity
@Table(name = "cheques")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "chequeId")
public class ChequeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cheque_id_seq_generator")
    @SequenceGenerator(name = "cheque_id_seq_generator", sequenceName = "cheque_id_seq", allocationSize = 1)
    @Column(name = "cheque_id", nullable = false)
    private Long chequeId;

    @ManyToOne
    @JoinColumn(name = "business_unit_id", nullable = false)
    private BusinessUnitEntity businessUnit;

    @Column(name = "cheque_number", nullable = false)
    private Long chequeNumber;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "creditor_transaction_id")
    private Long creditorTransactionId;

    @Column(name = "defendant_transaction_id")
    private Long defendantTransactionId;

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Convert(converter = ChequeAllocationTypeConverter.class)
    @ColumnTransformer(write = "?::t_cheque_allocation_type_enum")
    @Column(name = "allocation_type", length = 10, columnDefinition = "t_cheque_allocation_type_enum")
    private ChequeAllocationType allocationType;

    @Column(name = "reminder_date")
    private LocalDate reminderDate;

    @Convert(converter = ChequeStatusTypeConverter.class)
    @ColumnTransformer(write = "?::t_cheque_status_enum")
    @Column(name = "status", length = 1, nullable = false, columnDefinition = "t_cheque_status_enum")
    private ChequeStatusType status;

}
