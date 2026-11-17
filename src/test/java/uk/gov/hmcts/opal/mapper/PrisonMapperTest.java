package uk.gov.hmcts.opal.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.generated.model.PrisonsPrison;
import uk.gov.hmcts.opal.generated.model.PrisonsPrisonSummary;

class PrisonMapperTest {

    private final PrisonMapper mapper = Mappers.getMapper(PrisonMapper.class);

    @Test
    void shouldMapPrisonEntityToPrisonDto() {
        // Given
        PrisonEntity entity = prison("Brixton", 101L);

        // When
        PrisonsPrison result = mapper.toDto(entity);

        // Then
        assertThat(result.getPrisonId()).isEqualTo(101L);
        assertThat(result.getName()).isEqualTo("Brixton");
        assertThat(result.getAddressLine1()).isEqualTo("Line 1");
        assertThat(result.getAddressLine2()).isEqualTo("Line 2");
        assertThat(result.getAddressLine3()).isEqualTo("Line 3");
        assertThat(result.getAddressLine4()).isEqualTo("Line 4");
        assertThat(result.getAddressLine5()).isEqualTo("Line 5");
        assertThat(result.getPostcode()).isEqualTo("SW9 1AA");
    }

    @Test
    void shouldMapPrisonEntityToSummaryDto() {
        // Given
        PrisonEntity entity = prison("Brixton", 101L);

        // When
        PrisonsPrisonSummary result = mapper.toSummary(entity);

        // Then
        assertThat(result.getPrisonId()).isEqualTo(101L);
        assertThat(result.getName()).isEqualTo("Brixton");
    }

    private PrisonEntity prison(String name, Long id) {
        return PrisonEntity.builder()
            .prisonId(id)
            .name(name)
            .addressLine1("Line 1")
            .addressLine2("Line 2")
            .addressLine3("Line 3")
            .addressLine4("Line 4")
            .addressLine5("Line 5")
            .postcode("SW9 1AA")
            .build();
    }
}
