package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import static uk.gov.hmcts.opal.entity.interfacejob.InterfaceJobStoredProcedureNames.BUSINESS_UNIT_ID;
import static uk.gov.hmcts.opal.entity.interfacejob.InterfaceJobStoredProcedureNames.DB_PROC_NAME;
import static uk.gov.hmcts.opal.entity.interfacejob.InterfaceJobStoredProcedureNames.INTERFACE_JOB_ID;
import static uk.gov.hmcts.opal.entity.interfacejob.InterfaceJobStoredProcedureNames.JPA_PROC_NAME;
import static uk.gov.hmcts.opal.entity.interfacejob.InterfaceJobStoredProcedureNames.POSTED_BY;
import static uk.gov.hmcts.opal.entity.interfacejob.InterfaceJobStoredProcedureNames.POSTED_BY_NAME;
import static uk.gov.hmcts.opal.entity.interfacejob.InterfaceJobStoredProcedureNames.TILL_ID;
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
import jakarta.persistence.NamedStoredProcedureQuery;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.StoredProcedureParameter;
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
@NamedStoredProcedureQuery(name = JPA_PROC_NAME, procedureName = DB_PROC_NAME, parameters = {
    @StoredProcedureParameter(mode = ParameterMode.IN, name = INTERFACE_JOB_ID, type = Long.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = BUSINESS_UNIT_ID, type = Short.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = POSTED_BY, type = String.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = POSTED_BY_NAME, type = String.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = TILL_ID, type = Long.class)
})
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
