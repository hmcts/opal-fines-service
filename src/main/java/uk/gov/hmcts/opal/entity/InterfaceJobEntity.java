package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;

@Entity
@Table(name = "interface_jobs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InterfaceJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "interface_job_id_seq_generator")
    @SequenceGenerator(name = "interface_job_id_seq_generator",
        sequenceName = "interface_job_id_seq", allocationSize = 1)
    @Column(name = "interface_job_id", nullable = false)
    private Long interfaceJobId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_unit_id")
    private BusinessUnitEntity businessUnit;

    @Column(name = "interface_name", length = 50, nullable = false)
    private String interfaceName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", length = 10, nullable = false, columnDefinition = "t_interface_job_status_enum")
    private InterfaceJobStatus status;

    @Column(name = "created_datetime", nullable = false)
    private LocalDateTime createdDateTime;

    @Column(name = "started_datetime")
    private LocalDateTime startedDateTime;

    @Column(name = "completed_datetime")
    private LocalDateTime completedDateTime;

    @OneToMany(mappedBy = "interfaceJob", fetch = FetchType.LAZY)
    private List<InterfaceFileEntity> interfaceFiles;
}
