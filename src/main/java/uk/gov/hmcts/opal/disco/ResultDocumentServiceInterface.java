package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.search.ResultDocumentSearchDto;
import uk.gov.hmcts.opal.entity.ResultDocumentEntity;

import java.util.List;

public interface ResultDocumentServiceInterface {

    ResultDocumentEntity getResultDocument(long resultDocumentId);

    List<ResultDocumentEntity> searchResultDocuments(ResultDocumentSearchDto criteria);
}
