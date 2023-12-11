package uk.gov.hmcts.opal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class AccountDetailsDto implements ToJsonString{

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
    private String commentField;

    //defendant_accounts.prosecutor_case_reference
    private String pcr;

    //debtor_detail.document_language
    private String documentLanguage;

    //debtor_detail.hearing_language
    private String hearingLanguage;

    //payment_terms.installment_amount (if payment_terms.terms_type_code == I ) { + payment_terms.instalment_period }
    private Double paymentDetails;

    //to be defined
    //private Double lumpSum;

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
    private String enforcer;

    //defendant_accounts.enforcing_court_id
    private int enforcementCourt;

    //defendant_accounts.amount_imposed
    private Double imposed;

    //defendant_accounts.amount_paid
    private Double amountPaid;

    //to be defined
    //private Double arrears;

    //defendant_accounts.account_balance
    private Double balance;
}
