package uk.gov.hmcts.opal.entity.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.opal.entity.ChequeAllocationType;

class ChequeAllocationTypeConverterTest {

    private final ChequeAllocationTypeConverter converter = new ChequeAllocationTypeConverter();

    @ParameterizedTest
    @EnumSource(ChequeAllocationType.class)
    void givenChequeAllocationType_whenConvertToDatabaseColumn_thenReturnLabel(
        ChequeAllocationType chequeAllocationType) {
        assertThat(converter.convertToDatabaseColumn(chequeAllocationType)).isEqualTo(chequeAllocationType.getLabel());
    }

    @Test
    void givenNullChequeAllocationType_whenConvertToDatabaseColumn_thenReturnNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @ParameterizedTest
    @EnumSource(ChequeAllocationType.class)
    void givenDatabaseLabel_whenConvertToEntityAttribute_thenReturnChequeAllocationType(
        ChequeAllocationType chequeAllocationType) {
        assertThat(converter.convertToEntityAttribute(chequeAllocationType.getLabel())).isEqualTo(chequeAllocationType);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void givenBlankDatabaseLabel_whenConvertToEntityAttribute_thenReturnNull(String databaseLabel) {
        assertThat(converter.convertToEntityAttribute(databaseLabel)).isNull();
    }

    @Test
    void givenUnknownDatabaseLabel_whenConvertToEntityAttribute_thenThrowException() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("Unknown"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unknown ChequeAllocationType: Unknown");
    }
}
