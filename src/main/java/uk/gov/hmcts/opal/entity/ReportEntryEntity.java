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
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;

@Entity
@Table(name = "report_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "reportEntryId")
public class ReportEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_entry_id_seq_generator")
    @SequenceGenerator(name = "report_entry_id_seq_generator", sequenceName = "report_entry_id_seq", allocationSize = 1)
    @Column(name = "report_entry_id", nullable = false)
    private Long reportEntryId;

    @ManyToOne
    @JoinColumn(name = "business_unit_id")
    private BusinessUnitFullEntity businessUnit;

    @Column(name = "report_id")
    private String reportId;

    @Column(name = "entry_timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime entryTimestamp;

    @Column(name = "reported_timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime reportedTimestamp;

    @Column(name = "associated_record_type", length = 30)
    private String associatedRecordType;

    @Column(name = "associated_record_id", length = 30)
    private String associatedRecordId;

}
