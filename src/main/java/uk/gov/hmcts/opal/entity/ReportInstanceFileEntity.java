package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.gov.hmcts.opal.entity.report.SupportedFileType;

@Entity
@Table(name = "report_instance_files")
@IdClass(ReportInstanceFileEntityId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportInstanceFileEntity {

    @Id
    @Column(name = "report_instance_id", nullable = false)
    private Long reportInstanceId;

    @Id
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "file_type", columnDefinition = "r_supported_file_type_enum", nullable = false)
    private SupportedFileType fileType;

    @Column(name = "location_uuid", nullable = false)
    private UUID locationUuid;

    @Column(name = "created_timestamp", nullable = false)
    private LocalDateTime createdTimestamp;

    @Column(name = "last_accessed_timestamp", nullable = false)
    private LocalDateTime lastAccessedTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_instance_id", nullable = false, insertable = false, updatable = false)
    private ReportInstanceEntity reportInstance;
}
