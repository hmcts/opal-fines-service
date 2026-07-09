package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus;
import uk.gov.hmcts.opal.service.report.ReportError;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

@Entity
@Table(name = "report_instances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = PropertyGenerator.class, property = "reportInstanceId")
public class ReportInstanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_instance_id_seq_generator")
    @SequenceGenerator(name = "report_instance_id_seq_generator", sequenceName = "report_instance_id_seq",
        allocationSize = 1)
    @Column(name = "report_instance_id", nullable = false)
    private Long reportInstanceId;

    @ManyToOne
    @JoinColumn(name = "report_id", nullable = false)
    private ReportEntity report;

    @Column(name = "business_unit_id")
    private List<Short> businessUnit;

    @Column(name = "audit_sequence", nullable = false)
    private Long auditSequence;

    @Column(name = "created_timestamp")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime createdTimestamp;

    @Column(name = "requested_by")
    private Long requestedBy;

    @Column(name = "requested_by_name", length = 100, nullable = false)
    private String requestedByName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "report_parameters", columnDefinition = "json", nullable = false)
    private String reportParameters;

    @Column(name = "location", length = 50)
    private String location;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "generation_status", nullable = false)
    private ReportInstanceGenerationStatus generationStatus;

    @Column(name = "scheduled_deletion_timestamp")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime scheduledDeletionTimestamp;

    @Column(name = "report_name", length = 250)
    private String reportName;

    @Column(name = "no_of_records")
    private Long noOfRecords;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "errors", columnDefinition = "json")
    private ReportError errors;

    public String getReportId() {
        return report.getReportId();
    }
}
