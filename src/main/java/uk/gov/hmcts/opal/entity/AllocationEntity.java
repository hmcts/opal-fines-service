package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Convert;
import org.hibernate.annotations.ColumnTransformer;
import uk.gov.hmcts.opal.entity.converter.TransactionTypeConverter;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "allocations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocationEntity {

    @Id
    @Column(name = "allocation_id")
    private Long allocationId;

    @ManyToOne
    @JoinColumn(name = "imposition_id", nullable = false)
    private ImpositionEntity imposition;

    @Column(name = "allocated_date", nullable = false)
    private LocalDateTime allocatedDate;

    @Column(name = "allocated_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal allocatedAmount;

    @Convert(converter = TransactionTypeConverter.class)
    @ColumnTransformer(write = "?::t_allocation_transaction_type_enum")
    @Column(name = "transaction_type", nullable = false, length = 10,
        columnDefinition = "t_allocation_transaction_type_enum")
    private TransactionType transactionType;

    @Column(name = "allocation_function", nullable = false, length = 30)
    private String allocationFunction;

    @ManyToOne
    @JoinColumn(name = "defendant_transaction_id")
    private DefendantTransactionEntity defendantTransaction;
}
