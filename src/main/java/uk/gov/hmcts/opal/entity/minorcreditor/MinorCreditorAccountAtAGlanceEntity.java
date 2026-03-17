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
public class MinorCreditorAccountAtAGlanceEntity {

    @Id
    @Column(name = "creditor_account_id")
    private Long creditorId;

    @Column(name = "creditor_account_number")
    private String accountNumber;

    @Column(name = "pay_by_bacs")
    private Boolean payByBacs;

    @Column(name = "version_number")
    private Long versionNumber;

    @Column(name = "hold_payout")
    private Boolean holdPayout;

    @Column(name = "party_id")
    private Long partyId;

    @Column(name = "creditor_title")
    private String creditorTitle;

    @Column(name = "creditor_forenames")
    private String creditorForenames;

    @Column(name = "creditor_surname")
    private String creditorSurname;

    @Column(name = "creditor_organisation")
    private Boolean creditorOrganisation;

    @Column(name = "creditor_organisation_name")
    private String organisationName;

    @Column(name = "creditor_address_line_1")
    private String addressLine1;

    @Column(name = "creditor_address_line_2")
    private String addressLine2;

    @Column(name = "creditor_address_line_3")
    private String addressLine3;

    @Column(name = "creditor_address_line_4")
    private String addressLine4;

    @Column(name = "creditor_address_line_5")
    private String addressLine5;

    @Column(name = "creditor_postcode")
    private String postcode;

    @Column(name = "defendant_account_id")
    private Long defendantAccountId;

    @Column(name = "defendant_account_number")
    private String defendantAccountNumber;

    @Column(name = "defendant_title")
    private String defendantTitle;

    @Column(name = "defendant_forenames")
    private String defendantForenames;

    @Column(name = "defendant_surname")
    private String defendantSurname;

}
