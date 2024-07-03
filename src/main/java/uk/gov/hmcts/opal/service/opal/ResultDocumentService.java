package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ResultDocumentSearchDto;
import uk.gov.hmcts.opal.entity.ResultDocumentEntity;
import uk.gov.hmcts.opal.repository.ResultDocumentRepository;
import uk.gov.hmcts.opal.repository.jpa.ResultDocumentSpecs;
import uk.gov.hmcts.opal.service.ResultDocumentServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("resultDocumentService")
public class ResultDocumentService implements ResultDocumentServiceInterface {

    private final ResultDocumentRepository resultDocumentRepository;

    private final ResultDocumentSpecs specs = new ResultDocumentSpecs();

    @Override
    public ResultDocumentEntity getResultDocument(long resultDocumentId) {
        return resultDocumentRepository.getReferenceById(resultDocumentId);
    }

    @Override
    public List<ResultDocumentEntity> searchResultDocuments(ResultDocumentSearchDto criteria) {
        Page<ResultDocumentEntity> page = resultDocumentRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
