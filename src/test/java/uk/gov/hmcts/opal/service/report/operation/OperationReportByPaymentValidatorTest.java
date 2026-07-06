package uk.gov.hmcts.opal.service.report.operation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.ResultId;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportByPaymentFiltersDto;
import uk.gov.hmcts.opal.dto.report.operation.PaymentReportMode;

@ExtendWith(MockitoExtension.class)
class OperationReportByPaymentValidatorTest {

    private final OperationReportByPaymentValidator validator = new OperationReportByPaymentValidator();

    @Mock
    private OperationReportByPaymentFiltersDto filters;

    @Test
    void validate_validFilters_doesNotThrow() {
        when(filters.getBusinessUnitIds()).thenReturn(List.of(1L));
        when(filters.getIsPaymentMade()).thenReturn(true);
        when(filters.getReportMode()).thenReturn(PaymentReportMode.SINCE_DATE);
        when(filters.getSinceDate()).thenReturn(java.time.LocalDate.now());
        when(filters.getSinceLastEnforcementAction()).thenReturn(null);

        assertDoesNotThrow(() -> validator.validate(filters));
    }

    @Test
    void validate_whenIsPaymentMadeIsNull_throwsIllegalArgumentException() {
        when(filters.getBusinessUnitIds()).thenReturn(List.of(1L));
        when(filters.getIsPaymentMade()).thenReturn(null);
        when(filters.getReportMode()).thenReturn(PaymentReportMode.SINCE_DATE);
        when(filters.getSinceDate()).thenReturn(java.time.LocalDate.now());
        when(filters.getSinceLastEnforcementAction()).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> validator.validate(filters));

        assertEquals("'isPaymentMade' cannot be null", ex.getMessage());
    }

    @Test
    void validate_whenReportModeIsNull_throwsIllegalArgumentException() {
        when(filters.getBusinessUnitIds()).thenReturn(List.of(1L));
        when(filters.getIsPaymentMade()).thenReturn(true);
        when(filters.getReportMode()).thenReturn(null);
        when(filters.getSinceDate()).thenReturn(null);
        when(filters.getSinceLastEnforcementAction()).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> validator.validate(filters));

        assertEquals("'reportMode' cannot be null", ex.getMessage());
    }

    @Test
    void validate_whenSinceDateUsedWithWrongMode_throwsIllegalArgumentException() {
        when(filters.getBusinessUnitIds()).thenReturn(List.of(1L));
        when(filters.getIsPaymentMade()).thenReturn(true);
        when(filters.getReportMode()).thenReturn(PaymentReportMode.WITH_REGF);
        when(filters.getSinceDate()).thenReturn(java.time.LocalDate.now());
        when(filters.getSinceLastEnforcementAction()).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> validator.validate(filters));

        assertEquals("'sinceDate' can only be used with 'SINCE_DATE' reportMode", ex.getMessage());
    }

    @Test
    void validate_whenSinceLastEnforcementActionUsedWithWrongMode_throwsIllegalArgumentException() {
        when(filters.getBusinessUnitIds()).thenReturn(List.of(1L));
        when(filters.getIsPaymentMade()).thenReturn(true);
        when(filters.getReportMode()).thenReturn(PaymentReportMode.SINCE_DATE);
        when(filters.getSinceDate()).thenReturn(LocalDate.now());
        when(filters.getSinceLastEnforcementAction()).thenReturn(ResultId.ABDC);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> validator.validate(filters));

        assertEquals(
            "'sinceLastEnforcementAction' can only be used with 'SINCE_LAST_ENFORCEMENT' reportMode",
            ex.getMessage()
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    void validate_nullOrEmptyBusinessUnitIds_throwsIllegalArgumentException(List<Long> buIds) {
        when(filters.getBusinessUnitIds()).thenReturn(buIds);
        when(filters.getIsPaymentMade()).thenReturn(true);
        when(filters.getReportMode()).thenReturn(PaymentReportMode.WITH_REGF);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> validator.validate(filters));
        assertEquals(
            "'businessUnitIds' must contain at least one business unit id",
            ex.getMessage()
        );
    }

    @Test
    void validate_whenMultipleFieldsInvalid_joinsAllMessages() {
        when(filters.getIsPaymentMade()).thenReturn(null);
        when(filters.getReportMode()).thenReturn(PaymentReportMode.SINCE_LAST_ENFORCEMENT);
        when(filters.getSinceDate()).thenReturn(java.time.LocalDate.now());
        when(filters.getSinceLastEnforcementAction()).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> validator.validate(filters));

        assertEquals(
            "'businessUnitIds' must contain at least one business unit id; "
                + "'isPaymentMade' cannot be null; "
                + "'sinceLastEnforcementAction' cannot be null with 'SINCE_LAST_ENFORCEMENT' reportMode; "
                + "'sinceDate' can only be used with 'SINCE_DATE' reportMode",
            ex.getMessage()
        );
    }
}