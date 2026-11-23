package uk.gov.hmcts.opal.service.refdata.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.AnnotatedElementUtils;
import uk.gov.hmcts.opal.entity.result.ResultOpalEntity;
import uk.gov.hmcts.opal.repository.ResultOpalRepository;
import uk.gov.hmcts.opal.service.opal.OpalResultService;

@ExtendWith(MockitoExtension.class)
class ResultRefDataHandlerTest {
    @Mock
    private ResultOpalRepository repository;

    @Mock
    private ResultRefDataMapper mapper;

    private ResultRefDataHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ResultRefDataHandler(repository, mapper);
    }

    @Test
    void clearsCachesUsedByOpalResultService() throws NoSuchMethodException {
        // Given
        Cacheable resultCache = AnnotatedElementUtils.findMergedAnnotation(
            OpalResultService.class.getMethod("getResult", String.class, boolean.class), Cacheable.class);
        Cacheable referenceDataCache = AnnotatedElementUtils.findMergedAnnotation(
            OpalResultService.class.getMethod("getReferenceData", Optional.class), Cacheable.class);

        // When
        List<String> caches = handler.cachesToClear();

        // Then
        assertThat(resultCache).isNotNull();
        assertThat(referenceDataCache).isNotNull();
        assertThat(caches).containsExactlyInAnyOrderElementsOf(
            Stream.concat(Stream.of(resultCache.cacheNames()), Stream.of(referenceDataCache.cacheNames())).toList());
    }

    @Test
    void createsEntityWithBusinessKeyAndDelegatesMapping() {
        // Given
        ResultsRecord dto = ResultsRecord.builder().resultCode("ABC").build();

        // When
        ResultOpalEntity entity = handler.createEntityFromDto(dto);

        // Then
        assertThat(entity.getResultId()).isEqualTo("ABC");
        verify(mapper).updateEntityFromDto(dto, entity);
    }

    @Test
    void findsEntityByBusinessKey() {
        // Given
        ResultsRecord dto = ResultsRecord.builder().resultCode("ABC").build();
        ResultOpalEntity existing = ResultOpalEntity.builder().resultId("ABC").resultTitle("Old").build();
        when(repository.findById("ABC")).thenReturn(Optional.of(existing));

        // When
        Optional<ResultOpalEntity> found = handler.findEntity(dto);

        // Then
        assertThat(found).contains(existing);
    }

    @Test
    void delegatesUpdateToMapper() {
        // Given
        ResultsRecord dto = ResultsRecord.builder().resultCode("ABC").build();
        ResultOpalEntity existing = ResultOpalEntity.builder().resultId("ABC").resultTitle("Old").build();
        // When
        handler.updateEntityFromDto(dto, existing);

        // Then
        verify(mapper).updateEntityFromDto(dto, existing);
    }

    @Test
    void savesEntityAndReturnsRepositoryResult() {
        // Given
        ResultOpalEntity entity = ResultOpalEntity.builder().resultId("ABC").build();
        ResultOpalEntity saved = ResultOpalEntity.builder().resultId("ABC").build();
        when(repository.save(entity)).thenReturn(saved);

        // When
        ResultOpalEntity result = handler.saveEntity(entity);

        // Then
        assertThat(result).isSameAs(saved);
        verify(repository).save(entity);
    }

    @Test
    void deletesEntityByBusinessKey() {
        // When
        handler.deleteEntity("ABC");

        // Then
        verify(repository).deleteById("ABC");
    }

    @Test
    void exposesHandlerRegistrationTypes() {
        // When
        Class<ResultsRecord> payloadType = handler.payloadType();
        String refDataType = handler.refDataType();

        // Then
        assertThat(payloadType).isEqualTo(ResultsRecord.class);
        assertThat(refDataType).isEqualTo("Result");
    }

}
