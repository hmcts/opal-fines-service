package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "v_defendant_accounts_header")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefendantAccountHeaderViewEntity {

    @Id
    @Column(name = "defendant_account_id")
    private Long defendantAccountId;

    @Column(name = "version_number")
    private Long version;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "prosecutor_case_reference")
    private String prosecutorCaseReference;

    @Column(name = "account_status")
    private String accountStatus;

    @Column(name = "account_type")
    private String accountType;

    @Column(name = "paid_written_off")
    private BigDecimal paid;

    @Column(name = "account_balance")
    private BigDecimal accountBalance;

    @Column(name = "amount_imposed")
    private BigDecimal imposed;

    @Column(name = "arrears")
    private BigDecimal arrears;

    @Column(name = "defendant_account_party_id")
    private Long defendantAccountPartyId;

    @Column(name = "debtor_type")
    private String debtorType;

    @Column(name = "party_id")
    private Long partyId;

    @Column(name = "title")
    private String title;

    @Column(name = "forenames")
    private String firstnames;

    @Column(name = "surname")
    private String surname;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "organisation")
    private Boolean organisation;

    @Column(name = "organisation_name")
    private String organisationName;

    @Column(name = "business_unit_id")
    private Short businessUnitId;

    @Column(name = "business_unit_name")
    private String businessUnitName;

    @Column(name = "business_unit_code")
    private String businessUnitCode;

    @Column(name = "ticket_number")
    private String fixedPenaltyTicketNumber;

    @Column(name = "parent_guardian_account_party_id")
    private Long parentGuardianAccountPartyId;

    @Column(name = "has_parent_guardian")
    private Boolean hasParentGuardian;

    @Column(name = "parent_guardian_debtor_type")
    private String parentGuardianDebtorType;
}
