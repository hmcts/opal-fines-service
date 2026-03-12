package uk.gov.hmcts.opal.entity.converter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssociationTypeConverterTest {

    private final AssociationTypeConverter converter = new AssociationTypeConverter();

    @ParameterizedTest
    @EnumSource(AssociationType.class)
    void givenAssociationType_whenConvertToDatabaseColumn_thenReturnLabel(AssociationType associationType) {
        assertEquals(associationType.getLabel(), converter.convertToDatabaseColumn(associationType));
    }

    @Test
    void givenNullAssociationType_whenConvertToDatabaseColumn_thenReturnNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @ParameterizedTest
    @CsvSource({
        "Defendant, DEFENDANT",
        "'Parent/Guardian', PARENT_GUARDIAN",
        "Parent_Guardian, PARENT_GUARDIAN"
    })
    void givenValidLabel_whenConvertToEntityAttribute_thenReturnAssociationType(String label,
                                                                                AssociationType expectedType) {
        assertEquals(expectedType, converter.convertToEntityAttribute(label));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    void givenBlankLabel_whenConvertToEntityAttribute_thenReturnNull(String label) {
        assertNull(converter.convertToEntityAttribute(label));
    }

    @ParameterizedTest
    @ValueSource(strings = {"DEFENDANT", "Parent Guardian", "Respondent"})
    void givenUnsupportedLabel_whenConvertToEntityAttribute_thenThrowIllegalArgumentException(String label) {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute(label));
    }
}
