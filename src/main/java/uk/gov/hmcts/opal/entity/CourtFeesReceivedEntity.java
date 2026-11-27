package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

import java.time.LocalDateTime;

@Entity
@Table(name = "court_fees_received")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "courtFeeReceivedId")
public class CourtFeesReceivedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "court_fee_received_id_seq_generator")
    @SequenceGenerator(name = "court_fee_received_id_seq_generator", sequenceName = "court_fee_received_id_seq",
        allocationSize = 1)
    @Column(name = "court_fee_received_id", nullable = false)
    private Long courtFeeReceivedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_unit_id", nullable = false)
    private BusinessUnitEntity businessUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_fee_id")
    private CourtFeeEntity courtFee;

    @Column(name = "overpayment", nullable = false)
    private boolean overpayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suspense_transaction_id")
    private SuspenseTransactionEntity suspenseTransaction;

    @Column(name = "transferred_date")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime transferredDate;

    @Column(name = "number_of_items", nullable = false)
    private Short numberOfItems;

    @Column(name = "received_date")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime receivedDate;
}
