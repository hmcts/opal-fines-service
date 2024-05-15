package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.ResultEntity;
import uk.gov.hmcts.opal.repository.ResultRepository;
import uk.gov.hmcts.opal.repository.jpa.ResultSpecs;
import uk.gov.hmcts.opal.service.ResultServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("resultService")
public class ResultService implements ResultServiceInterface {

    private final ResultRepository resultRepository;

    private final ResultSpecs specs = new ResultSpecs();

    @Override
    public ResultEntity getResult(long resultId) {
        return resultRepository.getReferenceById(resultId);
    }

    @Override
    public List<ResultEntity> searchResults(ResultSearchDto criteria) {
        Page<ResultEntity> page = resultRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
