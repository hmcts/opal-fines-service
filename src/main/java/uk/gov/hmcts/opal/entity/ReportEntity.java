package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.entity.converter.DurationToStringConverter;
import uk.gov.hmcts.opal.entity.report.SupportedFileType;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "reportId")
public class ReportEntity {

    @Id
    @Column(name = "report_id", length = 30, nullable = false)
    private String reportId;

    @Column(name = "report_title", length = 50, nullable = false)
    private String reportTitle;

    @Column(name = "report_group", length = 50, nullable = false)
    private String reportGroup;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "supported_file_types", columnDefinition = "r_supported_file_type_enum[]")
    private List<SupportedFileType> supportedFileTypes;

    @Column(name = "audited_report", nullable = false)
    private boolean auditedReport;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "report_parameters", columnDefinition = "json")
    private String reportParameters;

    @Column(name = "supports_multi_bu", nullable = false)
    private boolean supportsMultiBu;

    @Column(name = "is_bespoke_journey", nullable = false)
    private boolean isBespokeJourney;

    @Column(name = "shown_as_worklist", nullable = false)
    private boolean shownAsWorklist;

    @Convert(converter = DurationToStringConverter.class)
    @Column(name = "retention_period", length = 30)
    private Duration retentionPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission", length = 30)
    private FinesPermission permission;

    @Column(name = "can_manually_create", nullable = false)
    private boolean canManuallyCreate;

}
