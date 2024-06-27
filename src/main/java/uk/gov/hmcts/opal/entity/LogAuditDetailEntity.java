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

import java.time.LocalDateTime;

@Entity
@Table(name = "log_audit_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LogAuditDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "log_audit_detail_id_seq_generator")
    @SequenceGenerator(name = "log_audit_detail_id_seq_generator", sequenceName = "log_audit_detail_id_seq",
        allocationSize = 1)
    @Column(name = "log_audit_detail_id", nullable = false)
    private Long logAuditDetailId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "log_timestamp", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime logTimestamp;

    @ManyToOne
    @JoinColumn(name = "log_action_id", updatable = false)
    private LogActionEntity logAction;

    @Column(name = "account_number", length = 20)
    private String accountNumber;

    @ManyToOne
    @JoinColumn(name = "business_unit_id", updatable = false)
    private BusinessUnitEntity businessUnit;

    @Column(name = "json_request", nullable = false)
    private String jsonRequest;

}
