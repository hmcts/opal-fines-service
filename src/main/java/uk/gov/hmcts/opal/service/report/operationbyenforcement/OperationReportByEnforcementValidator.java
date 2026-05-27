package uk.gov.hmcts.opal.service.report.operationbyenforcement;

import static uk.gov.hmcts.opal.service.report.ReportEnforcementMode.LAST_ACTION;
import static uk.gov.hmcts.opal.service.report.ReportEnforcementMode.REGF;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationReportByEnforcementFiltersDto;

@Component
public class OperationReportByEnforcementValidator {

    public void validate(OperationReportByEnforcementFiltersDto filters) {
        List<String> validationErrors = new ArrayList<>();
        validateLastActionFilters(filters, validationErrors);
        validateRegfFilters(filters, validationErrors);

        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException(
                String.join("; ", validationErrors)
            );
        }
    }

    private void validateRegfFilters(OperationReportByEnforcementFiltersDto filters,
        List<String> validationErrors) {
        boolean hasRegfDateFilter =
            filters.getRegfDateFrom() != null
                || filters.getRegfDateTo() != null;

        boolean isRegfMode =
            REGF.equals(filters.getReportEnforcementMode());

        if (hasRegfDateFilter && !isRegfMode) {
            validationErrors.add("reportEnforcementMode must be 'REGF' when filtering by REGF dates");
        }
    }

    private void validateLastActionFilters(OperationReportByEnforcementFiltersDto filters,
        List<String> validationErrors) {
        boolean hasLastActionDateFilter =
            filters.getLastActionDateFrom() != null
                || filters.getLastActionDateTo() != null;

        boolean isLastActionMode =
            LAST_ACTION.equals(filters.getReportEnforcementMode());

        if (hasLastActionDateFilter && !isLastActionMode) {
            validationErrors.add(
                "reportEnforcementMode must be 'LAST_ACTION' when filtering by last action dates"
            );
        }
        if (isLastActionMode && filters.getEnforcementAction() == null) {
            validationErrors.add(
                "enforcementAction is required when reportEnforcementMode is 'LAST_ACTION'"
            );
        }
    }
}
