package uk.gov.hmcts.opal.dto.report.operation;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.service.report.ReportEnforcementMode;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OperationReportByEnforcementFiltersDto extends OperationReportFiltersDto {

    private ReportEnforcementMode reportEnforcementMode;
    private LocalDate enforcementDateFrom;
    private LocalDate enforcementDateTo;
    private LocalDate lastActionDateFrom;
    private LocalDate lastActionDateTo;
    private LocalDate regfDateFrom;
    private LocalDate regfDateTo;
    private String enforcementAction;


}