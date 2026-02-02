package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.UpdateTimestamp;
import uk.gov.hmcts.opal.util.LocalDateAdapter;

@Entity
@Data
@Table(name = "parties")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "defendantAccounts"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "partyId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PartyEntity implements FullNameBuilder {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "party_id_seq")
    @SequenceGenerator(name = "party_id_seq", sequenceName = "party_id_seq", allocationSize = 1)
    @Column(name = "party_id")
    private Long partyId;

    @Column(name = "organisation")
    private boolean organisation;

    @Column(name = "organisation_name", length = 80)
    private String organisationName;

    @Column(name = "surname", length = 50)
    private String surname;

    @Column(name = "forenames", length = 50)
    private String forenames;

    @Column(name = "title", length = 20)
    private String title;

    @Column(name = "address_line_1", length = 35)
    private String addressLine1;

    @Column(name = "address_line_2", length = 35)
    private String addressLine2;

    @Column(name = "address_line_3", length = 35)
    private String addressLine3;

    @Column(name = "address_line_4", length = 35)
    private String addressLine4;

    @Column(name = "address_line_5", length = 35)
    private String addressLine5;

    @Column(name = "postcode", length = 10)
    private String postcode;

    @Column(name = "account_type", length = 20)
    private String accountType;

    @Column(name = "birth_date")
    @Temporal(TemporalType.DATE)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate birthDate;

    @Column(name = "age")
    private Short age;

    @Column(name = "national_insurance_number", length = 20)
    private String niNumber;

    @Column(name = "telephone_home")
    private String homeTelephoneNumber;

    @Column(name = "telephone_business")
    private String workTelephoneNumber;

    @Column(name = "telephone_mobile")
    private String mobileTelephoneNumber;

    @Column(name = "email_1")
    private String primaryEmailAddress;

    @Column(name = "email_2")
    private String secondaryEmailAddress;

    @Column(name = "last_changed_date")
    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private LocalDateTime lastChangedDate;

    @ToString.Exclude
    @OneToMany(mappedBy = "party")
    private List<DefendantAccountPartiesEntity> defendantAccountParties;

}
