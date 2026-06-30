package uk.gov.hmcts.opal.entity.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.opal.entity.ChequeStatusType;

class ChequeStatusTypeConverterTest {

    private final ChequeStatusTypeConverter converter = new ChequeStatusTypeConverter();

    @ParameterizedTest
    @EnumSource(ChequeStatusType.class)
    void givenChequeStatusType_whenConvertToDatabaseColumn_thenReturnLabel(ChequeStatusType chequeStatusType) {
        assertThat(converter.convertToDatabaseColumn(chequeStatusType)).isEqualTo(chequeStatusType.getLabel());
    }

    @Test
    void givenNullChequeStatusType_whenConvertToDatabaseColumn_thenReturnNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @ParameterizedTest
    @EnumSource(ChequeStatusType.class)
    void givenDatabaseLabel_whenConvertToEntityAttribute_thenReturnChequeStatusType(
        ChequeStatusType chequeStatusType) {
        assertThat(converter.convertToEntityAttribute(chequeStatusType.getLabel())).isEqualTo(chequeStatusType);
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
            .hasMessage("Unknown ChequeStatusType: Unknown");
    }
}
