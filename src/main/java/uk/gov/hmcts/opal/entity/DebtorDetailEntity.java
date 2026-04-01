package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "debtor_detail")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DebtorDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "party_id")
    private Long partyId;

    @Column(name = "vehicle_make")
    private String vehicleMake;

    @Column(name = "vehicle_registration")
    private String vehicleRegistration;

    @Column(name = "employer_name")
    private String employerName;

    @Column(name = "employer_address_line_1")
    private String employerAddressLine1;

    @Column(name = "employer_address_line_2")
    private String employerAddressLine2;

    @Column(name = "employer_address_line_3")
    private String employerAddressLine3;

    @Column(name = "employer_address_line_4")
    private String employerAddressLine4;

    @Column(name = "employer_address_line_5")
    private String employerAddressLine5;

    @Column(name = "employer_postcode")
    private String employerPostcode;

    @Column(name = "employee_reference")
    private String employeeReference;

    @Column(name = "employer_telephone")
    private String employerTelephone;

    @Column(name = "employer_email")
    private String employerEmail;

    @Column(name = "document_language")
    private String documentLanguage;

    @Column(name = "document_language_date")
    @Temporal(TemporalType.DATE)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDate documentLanguageDate;

    @Column(name = "hearing_language")
    private String hearingLanguage;

    @Column(name = "hearing_language_date")
    @Temporal(TemporalType.DATE)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDate hearingLanguageDate;
}
