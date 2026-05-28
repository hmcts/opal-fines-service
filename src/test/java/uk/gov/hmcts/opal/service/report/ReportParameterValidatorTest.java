package uk.gov.hmcts.opal.service.report;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.exception.ReportNotFoundException;
import uk.gov.hmcts.opal.exception.UnprocessableException;

@ExtendWith(MockitoExtension.class)
public class ReportParameterValidatorTest {

    private static final String REPORT_ID = "report-id";

    @InjectMocks
    ReportParameterValidator reportParameterValidator;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    ReportEntity report;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(report.getReportId()).thenReturn(REPORT_ID);
    }

    @Test
    void validateReportInstanceParameterValues_allSupportedParameterTypesValid_returnsTrue() {
        List<String> checkboxList = List.of("one", "two");
        List<String> radioList = List.of("one");
        when(objectMapper.convertValue(checkboxList,
            TypeFactory.defaultInstance().constructCollectionType(List.class, String.class))).thenReturn(checkboxList);
        when(objectMapper.convertValue(radioList,
            TypeFactory.defaultInstance().constructCollectionType(List.class, String.class))).thenReturn(radioList);

        when(report.getReportParameters()).thenReturn(List.of(
            parameter("date-param", "date", true, null, null, null),
            parameter("decimal-param", "decimal-2dp", true, 1.0, 10.0, null),
            parameter("integer-param", "integer", true, 1L, 10L, null),
            parameter("radio-param", "menu-radio", true, 1, 1, List.of("one", "two")),
            parameter("checkbox-param", "menu-checkbox", true, 1, 2, List.of("one", "two")),
            parameter("autocomplete-param", "menu-autocomplete", true, null, null, null),
            parameter("text-60-param", "text-60", true, 1, 60, null),
            parameter("text-100-param", "text-100", true, 1, 100, null),
            parameter("text-1000-param", "text-1000", true, 1, 1000, null)
        ));

        boolean result = reportParameterValidator.validateReportInstanceParameterValues(Map.of(
            "date-param", "2026-05-26",
            "decimal-param", 5.0,
            "integer-param", 5L,
            "radio-param", radioList,
            "checkbox-param", checkboxList,
            "autocomplete-param", "ignored by current validation",
            "text-60-param", "value",
            "text-100-param", "value",
            "text-1000-param", "value"
        ), report);

        assertTrue(result);
    }

    @Test
    void validateReportInstanceParameterValues_nullParametersWithMandatoryParameters_returnsFalse() {
        when(report.getReportParameters()).thenReturn(List.of(
            parameter("text-param", "text-60", true, null, null, null)
        ));

        boolean result = reportParameterValidator.validateReportInstanceParameterValues(null, report);

        assertFalse(result);
    }

    @Test
    void validateReportInstanceParameterValues_nullParametersWithNoMandatoryParameters_returnsTrue() {
        when(report.getReportParameters()).thenReturn(List.of(
            parameter("text-param", "text-60", false, null, null, null)
        ));

        boolean result = reportParameterValidator.validateReportInstanceParameterValues(null, report);

        assertTrue(result);
    }

    @Test
    void validateReportInstanceParameterValues_emptyParametersWithNoMandatoryParameters_returnsTrue() {
        when(report.getReportParameters()).thenReturn(List.of(
            parameter("text-param", "text-60", false, null, null, null)
        ));

        boolean result = reportParameterValidator.validateReportInstanceParameterValues(Map.of(), report);

        assertTrue(result);
    }

    @Test
    void validateReportInstanceParameterValues_emptyParametersWithMandatoryParameters_returnsFalse() {
        when(report.getReportParameters()).thenReturn(List.of(
            parameter("text-param", "text-60", true, null, null, null)
        ));

        boolean result = reportParameterValidator.validateReportInstanceParameterValues(Map.of(), report);

        assertFalse(result);
    }

    @Test
    void validateReportInstanceParameterValues_unknownParameterName_throwsException() {
        when(report.getReportParameters()).thenReturn(List.of(
            parameter("text-param", "text-60", false, null, null, null)
        ));

        assertThrows(UnprocessableException.class,
            () -> reportParameterValidator.validateReportInstanceParameterValues(Map.of("unknown-param", "value"),
                                                                               report));
    }

    @Test
    void validateReportInstanceParameterValues_unknownParameterType_throwsException() {
        when(report.getReportParameters()).thenReturn(List.of(
            parameter("text-param", "unknown-type", false, null, null, null)
        ));

        assertThrows(ReportNotFoundException.class,
            () -> reportParameterValidator.validateReportInstanceParameterValues(Map.of("text-param", "value"), report));
    }

    @Test
    void validateReportInstanceParameterValues_dateValueIsNotString_returnsFalse() {
        when(report.getReportParameters()).thenReturn(List.of(
            parameter("date-param", "date", false, null, null, null)
        ));

        boolean result = reportParameterValidator.validateReportInstanceParameterValues(Map.of("date-param", 123L),
                                                                                     report);

        assertFalse(result);
    }

    @Test
    void validateReportInstanceParameterValues_dateValueWithinConfiguredRange_returnsFalse() {
        when(report.getReportParameters()).thenReturn(List.of(
            parameter("date-param", "date", false, "2026-01-01", "2026-12-31", null)
        ));

        boolean result = reportParameterValidator.validateReportInstanceParameterValues(
            Map.of("date-param", "2026-05-26"), report);

        assertFalse(result);
    }

    @Test
    void validateReportInstanceParameterValues_dateValueCannotBeParsed_throwsException() {
        when(report.getReportParameters()).thenReturn(List.of(
            parameter("date-param", "date", false, null, null, null)
        ));

        assertThrows(DateTimeParseException.class,
            () -> reportParameterValidator.validateReportInstanceParameterValues(Map.of("date-param", "not-a-date"),
                                                                               report));
    }

    @Test
    void validateReportInstanceParameterValues_decimalValueIsNotDouble_returnsFalse() {
        when(report.getReportParameters()).thenReturn(List.of(
            parameter("decimal-param", "decimal-2dp", false, null, null, null)
        ));

        boolean result = reportParameterValidator.validateReportInstanceParameterValues(Map.of("decimal-param", 5L),
                                                                                     report);

        assertFalse(result);
    }

    @Test
    void validateReportInstanceParameterValues_decimalValueOutsideRange_returnsFalse() {
        when(report.getReportParameters()).thenReturn(List.of(
            parameter("decimal-param", "decimal-2dp", false, 1.0, 10.0, null)
        ));

        boolean result = reportParameterValidator.validateReportInstanceParameterValues(Map.of("decimal-param", 11.0),
                                                                                     report);

        assertFalse(result);
    }

    @Test
    void validateReportInstanceParameterValues_integerValueIsNotLong_returnsFalse() {
        when(report.getReportParameters()).thenReturn(List.of(
            parameter("integer-param", "integer", false, null, null, null)
        ));

        boolean result = reportParameterValidator.validateReportInstanceParameterValues(Map.of("integer-param", 5),
                                                                                     report);

        assertFalse(result);
    }

    @Test
    void validateReportInstanceParameterValues_integerValueOutsideRange_returnsFalse() {
        when(report.getReportParameters()).thenReturn(List.of(
            parameter("integer-param", "integer", false, 1L, 10L, null)
        ));

        boolean result = reportParameterValidator.validateReportInstanceParameterValues(Map.of("integer-param", 11L),
                                                                                     report);

        assertFalse(result);
    }

    @Test
    void validateReportInstanceParameterValues_menuHasTooManyValues_returnsFalse() {
        List<String> menuChoiceList = List.of("one", "two");
        when(objectMapper.convertValue(menuChoiceList,
            TypeFactory.defaultInstance().constructCollectionType(List.class, String.class))).thenReturn(menuChoiceList);

        when(report.getReportParameters()).thenReturn(List.of(
            parameter("menu-param", "menu-checkbox", false, 0, 1, List.of("one", "two"))
        ));

        boolean result = reportParameterValidator.validateReportInstanceParameterValues(
            Map.of("menu-param", menuChoiceList), report);

        assertFalse(result);
    }

    @Test
    void validateReportInstanceParameterValues_menuHasTooFewValues_returnsFalse() {
        List<String> menuChoiceList = List.of();
        when(objectMapper.convertValue(menuChoiceList,
            TypeFactory.defaultInstance().constructCollectionType(List.class, String.class))).thenReturn(menuChoiceList);
        when(report.getReportParameters()).thenReturn(List.of(
            parameter("menu-param", "menu-checkbox", false, 1, 2, List.of("one", "two"))
        ));

        boolean result = reportParameterValidator.validateReportInstanceParameterValues(
            Map.of("menu-param", menuChoiceList), report);

        assertFalse(result);
    }

    @Test
    void validateReportInstanceParameterValues_menuHasInvalidOption_returnsFalse() {
        List<String> menuChoiceList = List.of("three");
        when(objectMapper.convertValue(menuChoiceList,
            TypeFactory.defaultInstance().constructCollectionType(List.class, String.class))).thenReturn(menuChoiceList);

        when(report.getReportParameters()).thenReturn(List.of(
            parameter("menu-param", "menu-radio", false, 1, 1, List.of("one", "two"))
        ));

        boolean result = reportParameterValidator.validateReportInstanceParameterValues(
            Map.of("menu-param", menuChoiceList), report);

        assertFalse(result);
    }

    @Test
    void validateReportInstanceParameterValues_menuValueCannotBeConverted_returnsFalse() {
        Object notAList = new Object();
        when(objectMapper
            .convertValue(notAList, TypeFactory.defaultInstance().constructCollectionType(List.class, String.class)))
            .thenThrow(IllegalArgumentException.class);

        when(report.getReportParameters()).thenReturn(List.of(
            parameter("menu-param", "menu-radio", false, 0, 2, List.of("one", "two"))
        ));

        boolean result = reportParameterValidator.validateReportInstanceParameterValues(
            Map.of("menu-param", notAList), report);

        assertFalse(result);
    }

    @Test
    void validateReportInstanceParameterValues_textValueIsNotString_returnsFalse() {
        when(report.getReportParameters()).thenReturn(List.of(
            parameter("text-param", "text-60", false, null, null, null)
        ));

        boolean result = reportParameterValidator.validateReportInstanceParameterValues(Map.of("text-param", 123L),
                                                                                     report);

        assertFalse(result);
    }

    @Test
    void validateReportInstanceParameterValues_textValueShorterThanMin_returnsFalse() {
        when(report.getReportParameters()).thenReturn(List.of(
            parameter("text-param", "text-100", false, 5, 100, null)
        ));

        boolean result = reportParameterValidator.validateReportInstanceParameterValues(Map.of("text-param", "abcd"),
                                                                                     report);

        assertFalse(result);
    }

    @Test
    void validateReportInstanceParameterValues_textValueLongerThanMax_returnsFalse() {
        when(report.getReportParameters()).thenReturn(List.of(
            parameter("text-param", "text-1000", false, 0, 5, null)
        ));

        boolean result = reportParameterValidator.validateReportInstanceParameterValues(Map.of("text-param", "abcdef"),
                                                                                     report);

        assertFalse(result);
    }

    private ReportParameterData parameter(String name, String type, boolean mandatory, Object min, Object max,
                                          List<String> options) {
        return new ReportParameterData(name, null, type, mandatory, min, max, null, null, options, null);
    }
}
