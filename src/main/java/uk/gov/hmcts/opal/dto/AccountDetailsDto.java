package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    //defendant_accounts.defendant_account_id
    @JsonProperty("defendant_account_id")
    private Long defendantAccountId;

    //defendant_accounts.account_number
    @JsonProperty("account_number")
    private String accountNumber;

    // parties.surname, parties.forenames, parties.title
    @JsonProperty("full_name")
    private String fullName;

    // business_units.business_unit_name
    @JsonProperty("account_ct")
    private String accountCT;

    // business_units.business_unit_id
    @JsonProperty("business_unit_id")
    private Short businessUnitId;

    //parties.address_line_(*)
    private String address;

    //parties.postcode
    @JsonProperty("post_code")
    private String postCode;

    //parties.birth_date
    private LocalDate dob;

    //defendant_accounts.last_changed_date
    @JsonProperty("details_changed")
    private LocalDate detailsChanged;

    //defendant_accounts.last_hearing_date + defendant_accounts.last_hearing_court_id
    @JsonProperty("last_court_app_and_court_code")
    private String lastCourtAppAndCourtCode;

    //defendant_accounts.last_movement_date
    @JsonProperty("last_movement")
    private LocalDate lastMovement;

    //notes.note_text = AC
    @JsonProperty("comment_field")
    private List<String> commentField;

    //notes.note_text = AA
    @JsonProperty("account_notes")
    private String accountNotes;

    //defendant_accounts.prosecutor_case_reference
    private String pcr;

    //payment_terms.installment_amount / payment_terms.installment_period
    @JsonProperty("payment_details")
    private String paymentDetails;

    //payment_terms.instalment_lump_sum
    @JsonProperty("lump_sum")
    private BigDecimal lumpSum;

    //payment_terms.effective_date
    private LocalDate commencing;

    //payment_terms.jail_days
    @JsonProperty("days_in_default")
    private int daysInDefault;

    //defendant_account.imposed_hearing_date
    @JsonProperty("sentenced_date")
    private LocalDate sentencedDate;

    //defendant_accounts.last_enforcement
    @JsonProperty("last_enforcement")
    private String lastEnforcement;

    //defendant_accounts.enf_override_result_id
    private String override;

    //defendant_accounts.enf_override_enforcer_id
    private Short enforcer;

    //defendant_accounts.enforcing_court_id
    @JsonProperty("enforcement_court")
    private int enforcementCourt;

    //defendant_accounts.amount_imposed
    private BigDecimal imposed;

    //defendant_accounts.amount_paid
    @JsonProperty("amount_paid")
    private BigDecimal amountPaid;

    //defendant_accounts.account_balance
    private BigDecimal balance;
}
