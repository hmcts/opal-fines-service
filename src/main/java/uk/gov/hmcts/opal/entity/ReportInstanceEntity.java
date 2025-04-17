package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnit;

import java.time.LocalDate;

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

    @ManyToOne
    @JoinColumn(name = "business_unit_id", nullable = false)
    private BusinessUnit.Lite businessUnit;

    @Column(name = "audit_sequence", nullable = false)
    private Long auditSequence;

    @Column(name = "generated_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate generatedDate;

    @Column(name = "generated_by", length = 20, nullable = false)
    private String generatedBy;

    @Column(name = "report_parameters", nullable = false)
    private String reportParameters;

    @Column(name = "content", nullable = false)
    private String content;

}
