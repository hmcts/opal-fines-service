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
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_transfers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AccountTransferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_transfer_id_seq_generator")
    @SequenceGenerator(name = "account_transfer_id_seq_generator", sequenceName = "account_transfer_id_seq",
        allocationSize = 1)
    @Column(name = "account_transfer_id", nullable = false)
    private Long accountTransferId;

    @ManyToOne
    @JoinColumn(name = "business_unit_id", referencedColumnName = "business_unit_id", nullable = false)
    private BusinessUnitFullEntity businessUnit;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "defendant_account_id", nullable = false)
    private DefendantAccountEntity defendantAccount;

    @Column(name = "initiated_date")
    @Temporal(TemporalType.TIMESTAMP)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime initiatedDate;

    @Column(name = "initiated_by", length = 20)
    private String initiatedBy;

    @Column(name = "printed_date")
    @Temporal(TemporalType.TIMESTAMP)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime printedDate;

    @Column(name = "printed_by", length = 20)
    private String printedBy;

    @Column(name = "document_instance_id")
    private Long documentInstanceId;

    @Column(name = "destination_lja_id", nullable = false)
    private Short destinationLjaId;

    @Column(name = "reason", length = 100)
    private String reason;

    @Column(name = "reminder_date")
    @Temporal(TemporalType.TIMESTAMP)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime reminderDate;

}
