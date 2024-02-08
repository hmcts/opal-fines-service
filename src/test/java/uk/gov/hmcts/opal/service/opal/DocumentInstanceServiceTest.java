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
import uk.gov.hmcts.opal.dto.search.DocumentInstanceSearchDto;
import uk.gov.hmcts.opal.entity.DocumentInstanceEntity;
import uk.gov.hmcts.opal.repository.DocumentInstanceRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentInstanceServiceTest {

    @Mock
    private DocumentInstanceRepository documentInstanceRepository;

    @InjectMocks
    private DocumentInstanceService documentInstanceService;

    @Test
    void testGetDocumentInstance() {
        // Arrange

        DocumentInstanceEntity documentInstanceEntity = DocumentInstanceEntity.builder().build();
        when(documentInstanceRepository.getReferenceById(any())).thenReturn(documentInstanceEntity);

        // Act
        DocumentInstanceEntity result = documentInstanceService.getDocumentInstance(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchDocumentInstances() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        DocumentInstanceEntity documentInstanceEntity = DocumentInstanceEntity.builder().build();
        Page<DocumentInstanceEntity> mockPage = new PageImpl<>(List.of(documentInstanceEntity),
                                                               Pageable.unpaged(), 999L);
        when(documentInstanceRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<DocumentInstanceEntity> result = documentInstanceService.searchDocumentInstances(
            DocumentInstanceSearchDto.builder().build());

        // Assert
        assertEquals(List.of(documentInstanceEntity), result);

    }


}
