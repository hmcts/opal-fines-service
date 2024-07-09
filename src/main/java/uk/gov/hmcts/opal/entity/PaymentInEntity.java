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
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments_in")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentInEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_in_id_seq_generator")
    @SequenceGenerator(name = "payment_in_id_seq_generator", sequenceName = "payment_in_id_seq", allocationSize = 1)
    @Column(name = "payment_in_id", nullable = false)
    private Long paymentInId;

    @ManyToOne
    @JoinColumn(name = "till_id", referencedColumnName = "till_id", nullable = false)
    private TillEntity tillEntity;

    @Column(name = "payment_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal paymentAmount;

    @Column(name = "payment_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime paymentDate;

    @Column(name = "payment_method", length = 2, nullable = false)
    private String paymentMethod;

    @Column(name = "destination_type", length = 1, nullable = false)
    private String destinationType;

    @Column(name = "allocation_type", length = 20)
    private String allocationType;

    @Column(name = "associated_record_type", length = 30)
    private String associatedRecordType;

    @Column(name = "associated_record_id", length = 30)
    private String associatedRecordId;

    @Column(name = "third_party_payer_name", length = 30)
    private String thirdPartyPayerName;

    @Column(name = "additional_information", length = 500)
    private String additionalInformation;

    @Column(name = "receipt")
    private boolean receipt;

    @Column(name = "allocated")
    private boolean allocated;

}
