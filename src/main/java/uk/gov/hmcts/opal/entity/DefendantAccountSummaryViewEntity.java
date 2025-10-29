package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import org.hibernate.annotations.Immutable;
import uk.gov.hmcts.opal.util.Versioned;

@Entity
@Immutable
@Table(name = "v_defendant_accounts_summary")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefendantAccountSummaryViewEntity implements Versioned {

    @Id
    @Column(name = "defendant_account_id", nullable = false)
    private Long defendantAccountId;

    @Version
    @Column(name = "version_number")
    private Long version;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "debtor_type", nullable = false)
    private String debtorType;

    @Column(name = "last_enforcement", nullable = false)
    private String lastEnforcement;

    @Column(name = "last_enf_title", nullable = false)
    private String lastEnforcementTitle;

    @Column(name = "alias_1")
    private String alias1;

    @Column(name = "alias_2")
    private String alias2;

    @Column(name = "alias_3")
    private String alias3;

    @Column(name = "alias_4")
    private String alias4;

    @Column(name = "alias_5")
    private String alias5;

    @Column(name = "collection_order")
    private Boolean collectionOrder;

    @Column(name = "account_comments")
    private String accountComments;

    @Column(name = "account_note_1")
    private String accountNote1;

    @Column(name = "account_note_2")
    private String accountNote2;

    @Column(name = "account_note_3")
    private String accountNote3;

    // Enforcement Status

    @Column(name = "jail_days")
    private Integer jailDays;

    @Column(name = "enf_override_result_id")
    private String enforcementOverrideResultId;

    @Column(name = "enforcer_id")
    private Long enforcerId;

    @Column(name = "enforcer_name")
    private String enforcerName;

    @Column(name = "lja_id")
    private String ljaId;

    @Column(name = "lja_name")
    private String ljaName;

    @Column(name = "enf_override_title")
    private String enforcementOverrideTitle;

    @Column(name = "last_movement_date")
    private LocalDateTime lastMovementDate;

    @Column(name = "document_language")
    private String documentLanguage;

    @Column(name = "hearing_language")
    private String hearingLanguage;

    // Party Details

    @Column(name = "party_id")
    private Long partyId;

    @Column(name = "title")
    private String title;

    @Column(name = "forenames")
    private String forenames;

    @Column(name = "surname")
    private String surname;

    @Column(name = "birth_date")
    private LocalDateTime birthDate;

    @Column(name = "age")
    private Integer age;

    @Column(name = "organisation")
    private Boolean organisation;

    @Column(name = "organisation_name")
    private String organisationName;

    @Column(name = "address_line_1")
    private String addressLine1;

    @Column(name = "address_line_2")
    private String addressLine2;

    @Column(name = "address_line_3")
    private String addressLine3;

    @Column(name = "address_line_4")
    private String addressLine4;

    @Column(name = "address_line_5")
    private String addressLine5;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "national_insurance_number")
    private String nationalInsuranceNumber;

    @Column(name = "terms_type_code")
    private String termsTypeCode;

    @Column(name = "instalment_period")
    private String instalmentPeriod;

    @Column(name = "instalment_amount")
    private BigDecimal instalmentAmount;

    @Column(name = "instalment_lump_sum")
    private BigDecimal instalmentLumpSum;

    @Column(name = "effective_date")
    private LocalDateTime effectiveDate;
}
