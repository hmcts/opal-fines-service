package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Getter
@Table(name = "v_interface_jobs_processed_file_summary")
public class InterfaceJobProcessedFileSummaryEntity {

    @Id
    @Column(name = "interface_file_id")
    private Long interfaceFileId;

    @Column(name = "interface_job_id")
    private Long interfaceJobId;

    @Column(name = "interface_file_name")
    private String interfaceFileName;

    @Column(name = "source")
    private String source;

    @Column(name = "business_unit_name")
    private String businessUnitName;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "total_records")
    private Short totalRecords;

    @Column(name = "total_errors")
    private Long totalErrors;
}
