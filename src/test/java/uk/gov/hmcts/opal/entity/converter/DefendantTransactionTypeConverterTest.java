package uk.gov.hmcts.opal.entity.converter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefendantTransactionTypeConverterTest {

    private final DefendantTransactionTypeConverter converter = new DefendantTransactionTypeConverter();

    @ParameterizedTest
    @EnumSource(DefendantTransactionType.class)
    void givenTransactionType_whenConvertToDatabaseColumn_thenReturnLabel(DefendantTransactionType transactionType) {
        assertThat(converter.convertToDatabaseColumn(transactionType)).isEqualTo(transactionType.getLabel());
    }

    @Test
    void givenNullTransactionType_whenConvertToDatabaseColumn_thenReturnNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @ParameterizedTest
    @EnumSource(DefendantTransactionType.class)
    void givenDatabaseLabel_whenConvertToEntityAttribute_thenReturnTransactionType(
        DefendantTransactionType transactionType) {
        assertThat(converter.convertToEntityAttribute(transactionType.getLabel())).isEqualTo(transactionType);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void givenBlankDatabaseLabel_whenConvertToEntityAttribute_thenReturnNull(String databaseLabel) {
        assertThat(converter.convertToEntityAttribute(databaseLabel)).isNull();
    }

    @Test
    void givenUnknownDatabaseLabel_whenConvertToEntityAttribute_thenThrowException() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("UNKNOWN"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
