package uk.gov.hmcts.opal.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "v_search_defendant_accounts")
@Immutable
@SuperBuilder
@NoArgsConstructor
public class SearchDefendantAccountEntity {

    @Id
    @Column(name = "defendant_account_id", nullable = false)
    private Long defendantAccountId;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "prosecutor_case_reference")
    private String prosecutorCaseReference;

    @Column(name = "last_enforcement")
    private String lastEnforcement;

    @Column(name = "account_status")
    private String accountStatus;

    @Column(name = "defendant_account_balance")
    private BigDecimal defendantAccountBalance;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "business_unit_id")
    private Long businessUnitId;

    @Column(name = "business_unit_name")
    private String businessUnitName;

    @Column(name = "party_id")
    private Long partyId;

    @Column(name = "organisation")
    private Boolean organisation;

    @Column(name = "organisation_name")
    private String organisationName;

    @Column(name = "address_line_1")
    private String addressLine1;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "title")
    private String title;

    @Column(name = "forenames")
    private String forenames;

    @Column(name = "surname")
    private String surname;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "national_insurance_number")
    private String nationalInsuranceNumber;

    @Column(name = "parent_guardian_surname")
    private String parentGuardianSurname;

    @Column(name = "parent_guardian_forenames")
    private String parentGuardianForenames;

    // --- Flattened alias columns from v_search_defendant_accounts (may be null) ---
    @Column(name = "alias1")
    private String alias1;

    @Column(name = "alias2")
    private String alias2;

    @Column(name = "alias3")
    private String alias3;

    @Column(name = "alias4")
    private String alias4;

    @Column(name = "alias5")
    private String alias5;
}

