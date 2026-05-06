package uk.gov.hmcts.opal.dto.report;

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

    /**
     * See <a
     * href="https://centralgovernmentcgi.atlassian.net/wiki/spaces/LGR/pages/4606787585/F133+-+List+Fines+Summary+CSV">specification
     * page</a>
     */

    private String header1;                 // fixed literal "DETAIL"
    private String company;                 // parties.organisation -> "Y"/"N"
    private String defendantName;           // formatted surname, forenames or organisation_name (truncated 34)
    private String accountNo;               // defendant_accounts.account_number
    private LocalDate dateOfBirth;          // parties.birth_date
    private String nationalInsuranceNo;     // parties.national_insurance_number
    private String collectionOrder;         // defendant_accounts.collection_order ("Y"/"N"/null)
    private LocalDate lastMovementDate;     // defendant_accounts.last_movement_date
    private BigDecimal amountImposed;       // defendant_accounts.amount_imposed
    private BigDecimal amountPaid;          // defendant_accounts.amount_paid
    private BigDecimal balance;             // defendant_accounts.account_balance
    private String address1;                // parties.address_line_1
    private String address2;                // parties.address_line_2
    private String address3;                // parties.address_line_3
    private String postcode;                // parties.postcode
    private String imposingCourt;           // derived/imposing court string
    private String mobTel;                  // parties.telephone_mobile
    private String homeTel;                 // parties.telephone_home
    private String businessTel;             // parties.telephone_business
    private String vehicleReg;              // debtor_detail.vehicle_registration
    private String vehicleMake;             // debtor_detail.vehicle_make
    private String email1;                  // parties.email_1 (email-1)
    private String email2;                  // parties.email_2 (email-2)
    private String employeeRef;             // debtor_detail.employee_reference
    private String employerName;            // debtor_detail.employer_name
    private String employerAddress1;        // debtor_detail.employer_address_line_1
    private String employerAddress2;        // debtor_detail.employer_address_line_2
    private String employerAddress3;        // debtor_detail.employer_address_line_3
    private String employerAddress4;        // debtor_detail.employer_address_line_4
    private String employerAddress5;        // debtor_detail.employer_address_line_5
    private String employerPostcode;        // debtor_detail.employer_postcode
    private String employerTel;             // debtor_detail.employer_telephone
    private String employerEmail;           // debtor_detail.employer_email
    private String lastEnforcement;         // defendant_accounts.last_enforcement (code)
    private String enforcementReason;       // enforcements.reason (text)
    private LocalDate lastEnforcementDate;  // enforcements.posted_date (last enforcement)
    private LocalDate earliestReleaseDate;
    private String user;                    // users.username who applied the last enforcement (or 'auto')
    private String enforcingCourtCode;      // courts.court_code (enforcing court code)
    private String warrantRef;              // enforcements.warrant_reference
    private Integer jailDays;               // payment_terms.jail_days
    private String parentOrGuardian;        // Parent or Guardian is to Pay 'Y'/'N' (PG)
    private String prosecutorCaseReference; // defendant_accounts.prosecutor_case_reference

}