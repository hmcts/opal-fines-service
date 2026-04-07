package uk.gov.hmcts.opal.entity.converter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionWriteOffCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefendantTransactionWriteOffCodeConverterTest {

    private final DefendantTransactionWriteOffCodeConverter converter = new DefendantTransactionWriteOffCodeConverter();

    @ParameterizedTest
    @EnumSource(DefendantTransactionWriteOffCode.class)
    void givenWriteOffCode_whenConvertToDatabaseColumn_thenReturnLabel(DefendantTransactionWriteOffCode writeOffCode) {
        assertThat(converter.convertToDatabaseColumn(writeOffCode)).isEqualTo(writeOffCode.getLabel());
    }

    @Test
    void givenNullWriteOffCode_whenConvertToDatabaseColumn_thenReturnNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @ParameterizedTest
    @EnumSource(DefendantTransactionWriteOffCode.class)
    void givenDatabaseLabel_whenConvertToEntityAttribute_thenReturnWriteOffCode(
        DefendantTransactionWriteOffCode writeOffCode) {
        assertThat(converter.convertToEntityAttribute(writeOffCode.getLabel())).isEqualTo(writeOffCode);
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
