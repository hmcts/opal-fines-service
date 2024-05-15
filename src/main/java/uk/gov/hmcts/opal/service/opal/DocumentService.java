package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.DocumentSearchDto;
import uk.gov.hmcts.opal.entity.DocumentEntity;
import uk.gov.hmcts.opal.repository.DocumentRepository;
import uk.gov.hmcts.opal.repository.jpa.DocumentSpecs;
import uk.gov.hmcts.opal.service.DocumentServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("documentService")
public class DocumentService implements DocumentServiceInterface {

    private final DocumentRepository documentRepository;

    private final DocumentSpecs specs = new DocumentSpecs();

    @Override
    public DocumentEntity getDocument(String documentId) {
        return documentRepository.getReferenceById(documentId);
    }

    @Override
    public List<DocumentEntity> searchDocuments(DocumentSearchDto criteria) {
        Page<DocumentEntity> page = documentRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
