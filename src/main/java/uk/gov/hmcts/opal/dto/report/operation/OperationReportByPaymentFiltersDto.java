package uk.gov.hmcts.opal.dto.report.operation;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.dto.ResultId;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OperationReportByPaymentFiltersDto extends OperationReportFiltersDto {

    private Boolean isPaymentMade;
    private PaymentReportMode reportMode;
    private ResultId sinceLastEnforcementAction;
    private LocalDate sinceDate;

}