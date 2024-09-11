package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.ResultEntity_;
import uk.gov.hmcts.opal.entity.ResultEntity;
import uk.gov.hmcts.opal.entity.projection.ResultReferenceData;
import uk.gov.hmcts.opal.repository.ResultRepository;
import uk.gov.hmcts.opal.repository.jpa.ResultSpecs;
import uk.gov.hmcts.opal.service.ResultServiceInterface;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Qualifier("resultService")
public class ResultService implements ResultServiceInterface {

    private final ResultRepository resultRepository;

    private final ResultSpecs specs = new ResultSpecs();

    @Override
    public ResultEntity getResult(String resultId) {
        return resultRepository.getReferenceById(resultId);
    }

    public ResultReferenceData getResultReferenceData(String resultId) {
        return toRefData(resultRepository.getReferenceById(resultId));
    }

    public List<ResultReferenceData> getAllResults() {
        return resultRepository.findAll().stream().map(this::toRefData).toList();
    }

    public List<ResultReferenceData> getResultsbyIds(List<String> resultIds) {
        return resultRepository.findByResultIdIn(resultIds).stream().map(this::toRefData).toList();
    }



    @Override
    public List<ResultEntity> searchResults(ResultSearchDto criteria) {
        Page<ResultEntity> page = resultRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

    @Cacheable(
        cacheNames = "resultReferenceDataCache",
        key = "#filter.orElse('noFilter')"
    )
    public List<ResultReferenceData> getReferenceData(Optional<String> filter) {

        Sort nameSort = Sort.by(Sort.Direction.ASC, ResultEntity_.RESULT_TITLE);

        Page<ResultEntity> page = resultRepository
            .findBy(specs.referenceDataFilter(filter),
                    ffq -> ffq
                        .sortBy(nameSort)
                        .page(Pageable.unpaged()));

        return page.getContent().stream().map(this::toRefData).toList();
    }

    private ResultReferenceData toRefData(ResultEntity entity) {
        return new ResultReferenceData(
            entity.getResultId(),
            entity.getResultTitle(),
            entity.getResultTitleCy(),
            entity.isActive(),
            entity.getResultType(),
            entity.getImpositionCreditor(),
            entity.getImpositionAllocationPriority()
        );
    }
}
