package uk.gov.hmcts.opal.service.report.operation;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportByPaymentFiltersDto;

@Component
public class OperationReportByPaymentValidator {

    public void validate(OperationReportByPaymentFiltersDto filters) {
        List<String> validationErrors = new ArrayList<>();
        validateIsPaymentMadeFilter(filters, validationErrors);
        validateIncludeAccounts(filters, validationErrors);

        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException(
                String.join("; ", validationErrors)
            );
        }
    }

    private void validateIsPaymentMadeFilter(OperationReportByPaymentFiltersDto filters,
        List<String> validationErrors) {
        if (filters.getIsPaymentMade() == null) {
            validationErrors.add(
                "'isPaymentMade' cannot be null");
        }
    }

    private void validateIncludeAccounts(OperationReportByPaymentFiltersDto filters,
        List<String> validationErrors) {
        int count = 0;
        if (filters.getSinceLastEnforcementAction() != null) {
            count++;
        }
        if (filters.getSinceDate() != null) {
            count++;
        }
        if (filters.getIsWithRegf() != null) {
            count++;
        }
        if (count != 1) {
            validationErrors.add(
                "Filters should contain exactly one of 'sinceLastEnforcementAction', 'sinceDate' or 'isWithRegf'");
        }
    }
}
