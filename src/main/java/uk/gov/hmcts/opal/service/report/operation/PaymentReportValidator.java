package uk.gov.hmcts.opal.service.report.operation;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportByPaymentFiltersDto;
import uk.gov.hmcts.opal.dto.report.operation.PaymentReportMode;

@Component
public class PaymentReportValidator {

    public void validate(OperationReportByPaymentFiltersDto filters) {
        List<String> validationErrors = new ArrayList<>();
        validateBusinessUnits(filters, validationErrors);
        validateIsPaymentMadeFilter(filters, validationErrors);
        validateReportMode(filters, validationErrors);
        validateSinceDate(filters, validationErrors);
        validateSinceLastEnforcementAction(filters, validationErrors);

        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException(
                String.join("; ", validationErrors)
            );
        }
    }

    private void validateBusinessUnits(OperationReportByPaymentFiltersDto filters, List<String> validationErrors) {
        List<Long> buIds = filters.getBusinessUnitIds();
        if (buIds == null || buIds.isEmpty()) {
            validationErrors.add(
                "'businessUnitIds' must contain at least one business unit id");
        }
    }

    private void validateIsPaymentMadeFilter(OperationReportByPaymentFiltersDto filters,
        List<String> validationErrors) {
        if (filters.getIsPaymentMade() == null) {
            validationErrors.add(
                "'isPaymentMade' cannot be null");
        }
    }

    private void validateReportMode(
        OperationReportByPaymentFiltersDto filters,
        List<String> validationErrors
    ) {
        if (filters.getReportMode() == null) {
            validationErrors.add("'reportMode' cannot be null");
            return;
        }

        if (filters.getReportMode() == PaymentReportMode.SINCE_LAST_ENFORCEMENT
            && filters.getSinceLastEnforcementAction() == null) {
            validationErrors.add(
                "'sinceLastEnforcementAction' cannot be null with 'SINCE_LAST_ENFORCEMENT' reportMode"
            );
        }

        if (filters.getReportMode() == PaymentReportMode.SINCE_DATE
            && filters.getSinceDate() == null) {
            validationErrors.add(
                "'sinceDate' cannot be null with 'SINCE_DATE' reportMode"
            );
        }
    }

    private void validateSinceDate(
        OperationReportByPaymentFiltersDto filters,
        List<String> validationErrors
    ) {
        if (filters.getSinceDate() != null
            && filters.getReportMode() != null
            && !filters.getReportMode().equals(PaymentReportMode.SINCE_DATE)) {
            validationErrors.add("'sinceDate' can only be used with 'SINCE_DATE' reportMode");
        }
    }

    private void validateSinceLastEnforcementAction(
        OperationReportByPaymentFiltersDto filters,
        List<String> validationErrors
    ) {
        if (filters.getSinceLastEnforcementAction() != null
            && filters.getReportMode() != null
            && !filters.getReportMode().equals(PaymentReportMode.SINCE_LAST_ENFORCEMENT)) {
            validationErrors.add(
                "'sinceLastEnforcementAction' can only be used with 'SINCE_LAST_ENFORCEMENT' reportMode");
        }
    }
}
