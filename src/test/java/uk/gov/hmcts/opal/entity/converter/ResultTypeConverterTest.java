package uk.gov.hmcts.opal.entity.converter;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.result.ResultType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResultTypeConverterTest {

    private final ResultTypeConverter converter = new ResultTypeConverter();

    @Test
    void given_resultType_when_convertToDatabaseColumn_then_returnsLabel() {
        assertEquals("Action", converter.convertToDatabaseColumn(ResultType.ACTION));
        assertEquals("Result", converter.convertToDatabaseColumn(ResultType.RESULT));
    }

    @Test
    void given_nullResultType_when_convertToDatabaseColumn_then_returnsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void given_validLabel_when_convertToEntityAttribute_then_returnsResultType() {
        assertEquals(ResultType.ACTION, converter.convertToEntityAttribute("Action"));
        assertEquals(ResultType.RESULT, converter.convertToEntityAttribute("Result"));
    }

    @Test
    void given_blankOrNullLabel_when_convertToEntityAttribute_then_returnsNull() {
        assertNull(converter.convertToEntityAttribute(null));
        assertNull(converter.convertToEntityAttribute(""));
        assertNull(converter.convertToEntityAttribute(" "));
    }

    @Test
    void given_unknownLabel_when_convertToEntityAttribute_then_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("Unknown"));
    }
}
