package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "parties")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyEntity {

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
    private LocalDate dateOfBirth;

    @Column(name = "age")
    private Short age;

    @Column(name = "national_insurance_number", length = 20)
    private String niNumber;

    @Column(name = "last_changed_date")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime lastChangedDate;

    @Column(name = "account_number", length = 20)
    private String accountNo;

    @Column(name = "amount_imposed", precision = 18, scale = 2)
    private BigDecimal amountImposed;

}
