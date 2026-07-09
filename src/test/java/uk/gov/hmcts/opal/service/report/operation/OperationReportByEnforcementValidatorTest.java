package uk.gov.hmcts.opal.service.report.operation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.opal.service.report.ReportEnforcementMode.LAST_ACTION;
import static uk.gov.hmcts.opal.service.report.ReportEnforcementMode.REGF;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.opal.dto.ResultId;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportByEnforcementFiltersDto;
import uk.gov.hmcts.opal.service.report.ReportEnforcementMode;

class OperationReportByEnforcementValidatorTest {

    private final OperationReportByEnforcementValidator validator =
        new OperationReportByEnforcementValidator();

    @Test
    void validate_whenNoRelevantFilters_doesNotThrow() {
        OperationReportByEnforcementFiltersDto filters = new OperationReportByEnforcementFiltersDto();
        filters.setBusinessUnitIds(List.of(1L));
        assertThatCode(() -> validator.validate(filters))
            .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void validate_nullOrEmptyBusinessUnitIds_throwsIllegalArgumentException(List<Long> buIds) {
        OperationReportByEnforcementFiltersDto filters = new OperationReportByEnforcementFiltersDto();
        filters.setBusinessUnitIds(buIds);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> validator.validate(filters));
        assertEquals(
            "'businessUnitIds' must contain at least one business unit id",
            ex.getMessage()
        );
    }

    @Test
    void validate_whenLastActionDatesProvidedWithoutLastActionMode_throwsException() {
        OperationReportByEnforcementFiltersDto filters = new OperationReportByEnforcementFiltersDto();
        filters.setBusinessUnitIds(List.of(1L));
        filters.setLastActionDateFrom(LocalDate.of(2024, 1, 1));
        assertThatThrownBy(() -> validator.validate(filters))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("reportEnforcementMode must be 'LAST_ACTION' when filtering by last action dates");
    }

    @Test
    void validate_whenLastActionDatesProvidedWithWrongMode_throwsException() {
        OperationReportByEnforcementFiltersDto filters = new OperationReportByEnforcementFiltersDto();
        filters.setBusinessUnitIds(List.of(1L));
        filters.setReportEnforcementMode(ReportEnforcementMode.ALL);
        filters.setLastActionDateTo(LocalDate.of(2024, 1, 31));

        assertThatThrownBy(() -> validator.validate(filters))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("reportEnforcementMode must be 'LAST_ACTION' when filtering by last action dates");
    }

    @Test
    void validate_whenLastActionModeButNoEnforcementAction_throwsException() {
        OperationReportByEnforcementFiltersDto filters = new OperationReportByEnforcementFiltersDto();
        filters.setBusinessUnitIds(List.of(1L));
        filters.setReportEnforcementMode(LAST_ACTION);

        assertThatThrownBy(() -> validator.validate(filters))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("enforcementAction is required when reportEnforcementMode is 'LAST_ACTION'");
    }

    @Test
    void validate_whenLastActionModeAndEnforcementActionProvided_doesNotThrow() {
        OperationReportByEnforcementFiltersDto filters = new OperationReportByEnforcementFiltersDto();
        filters.setBusinessUnitIds(List.of(1L));
        filters.setReportEnforcementMode(LAST_ACTION);
        filters.setEnforcementAction(ResultId.ABDC.value());

        assertThatCode(() -> validator.validate(filters))
            .doesNotThrowAnyException();
    }

    @Test
    void validate_whenRegfDatesProvidedWithoutRegfMode_throwsException() {
        OperationReportByEnforcementFiltersDto filters = new OperationReportByEnforcementFiltersDto();
        filters.setBusinessUnitIds(List.of(1L));
        filters.setRegfDateFrom(LocalDate.of(2000, 1, 1));

        assertThatThrownBy(() -> validator.validate(filters))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("reportEnforcementMode must be 'REGF' when filtering by REGF dates");
    }

    @Test
    void validate_whenRegfDatesProvidedWithWrongMode_throwsException() {
        OperationReportByEnforcementFiltersDto filters = new OperationReportByEnforcementFiltersDto();
        filters.setBusinessUnitIds(List.of(1L));
        filters.setReportEnforcementMode(ReportEnforcementMode.ALL);
        filters.setRegfDateTo(LocalDate.of(2000, 2, 2));

        assertThatThrownBy(() -> validator.validate(filters))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("reportEnforcementMode must be 'REGF' when filtering by REGF dates");
    }

    @Test
    void validate_whenRegfModeAndRegfDatesProvided_doesNotThrow() {
        OperationReportByEnforcementFiltersDto filters = new OperationReportByEnforcementFiltersDto();
        filters.setBusinessUnitIds(List.of(1L));
        filters.setReportEnforcementMode(REGF);
        filters.setRegfDateFrom(LocalDate.of(2000, 1, 1));
        filters.setRegfDateTo(LocalDate.of(2000, 2, 2));

        assertThatCode(() -> validator.validate(filters))
            .doesNotThrowAnyException();
    }

    @Test
    void validate_whenMultipleValidationFailuresExist_returnsAllErrors() {
        OperationReportByEnforcementFiltersDto filters =
            new OperationReportByEnforcementFiltersDto();

        filters.setBusinessUnitIds(List.of(1L));
        filters.setLastActionDateFrom(LocalDate.of(2024, 1, 1));
        filters.setRegfDateFrom(LocalDate.of(2000, 1, 1));

        assertThatThrownBy(() -> validator.validate(filters))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(
                "reportEnforcementMode must be 'LAST_ACTION' when filtering by last action dates; "
                    + "reportEnforcementMode must be 'REGF' when filtering by REGF dates"
            );
    }
}