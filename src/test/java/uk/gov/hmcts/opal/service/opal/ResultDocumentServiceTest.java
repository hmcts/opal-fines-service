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
import org.springframework.data.jpa.repository.JpaSpecificationExecutor.SpecificationFluentQuery;
import uk.gov.hmcts.opal.dto.search.ResultDocumentSearchDto;
import uk.gov.hmcts.opal.entity.ResultDocumentEntity;
import uk.gov.hmcts.opal.repository.ResultDocumentRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultDocumentServiceTest {

    @Mock
    private ResultDocumentRepository resultDocumentRepository;

    @InjectMocks
    private ResultDocumentService resultDocumentService;

    @Test
    void testGetResultDocument() {
        // Arrange

        ResultDocumentEntity resultDocumentEntity = ResultDocumentEntity.builder().build();
        when(resultDocumentRepository.getReferenceById(any())).thenReturn(resultDocumentEntity);

        // Act
        ResultDocumentEntity result = resultDocumentService.getResultDocument(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchResultDocuments() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);

        ResultDocumentEntity resultDocumentEntity = ResultDocumentEntity.builder().build();
        Page<ResultDocumentEntity> mockPage = new PageImpl<>(List.of(resultDocumentEntity), Pageable.unpaged(), 999L);
        when(resultDocumentRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<ResultDocumentEntity> result = resultDocumentService.searchResultDocuments(ResultDocumentSearchDto
                                                                                            .builder().build());

        // Assert
        assertEquals(List.of(resultDocumentEntity), result);

    }


}
