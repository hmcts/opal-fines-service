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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "control_totals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "controlTotalId")
public class ControlTotalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "control_total_id_seq_generator")
    @SequenceGenerator(name = "control_total_id_seq_generator", sequenceName = "control_total_id_seq",
        allocationSize = 1)
    @Column(name = "control_total_id", nullable = false)
    private Long controlTotalId;

    @ManyToOne
    @JoinColumn(name = "business_unit_id", nullable = false)
    private BusinessUnitEntity businessUnit;

    @Column(name = "item_number", nullable = false)
    private Short itemNumber;

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "associated_record_type", length = 30)
    private String associatedRecordType;

    @Column(name = "associated_record_id", length = 30)
    private String associatedRecordId;

    @Column(name = "ct_report_instance_id")
    private Long ctReportInstanceId;

    @Column(name = "qe_report_instance_id")
    private Long qeReportInstanceId;
}
