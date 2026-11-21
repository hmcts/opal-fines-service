package uk.gov.hmcts.opal.entity.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.result.ImpositionCategory;

class ImpositionCategoryConverterTest {
    private final ImpositionCategoryConverter converter = new ImpositionCategoryConverter();

    @Test
    void roundTripsEveryDatabaseLabel() {
        // Given
        ImpositionCategory[] categories = ImpositionCategory.values();

        // When / Then
        for (ImpositionCategory category : categories) {
            assertThat(converter.convertToEntityAttribute(converter.convertToDatabaseColumn(category)))
                .isEqualTo(category);
        }
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
        assertThatThrownBy(() -> converter.convertToEntityAttribute("Fund"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
