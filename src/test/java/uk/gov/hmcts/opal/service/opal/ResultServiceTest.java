package uk.gov.hmcts.opal.service.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.FluentQuery;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.ResultEntity;
import uk.gov.hmcts.opal.entity.projection.ResultReferenceData;
import uk.gov.hmcts.opal.repository.ResultRepository;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultServiceTest {

    @Mock
    private ResultRepository resultRepository;

    @InjectMocks
    private ResultService resultService;

    @Test
    void testGetResult() {
        // Arrange

        ResultEntity resultEntity = ResultEntity.builder().build();
        when(resultRepository.getReferenceById(any())).thenReturn(resultEntity);

        // Act
        ResultEntity result = resultService.getResult("ABC");

        // Assert
        assertNotNull(result);

    }

    @Test
    void testGetResultReferenceData() {
        // Arrange

        ResultEntity resultEntity = ResultEntity.builder().build();
        when(resultRepository.getReferenceById(any())).thenReturn(resultEntity);

        // Act
        ResultReferenceData result = resultService.getResultReferenceData("ABC");

        // Assert
        assertNotNull(result);

    }

    @Test
    void testGetAllResults() {
        // Arrange
        ResultEntity resultEntity = ResultEntity.builder().build();
        when(resultRepository.findAll()).thenReturn(List.of(resultEntity));

        // Act
        List<ResultReferenceData> result = resultService.getAllResults();

        ResultReferenceData refData =  new ResultReferenceData(
            resultEntity.getResultId(),
            resultEntity.getResultTitle(),
            resultEntity.getResultTitleCy(),
            resultEntity.isActive(),
            resultEntity.getResultType(),
            resultEntity.getImpositionCreditor(),
            resultEntity.getImpositionAllocationPriority()
        );

        // Assert
        assertEquals(List.of(refData), result);

    }

    @Test
    void testGetResultsByIds() {
        // Arrange
        ResultEntity resultEntity = ResultEntity.builder().build();
        when(resultRepository.findByResultIdIn(any())).thenReturn(List.of(resultEntity));

        // Act
        List<ResultReferenceData> result = resultService.getResultsByIds(List.of("ABC"));

        ResultReferenceData refData =  new ResultReferenceData(
            resultEntity.getResultId(),
            resultEntity.getResultTitle(),
            resultEntity.getResultTitleCy(),
            resultEntity.isActive(),
            resultEntity.getResultType(),
            resultEntity.getImpositionCreditor(),
            resultEntity.getImpositionAllocationPriority()
        );

        // Assert
        assertEquals(List.of(refData), result);

    }




    @SuppressWarnings("unchecked")
    @Test
    void testSearchResults() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        ResultEntity resultEntity = ResultEntity.builder().build();
        Page<ResultEntity> mockPage = new PageImpl<>(List.of(resultEntity), Pageable.unpaged(), 999L);
        when(resultRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<ResultEntity> result = resultService.searchResults(ResultSearchDto.builder().build());

        // Assert
        assertEquals(List.of(resultEntity), result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testResultsReferenceData() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);
        when(ffq.sortBy(any())).thenReturn(ffq);

        ResultEntity entity = ResultEntity.builder().build();
        Page<ResultEntity> mockPage = new PageImpl<>(List.of(entity), Pageable.unpaged(), 999L);
        when(resultRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<ResultReferenceData> result = resultService.getReferenceData(Optional.empty());

        ResultReferenceData refData =  new ResultReferenceData(
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
}
