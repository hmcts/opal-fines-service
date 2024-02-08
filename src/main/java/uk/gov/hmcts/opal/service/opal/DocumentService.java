package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.DocumentSearchDto;
import uk.gov.hmcts.opal.entity.DocumentEntity;
import uk.gov.hmcts.opal.repository.DocumentRepository;
import uk.gov.hmcts.opal.service.DocumentServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService implements DocumentServiceInterface {

    private final DocumentRepository documentRepository;

    @Override
    public DocumentEntity getDocument(long documentId) {
        return documentRepository.getReferenceById(documentId);
    }

    @Override
    public List<DocumentEntity> searchDocuments(DocumentSearchDto criteria) {
        return null;
    }

}
