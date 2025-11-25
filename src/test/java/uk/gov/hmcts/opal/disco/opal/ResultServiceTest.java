package uk.gov.hmcts.opal.disco.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor.SpecificationFluentQuery;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceDataResponse;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.entity.result.ResultEntity.Lite;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceData;
import uk.gov.hmcts.opal.mapper.ResultMapper;
import uk.gov.hmcts.opal.repository.ResultLiteRepository;
import uk.gov.hmcts.opal.service.opal.ResultService;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultServiceTest {

    @Mock
    private ResultLiteRepository resultLiteRepository;

    @Spy
    private ResultMapper resultMapper;

    @InjectMocks
    private ResultService resultService;

    @Test
    void testGetResult() {
        // Arrange

        ResultEntity.Lite resultEntity = Lite.builder().build();
        when(resultLiteRepository.findById(any())).thenReturn(Optional.of(resultEntity));

        // Act
        ResultEntity.Lite result = resultService.getLiteResultById("ABC");

        // Assert
        assertNotNull(result);

    }

    @Test
    void testGetResultReferenceData() {
        // Arrange

        ResultEntity.Lite resultEntity = ResultEntity.Lite.builder().resultId("ABC").build();
        ResultReferenceData expectedRefData = new ResultReferenceData(
            "ABC", null, null, false, null, null, null
        );
        when(resultLiteRepository.findById(any())).thenReturn(Optional.of(resultEntity));
        when(resultMapper.toRefData(resultEntity)).thenReturn(expectedRefData);

        // Act
        ResultReferenceData result = resultService.getResultRefDataById("ABC");

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetResultsByIds() {
        // Arrange
        Lite resultEntity = Lite.builder().resultId("ABC").build();
        List<ResultEntity.Lite> resultEntities = List.of(resultEntity);
        ResultReferenceData dto = new ResultReferenceData(
            "ABC", null, null, false, null, null, null);

        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        Page<Lite> mockPage = new PageImpl<>(List.of(resultEntity), Pageable.unpaged(), 999L);
        when(resultLiteRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        when(resultMapper.toRefData(any())).thenReturn(dto);

        // Act
        ResultReferenceDataResponse result = resultService.getResultsByIds(Optional.of(List.of("ABC")));

        ResultReferenceDataResponse expectedResponse = ResultReferenceDataResponse.builder()
            .refData(List.of(dto))
            .build();

        // Assert
        assertEquals(expectedResponse.getCount(), result.getCount());
        assertEquals(expectedResponse.getRefData(), result.getRefData());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchResults() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);

        ResultEntity.Lite resultEntity = ResultEntity.Lite.builder().build();
        Page<ResultEntity.Lite> mockPage = new PageImpl<>(List.of(resultEntity), Pageable.unpaged(), 999L);
        when(resultLiteRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<ResultEntity.Lite> result = resultService.searchResults(ResultSearchDto.builder().build());

        // Assert
        assertEquals(List.of(resultEntity), result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testResultsReferenceData() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        ResultEntity.Lite entity = ResultEntity.Lite.builder().build();
        ResultReferenceData expectedRefData = new ResultReferenceData(
            null, null, null, false, null, null, null
        );
        when(resultMapper.toRefData(entity)).thenReturn(expectedRefData);

        Page<ResultEntity.Lite> mockPage = new PageImpl<>(List.of(entity), Pageable.unpaged(), 999L);
        when(resultLiteRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<ResultReferenceData> result = resultService.getReferenceData(Optional.empty());

        ResultReferenceData refData = new ResultReferenceData(
            entity.getResultId(),
            entity.getResultTitle(),
            entity.getResultTitleCy(),
            entity.isActive(),
            entity.getResultType(),
            entity.getImpositionCreditor(),
            entity.getImpositionAllocationPriority()
        );

        // Assert
        assertEquals(List.of(refData), result);
    }

    @Test
    void testGetResultById_ReturnsFullDto() {
        // Arrange
        ResultEntity.Lite entity = ResultEntity.Lite.builder()
            .resultId("ABC")
            .resultTitle("Result Title")
            .resultTitleCy("Welsh Title")
            .resultType("TYPE1")
            .active(true)
            .build();

        uk.gov.hmcts.opal.dto.ResultDto dto = uk.gov.hmcts.opal.dto.ResultDto.builder()
            .resultId("ABC")
            .resultTitle("Result Title")
            .resultTitleCy("Welsh Title")
            .resultType("TYPE1")
            .active(true)
            .build();

        when(resultLiteRepository.findById("ABC")).thenReturn(Optional.of(entity));
        when(resultMapper.toDto(entity)).thenReturn(dto);

        // Act
        uk.gov.hmcts.opal.dto.ResultDto result = resultService.getResultById("ABC");

        // Assert
        assertNotNull(result);
        assertEquals("ABC", result.getResultId());
        assertEquals("Result Title", result.getResultTitle());
        verify(resultLiteRepository).findById("ABC");
        verify(resultMapper).toDto(entity);
    }

    @Test
    void testGetResultById_ThrowsWhenNotFound() {
        // Arrange
        when(resultLiteRepository.findById("MISSING")).thenReturn(Optional.empty());

        // Act + Assert
        org.junit.jupiter.api.Assertions.assertThrows(
            jakarta.persistence.EntityNotFoundException.class,
            () -> resultService.getResultById("MISSING")
        );
    }
}
