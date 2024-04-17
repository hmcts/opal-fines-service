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

import java.time.LocalDateTime;

@Entity
@Table(name = "suspense_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "suspense_itemId")
public class SuspenseItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suspense_item_id_seq_generator")
    @SequenceGenerator(name = "suspense_item_id_seq_generator", sequenceName = "suspense_item_id_seq",
        allocationSize = 1)
    @Column(name = "suspense_item_id", nullable = false)
    private Long suspenseItemId;

    @ManyToOne
    @JoinColumn(name = "suspense_account_id", nullable = false)
    private SuspenseAccountEntity suspenseAccount;

    @Column(name = "suspense_item_number", nullable = false)
    private Short suspenseItemNumber;

    @Column(name = "suspense_item_type", length = 2, nullable = false)
    private String suspenseItemType;

    @Column(name = "created_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdDate;

    @Column(name = "payment_method", length = 2)
    private String paymentMethod;

    @Column(name = "court_fee_id")
    private Long courtFeeId;
}
