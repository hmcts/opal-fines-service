package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.util.LocalDateAdapter;

import java.time.LocalDate;

@Entity
@Table(name = "fixed_penalty_offences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FixedPenaltyOffenceEntity {

    @Id
    @Column(name = "defendant_account_id", nullable = false)
    private Long defendantAccountId;

    @Column(name = "ticket_number", length = 120, nullable = false)
    private String ticketNumber;

    @Column(name = "vehicle_registration", length = 10)
    private String vehicleRegistration;

    @Column(name = "offence_location", length = 30)
    private String offenceLocation;

    @Column(name = "notice_number", length = 10)
    private String noticeNumber;

    @Column(name = "issued_date")
    @Temporal(TemporalType.DATE)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate issuedDate;

    @Column(name = "licence_number", length = 20)
    private String licenceNumber;

    @Column(name = "vehicle_fixed_penalty")
    private Boolean vehicleFixedPenalty;

    @Column(name = "offence_time")
    private String timeOfOffence;

}
