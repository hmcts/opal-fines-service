package uk.gov.hmcts.opal.entity.search;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Getter
@Immutable
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
@ToString(callSuper = true)
public abstract class SearchDefendantAccount {

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
    private Short businessUnitId;

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

    @Entity
    @Getter
    @EqualsAndHashCode(callSuper = true)
    @Table(name = "v_search_defendant_accounts")
    @SuperBuilder
    @NoArgsConstructor
    public static class BasicEntity extends SearchDefendantAccount {
    }
}

