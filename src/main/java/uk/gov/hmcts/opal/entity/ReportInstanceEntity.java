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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;
import uk.gov.hmcts.opal.service.report.ReportError;
import uk.gov.hmcts.opal.service.report.ReportInstanceGenerationStatus;
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

    @Column(name = "report_id", nullable = false)
    private String reportId;

    @Column(name = "business_unit_id")
    private List<Long> businessUnit;

    @Column(name = "audit_sequence", nullable = false)
    private Long auditSequence;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "report_parameters", columnDefinition = "json", nullable = false)
    private String reportParameters;

    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "generation_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportInstanceGenerationStatus generationStatus;

    @Column(name = "location", length = 30)
    private String location;

    @Column(name = "created_timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime createdTimestamp;

    @Column(name = "scheduled_deletion_timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime scheduledDeletionTimestamp;

    @Column(name = "no_of_records")
    private Short noOfRecords;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "errors", columnDefinition = "json")
    private ReportError errors;
}