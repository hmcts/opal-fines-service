package uk.gov.hmcts.opal.dto.report.operationbyenforcement;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OperationByEnforcementSummaryReportRowDto extends OperationByEnforcementReportBaseRowDto {

    /**
     * See <a
     * href="https://centralgovernmentcgi.atlassian.net/wiki/spaces/LGR/pages/4606787585/F133+-+List+Fines+Summary+CSV">specification
     * page</a>.
     */

    private String nationalInsuranceNo;     // parties.national_insurance_number
    private BigDecimal amountPaid;          // defendant_accounts.amount_paid
    private String mobTel;                  // parties.telephone_mobile
    private String homeTel;                 // parties.telephone_home
    private String businessTel;             // parties.telephone_business
    private String vehicleReg;              // debtor_detail.vehicle_registration
    private String vehicleMake;             // debtor_detail.vehicle_make
    private String email1;                  // parties.email_1 (email-1)
    private String email2;                  // parties.email_2 (email-2)
    private String lastEnforcement;         // defendant_accounts.last_enforcement (code)
    private String enforcementReason;       // enforcements.reason (text)
    private LocalDate lastEnforcementDate;  // enforcements.posted_date (last enforcement)
    private LocalDate earliestReleaseDate;
    private String user;                    // users.username who applied the last enforcement (or 'auto')
    private String enforcingCourtCode;      // courts.court_code (enforcing court code)
    private String warrantRef;              // enforcements.warrant_reference
    private Integer jailDays;               // payment_terms.jail_days
    private String parentOrGuardian;        // Parent or Guardian is to Pay 'Y'/'N' (PG)

}
