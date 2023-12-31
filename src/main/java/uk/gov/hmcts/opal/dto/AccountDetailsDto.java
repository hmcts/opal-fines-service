package uk.gov.hmcts.opal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailsDto implements ToJsonString {

    //defendant_accounts.account_number
    private String accountNumber;

    // parties.surname, parties,initials, parties.forenames, parties.title
    private String fullName;

    // business_units.business_unit_type
    private String accountCT;

    //parties.account_type
    private String accountType;

    //parties.address_line_(*)
    private String address;

    //parties.postcode
    private String postCode;

    //parties.birth_date
    private LocalDate dob;

    //defendant_accounts.last_changed_date
    private LocalDate detailsChanged;

    //defendant_accounts.last_hearing_date + defendant_accounts.last_hearing_court_id
    private String lastCourtAppAndCourtCode;

    //defendant_accounts.last_movement_date
    private LocalDate lastMovement;

    //notes.note_text
    private List<String> commentField;

    //defendant_accounts.prosecutor_case_reference
    private String pcr;

    //debtor_detail.document_language
    private String documentLanguage;

    //debtor_detail.hearing_language
    private String hearingLanguage;

    //payment_terms.installment_amount / payment_terms.installment_period
    private String paymentDetails;

    //payment_terms.instalment_lump_sum
    private BigDecimal lumpSum;

    //payment_terms.effective_date
    private LocalDate commencing;

    //payment_terms.jail_days
    private int daysInDefault;

    //defendant_accounts.suspended_committal_date
    private LocalDate sentencedDate;

    //defendant_accounts.last_enforcement
    private String lastEnforcement;

    //defendant_accounts.enf_override_result_id
    private String override;

    //defendant_accounts.enf_override_enforcer_id
    private Short enforcer;

    //defendant_accounts.enforcing_court_id
    private int enforcementCourt;

    //defendant_accounts.amount_imposed
    private BigDecimal imposed;

    //defendant_accounts.amount_paid
    private BigDecimal amountPaid;

    //defendant_accounts.account_balance
    private BigDecimal arrears;

    //defendant_accounts.account_balance
    private BigDecimal balance;
}
