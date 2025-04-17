package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccountPartiesEntityFull;
import uk.gov.hmcts.opal.util.LocalDateAdapter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "parties")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    @Column(name = "initials", length = 2)
    private String initials;

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
    private LocalDate dateOfBirth;

    @Column(name = "age")
    private Short age;

    @Column(name = "national_insurance_number", length = 20)
    private String niNumber;

    @Column(name = "last_changed_date")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime lastChangedDate;

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<DefendantAccountPartiesEntityFull> defendantAccounts;
}
