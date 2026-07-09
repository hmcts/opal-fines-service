package uk.gov.hmcts.opal.entity.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.opal.entity.TransactionType;

class TransactionTypeConverterTest {

    private final TransactionTypeConverter converter = new TransactionTypeConverter();

    @ParameterizedTest
    @EnumSource(TransactionType.class)
    void givenTransactionType_whenConvertToDatabaseColumn_thenReturnLabel(TransactionType transactionType) {
        assertThat(converter.convertToDatabaseColumn(transactionType)).isEqualTo(transactionType.getLabel());
    }

    @Test
    void givenNullTransactionType_whenConvertToDatabaseColumn_thenReturnNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @ParameterizedTest
    @EnumSource(TransactionType.class)
    void givenDatabaseLabel_whenConvertToEntityAttribute_thenReturnTransactionType(TransactionType transactionType) {
        assertThat(converter.convertToEntityAttribute(transactionType.getLabel())).isEqualTo(transactionType);
    }

    @Test
    void givenNullDatabaseLabel_whenConvertToEntityAttribute_thenReturnNull() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void givenUnknownDatabaseLabel_whenConvertToEntityAttribute_thenThrowException() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("Unknown"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unknown TransactionType: Unknown");
    }
}
