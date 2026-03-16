package uk.gov.hmcts.opal.service.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnforcementReportRowDto {

    private String header1;           // fixed literal "DETAIL"
    private String company;           // parties.organisation -> "Y"/"N"
    private String defname;           // formatted surname, forenames or organisation_name (truncated 34)
    private String accountNo;         // defendant_accounts.account_number
    private LocalDate dob;            // parties.birth_date
    private String nino;              // parties.national_insurance_number
    private String co;                // defendant_accounts.collection_order ("Y"/"N"/null)
    private LocalDate dlmove;         // defendant_accounts.last_movement_date
    private BigDecimal imposed;       // defendant_accounts.amount_imposed
    private BigDecimal paidsf;        // defendant_accounts.amount_paid
    private BigDecimal balance;       // defendant_accounts.account_balance
    private String address1;         // parties.address_line_1
    private String address2;         // parties.address_line_2
    private String address3;         // parties.address_line_3
    private String postcode;         // parties.postcode
    private String imposingCourt;    // derived/imposing court string
    private String mobtel;           // parties.telephone_mobile
    private String hometel;          // parties.telephone_home
    private String bustel;           // parties.telephone_business
    private String vehicleReg;       // debtor_detail.vehicle_registration
    private String vehicleMake;      // debtor_detail.vehicle_make
    private String email1;           // parties.email_1 (email-1)
    private String email2;           // parties.email_2 (email-2)
    private String empRef;           // debtor_detail.employee_reference
    private String empName;          // debtor_detail.employer_name
    private String empAdd1;          // debtor_detail.employer_address_line_1
    private String empAdd2;          // debtor_detail.employer_address_line_2
    private String empAdd3;          // debtor_detail.employer_address_line_3
    private String empAdd4;          // debtor_detail.employer_address_line_4
    private String empAdd5;          // debtor_detail.employer_address_line_5
    private String empPCode;         // debtor_detail.employer_postcode
    private String empTel;           // debtor_detail.employer_telephone
    private String empEmail;         // debtor_detail.employer_email
    private String lastEnf;          // defendant_accounts.last_enforcement (code)
    private LocalDate edrDate;
    private String enfReason;        // enforcements.reason (text)
    private LocalDate leDate;        // enforcements.posted_date (last enforcement)
    private String user;             // users.username who applied the last enforcement (or 'auto')
    private String enfCrt;           // courts.court_code (enforcing court code)
    private String warrNo;           // enforcements.warrant_reference
    private Integer did;            // payment_terms.jail_days
    private String pg;               // Parent or Guardian is to Pay 'Y'/'N' (PG)
    private String pcr;              // defendant_accounts.prosecutor_case_reference

    // SUMMARY totals (single row at end of report)
    private Integer accountsReported;  // count
    private BigDecimal totalImposed;    // sum of amount_imposed
    private BigDecimal totalPaid;       // sum of amount_paid
    private BigDecimal totalBalance;    // sum of account_balance
}