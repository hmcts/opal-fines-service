package uk.gov.hmcts.opal.service.report.operation;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.opal.dto.ResultId;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportByPaymentFiltersDto;

class OperationReportByPaymentValidatorTest {

    private final OperationReportByPaymentValidator validator =
        new OperationReportByPaymentValidator();

    @Test
    void validate_paymentFilterIsNull_throwsException() {
        OperationReportByPaymentFiltersDto filter = OperationReportByPaymentFiltersDto.builder()
            .isWithRegf(TRUE)
            .build();

        assertThatThrownBy(() -> validator.validate(filter))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("'isPaymentMade' cannot be null");
    }

    @Test
    void validate_multipleInvalidFilters_throwsException() {
        OperationReportByPaymentFiltersDto filter = OperationReportByPaymentFiltersDto.builder().build();

        assertThatThrownBy(() -> validator.validate(filter))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("'isPaymentMade' cannot be null;"
                + " Filters should contain exactly one of 'sinceLastEnforcementAction', 'sinceDate' or 'isWithRegf'");
    }

    @ParameterizedTest
    @MethodSource("provideValidAccountFilters")
    void validate_whenFiltersContainExactlyOneAccountFilter_doesNotThrow(OperationReportByPaymentFiltersDto filters) {
        assertThatCode(() -> validator.validate(filters))
            .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("provideInvalidAccountFilters")
    void validate_whenFiltersContainsMoreThanOneAccountFilter_throwsException(
        OperationReportByPaymentFiltersDto filters) {
        assertThatThrownBy(() -> validator.validate(filters))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(
                "Filters should contain exactly one of 'sinceLastEnforcementAction', 'sinceDate' or 'isWithRegf'");
    }

    private static Stream<OperationReportByPaymentFiltersDto> provideValidAccountFilters() {
        return Stream.of(
            OperationReportByPaymentFiltersDto.builder()
                .isPaymentMade(TRUE)
                .isWithRegf(TRUE)
                .build(),
            OperationReportByPaymentFiltersDto.builder()
                .isPaymentMade(FALSE)
                .sinceLastEnforcementAction(ResultId.ABDC)
                .build(),
            OperationReportByPaymentFiltersDto.builder()
                .isPaymentMade(TRUE)
                .sinceDate(LocalDate.now())
                .build());
    }

    private static Stream<OperationReportByPaymentFiltersDto> provideInvalidAccountFilters() {
        return Stream.of(
            OperationReportByPaymentFiltersDto.builder()
                .isPaymentMade(TRUE)
                .isWithRegf(true)
                .sinceDate(LocalDate.now())
                .build(),
            OperationReportByPaymentFiltersDto.builder()
                .isPaymentMade(TRUE)
                .isWithRegf(true)
                .sinceLastEnforcementAction(ResultId.ABDC)
                .build(),
            OperationReportByPaymentFiltersDto.builder()
                .isPaymentMade(TRUE)
                .sinceDate(LocalDate.now())
                .sinceLastEnforcementAction(ResultId.ABDC)
                .build(),
            OperationReportByPaymentFiltersDto.builder()
                .isPaymentMade(TRUE)
                .sinceLastEnforcementAction(ResultId.ABDC)
                .sinceDate(LocalDate.now())
                .isWithRegf(TRUE).build(),
            OperationReportByPaymentFiltersDto.builder()
                .isPaymentMade(TRUE)
                .build());
    }
}