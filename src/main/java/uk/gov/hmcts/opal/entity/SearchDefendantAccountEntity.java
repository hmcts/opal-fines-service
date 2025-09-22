package uk.gov.hmcts.opal.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Entity
@Table(name = "v_search_def_account_and_alias")
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
    private Long lastEnforcement;

    @Column(name = "account_status")
    private String accountStatus;

    @Column(name = "defendant_account_balance")
    private BigDecimal defendantAccountBalance;

    @Column(name = "completed_date")
    private LocalDate completedDate;

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

    // --- Alias columns (LEFT JOIN; may be null) ---
    @Column(name = "alias_sequence_number")
    private Integer aliasSequenceNumber;

    @Column(name = "alias_organisation_name")
    private String aliasOrganisationName;

    @Column(name = "alias_surname")
    private String aliasSurname;

    @Column(name = "alias_forenames")
    private String aliasForenames;
}

