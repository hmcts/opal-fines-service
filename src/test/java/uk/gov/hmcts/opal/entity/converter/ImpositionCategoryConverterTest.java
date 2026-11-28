package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.opal.entity.result.ImpositionCategory;

class ImpositionCategoryConverterTest {

    private final ImpositionCategoryConverter converter = new ImpositionCategoryConverter();

    @ParameterizedTest
    @EnumSource(ImpositionCategory.class)
    void roundTripsEveryCategory(ImpositionCategory category) {
        assertEquals(category.getLabel(), converter.convertToDatabaseColumn(category));
        assertEquals(category, converter.convertToEntityAttribute(category.getLabel()));
    }

    @Test
    void convertsNullValuesToNull() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "\t"})
    void convertsBlankDatabaseValuesToNull(String value) {
        assertNull(converter.convertToEntityAttribute(value));
    }

    @Test
    void rejectsUnknownDatabaseValue() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("unknown"));
    }
}
