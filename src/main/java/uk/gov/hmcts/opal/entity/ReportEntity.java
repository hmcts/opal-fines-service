package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @Column(name = "report_id", length = 20, nullable = false)
    private String reportId;

    @Column(name = "report_title", length = 50, nullable = false)
    private String reportTitle;

    @Column(name = "report_group", length = 50, nullable = false)
    private String reportGroup;

    //@Column(name = "user_entries", nullable = false)   check if this needs to be deleted
    //private String userEntries;

    @Column(name = "audited_report", nullable = false)
    private String auditedReport;

}
