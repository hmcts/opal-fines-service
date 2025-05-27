package uk.gov.hmcts.opal.service.opal;

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
import org.springframework.data.repository.query.FluentQuery;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceDataResponse;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.result.ResultEntityFull;
import uk.gov.hmcts.opal.entity.result.ResultEntityLite;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceData;
import uk.gov.hmcts.opal.mapper.ResultMapper;
import uk.gov.hmcts.opal.repository.ResultFullRepository;
import uk.gov.hmcts.opal.repository.ResultLiteRepository;

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
    private ResultLiteRepository resultLiteRepository;

    @Mock
    private ResultFullRepository resultFullRepository;

    @Spy
    private ResultMapper resultMapper;

    @InjectMocks
    private ResultService resultService;

    @Test
    void testGetResult() {
        // Arrange

        ResultEntityLite resultEntity = ResultEntityLite.builder().build();
        when(resultLiteRepository.findById(any())).thenReturn(Optional.of(resultEntity));

        // Act
        ResultEntityLite result = resultService.getLiteResultById("ABC");

        // Assert
        assertNotNull(result);

    }

    @Test
    void testGetResultReferenceData() {
        // Arrange

        ResultEntityLite resultEntity = ResultEntityLite.builder().resultId("ABC").build();
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
        ResultEntityLite resultEntity = ResultEntityLite.builder().resultId("ABC").build();
        List<ResultEntityLite> resultEntities = List.of(resultEntity);
        ResultReferenceData dto = new ResultReferenceData(
            "ABC", null, null, false, null, null, null);

        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);
        when(ffq.sortBy(any())).thenReturn(ffq);

        Page<ResultEntityLite> mockPage = new PageImpl<>(List.of(resultEntity), Pageable.unpaged(), 999L);
        when(resultLiteRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
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
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        ResultEntityFull resultEntity = ResultEntityFull.builder().build();
        Page<ResultEntityFull> mockPage = new PageImpl<>(List.of(resultEntity), Pageable.unpaged(), 999L);
        when(resultFullRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<ResultEntityFull> result = resultService.searchResults(ResultSearchDto.builder().build());

        // Assert
        assertEquals(List.of(resultEntity), result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testResultsReferenceData() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);
        when(ffq.sortBy(any())).thenReturn(ffq);

        ResultEntityFull entity = ResultEntityFull.builder().build();
        ResultReferenceData expectedRefData = new ResultReferenceData(
            null, null, null, false, null, null, null
        );
        when(resultMapper.toRefDataFromFull(entity)).thenReturn(expectedRefData);

        Page<ResultEntityFull> mockPage = new PageImpl<>(List.of(entity), Pageable.unpaged(), 999L);
        when(resultFullRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
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
}
