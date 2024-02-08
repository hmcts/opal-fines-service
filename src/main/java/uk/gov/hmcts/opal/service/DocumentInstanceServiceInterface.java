package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.DocumentInstanceSearchDto;
import uk.gov.hmcts.opal.entity.DocumentInstanceEntity;

import java.util.List;

public interface DocumentInstanceServiceInterface {

    DocumentInstanceEntity getDocumentInstance(long documentInstanceId);

    List<DocumentInstanceEntity> searchDocumentInstances(DocumentInstanceSearchDto criteria);
}
