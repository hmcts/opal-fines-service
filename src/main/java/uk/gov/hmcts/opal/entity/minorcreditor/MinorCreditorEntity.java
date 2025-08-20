package uk.gov.hmcts.opal.entity.minorcreditor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Immutable;

@Getter
@Entity
@Table(name = "v_search_minor_creditor_accounts")
@Immutable
@SuperBuilder
@NoArgsConstructor
public class MinorCreditorEntity {

    @Id
    @Column(name = "creditor_account_id")
    private long creditorId;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "business_unit_id")
    private short businessUnitId;

    @Column(name = "business_unit_name")
    private String businessUnitName;

    @Column(name = "party_id")
    private long partyId;

    @Column(name = "organisation")
    private boolean organisation;

    @Column(name = "address_line_1")
    private String addressLine1;

    @Column(name = "postcode")
    private String postCode;

    @Column(name = "forenames")
    private String forenames;

    @Column(name = "surname")
    private String surname;

    @Column(name = "defendant_account_id")
    private long defendantAccountId;

    @Column(name = "defendant_organisation_name")
    private String defendantOrganisationName;

    @Column(name = "defendant_forenames")
    private String defendantFornames;

    @Column(name = "defendant_surname")
    private String defendantSurname;

    @Column(name = "creditor_account_balance")
    private int creditorAccountBalance;

}
