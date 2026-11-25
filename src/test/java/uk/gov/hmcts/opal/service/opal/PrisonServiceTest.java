package uk.gov.hmcts.opal.service.opal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.generated.model.PrisonsPrison;
import uk.gov.hmcts.opal.generated.model.PrisonsPrisonSummary;
import uk.gov.hmcts.opal.generated.model.PrisonsReferenceDataResponse;
import uk.gov.hmcts.opal.mapper.PrisonMapper;
import uk.gov.hmcts.opal.repository.PrisonRepository;

@ExtendWith(MockitoExtension.class)
class PrisonServiceTest {

    @Mock
    private PrisonRepository prisonRepository;

    @Mock
    private PrisonMapper prisonMapper;

    @InjectMocks
    private PrisonService prisonService;

    @Test
    void shouldGetPrisonById() {
        // Given
        PrisonEntity entity = prison("Brixton", 101L);
        PrisonsPrison dto = PrisonsPrison.builder().prisonId(101L).name("Brixton").build();
        when(prisonRepository.findById(101L)).thenReturn(Optional.of(entity));
        when(prisonMapper.toDto(entity)).thenReturn(dto);

        // When
        PrisonsPrison result = prisonService.getPrisonById(101L);

        // Then
        assertThat(result).isSameAs(dto);
        verify(prisonMapper).toDto(entity);
    }

    @Test
    void shouldThrowWhenPrisonDoesNotExist() {
        // Given
        when(prisonRepository.findById(999L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> prisonService.getPrisonById(999L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Prison not found with id: 999");
    }

    @Test
    void shouldReturnSortedPrisonReferenceData() {
        // Given
        PrisonEntity zed = prison("Zed", 2L);
        PrisonEntity alpha = prison("Alpha", 1L);
        PrisonsPrisonSummary zedSummary = PrisonsPrisonSummary.builder().prisonId(2L).name("Zed").build();
        PrisonsPrisonSummary alphaSummary = PrisonsPrisonSummary.builder().prisonId(1L).name("Alpha").build();
        when(prisonRepository.findAll()).thenReturn(List.of(zed, alpha));
        when(prisonMapper.toSummary(alpha)).thenReturn(alphaSummary);
        when(prisonMapper.toSummary(zed)).thenReturn(zedSummary);

        // When
        PrisonsReferenceDataResponse result = prisonService.getPrisonsRefData();

        // Then
        assertThat(result.getCount()).isEqualTo(2);
        assertThat(result.getPrisons()).containsExactly(alphaSummary, zedSummary);
    }

    private PrisonEntity prison(String name, Long id) {
        return PrisonEntity.builder().prisonId(id).name(name).addressLine1("Line 1").build();
    }
}
