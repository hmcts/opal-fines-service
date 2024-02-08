package uk.gov.hmcts.opal.service.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.opal.dto.search.DocumentSearchDto;
import uk.gov.hmcts.opal.entity.DocumentEntity;
import uk.gov.hmcts.opal.repository.DocumentRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private DocumentService documentService;

    @Test
    void testGetDocument() {
        // Arrange

        DocumentEntity documentEntity = DocumentEntity.builder().build();
        when(documentRepository.getReferenceById(any())).thenReturn(documentEntity);

        // Act
        DocumentEntity result = documentService.getDocument(1);

        // Assert
        assertNotNull(result);

    }

    @Test
    void testSearchDocuments() {
        // Arrange

        DocumentEntity documentEntity = DocumentEntity.builder().build();
        Page<DocumentEntity> mockPage = new PageImpl<>(List.of(documentEntity), Pageable.unpaged(), 999L);
        // when(documentRepository.findBy(any(Specification.class), any())).thenReturn(mockPage);

        // Act
        List<DocumentEntity> result = documentService.searchDocuments(DocumentSearchDto.builder().build());

        // Assert
        assertNull(result);

    }


}
