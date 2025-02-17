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
@Table(name = "hmrc_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "hmrcRequestId")
public class HmrcRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hmrc_request_id_seq_generator")
    @SequenceGenerator(name = "hmrc_request_id_seq_generator", sequenceName = "hmrc_request_id_seq", allocationSize = 1)
    @Column(name = "hmrc_request_id", nullable = false)
    private Long hmrcRequestId;

    @ManyToOne
    @JoinColumn(name = "business_unit_id", nullable = false)
    private BusinessUnit.Lite businessUnit;

    @Column(name = "uuid", length = 36, nullable = false)
    private String uuid;

    @Column(name = "requested_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate requestedDate;

    @Column(name = "requested_by", nullable = false)
    private Long requestedBy;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "forename", length = 50, nullable = false)
    private String forename;

    @Column(name = "surname", length = 50, nullable = false)
    private String surname;

    @Column(name = "ni_number", length = 12, nullable = false)
    private String niNumber;

    @Column(name = "dob", nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate dob;

    @Column(name = "last_enforcement", length = 24)
    private String lastEnforcement;

    @Column(name = "response_date")
    @Temporal(TemporalType.DATE)
    private LocalDate responseDate;

    @Column(name = "response_data")
    private String responseData;

    @Column(name = "qa_report_data")
    private String qaReportData;

}
