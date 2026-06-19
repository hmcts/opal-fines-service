package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.converter.AssociatedRecordTypeConverter;

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
    private BusinessUnitEntity businessUnit;

    @Column(name = "report_id")
    private String reportId;

    @Column(name = "entry_timestamp")
    private LocalDateTime entryTimestamp;

    @Column(name = "reported_timestamp")
    private LocalDateTime reportedTimestamp;

    @Convert(converter = AssociatedRecordTypeConverter.class)
    @ColumnTransformer(write = "?::t_associated_record_type_enum")
    @Column(name = "associated_record_type", length = 30, columnDefinition = "t_associated_record_type_enum")
    private AssociatedRecordType associatedRecordType;

    @Column(name = "associated_record_id", length = 30)
    private String associatedRecordId;

}
