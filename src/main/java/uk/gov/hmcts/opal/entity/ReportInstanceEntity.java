package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus;

@Entity
@Table(name = "report_instances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "reportInstanceId")
public class ReportInstanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_instance_id_seq_generator")
    @SequenceGenerator(name = "report_instance_id_seq_generator", sequenceName = "report_instance_id_seq",
        allocationSize = 1)
    @Column(name = "report_instance_id", nullable = false)
    private Long reportInstanceId;

    @Column(name = "report_id", nullable = false)
    private Long reportId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "business_unit_id", columnDefinition = "smallint[]")
    private Short[] businessUnitId;

    @Column(name = "audit_sequence", nullable = false)
    private Long auditSequence;

    @Column(name = "created_timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdTimestamp;

    @Column(name = "requested_by")
    private Long requestedBy;

    @Column(name = "requested_by_name", length = 100, nullable = false)
    private String requestedByName;

    @Column(name = "report_parameters", nullable = false)
    private String reportParameters;

    @Column(name = "location", length = 30)
    private String location;

    @Column(name = "requested_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime requestedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "generation_status", nullable = false)
    private ReportInstanceGenerationStatus generationStatus;

    @Column(name = "scheduled_deletion_timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime scheduledDeletionTimestamp;

    @Column(name = "report_name", length = 250)
    private String reportName;

    @Column(name = "no_of_records")
    private Short noOfRecords;

    @Column(name = "errors", columnDefinition = "json")
    @ColumnTransformer(write = "?::jsonb")
    @JsonRawValue
    private String errors;

}
