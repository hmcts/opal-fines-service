package uk.gov.hmcts.opal.entity.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.opal.entity.BacsStatusType;

class BacsStatusTypeConverterTest {

    private final BacsStatusTypeConverter converter = new BacsStatusTypeConverter();

    @ParameterizedTest
    @EnumSource(BacsStatusType.class)
    void givenBacsStatusType_whenConvertToDatabaseColumn_thenReturnLabel(BacsStatusType bacsStatusType) {
        assertThat(converter.convertToDatabaseColumn(bacsStatusType)).isEqualTo(bacsStatusType.getLabel());
    }

    @Test
    void givenNullBacsStatusType_whenConvertToDatabaseColumn_thenReturnNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @ParameterizedTest
    @EnumSource(BacsStatusType.class)
    void givenDatabaseLabel_whenConvertToEntityAttribute_thenReturnBacsStatusType(BacsStatusType bacsStatusType) {
        assertThat(converter.convertToEntityAttribute(bacsStatusType.getLabel())).isEqualTo(bacsStatusType);
    }

    @Test
    void givenNullDatabaseLabel_whenConvertToEntityAttribute_thenReturnNull() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void givenUnknownDatabaseLabel_whenConvertToEntityAttribute_thenThrowException() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("Unknown"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unknown BACS Status Type: Unknown");
    }
}
