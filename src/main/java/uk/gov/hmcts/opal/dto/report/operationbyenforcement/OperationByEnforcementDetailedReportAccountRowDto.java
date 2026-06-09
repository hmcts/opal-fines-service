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
public class OperationByEnforcementDetailedReportAccountRowDto extends OperationByEnforcementReportBaseRowDto {

    /**
     * See <a
     * href="https://centralgovernmentcgi.atlassian.net/wiki/spaces/LGR/pages/4552589335/F135+-+List+Fines+Detail">
     * specification page</a>.
     */

    private LocalDate dateOfHearing;                //imposed hearing date
    private String paymentTerms;                    //payment_terms.effective_date or formatted instalments
    private BigDecimal arrearsTotal;                //mis_debtors.arrears
    private BigDecimal fineImpositions;             //total of all fines impositions (type FO from account)
    private BigDecimal costImpositions;             //total of all cost impositions (type FCOST and FCPC from account)
    private BigDecimal compensationImpositions;     //total of all compensation impositions (type FCOMP from account)
    private BigDecimal criminalCourtsChargeImpositions;//total of all Criminal Courts Charge impositions (type FCC)
    private BigDecimal victimSurchargeImpositions;   //total of all victim surcharge impositions(type FVS from account)
    private BigDecimal otherImpositions;            //total of all other impositions taken from account

}
