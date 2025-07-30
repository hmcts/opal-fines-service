package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.search.DocumentSearchDto;
import uk.gov.hmcts.opal.entity.DocumentEntity;

import java.util.List;

public interface DocumentServiceInterface {

    DocumentEntity getDocument(String documentId);

    List<DocumentEntity> searchDocuments(DocumentSearchDto criteria);
}
