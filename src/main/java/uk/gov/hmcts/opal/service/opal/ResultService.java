package uk.gov.hmcts.opal.service.opal;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceDataResponse;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.result.ResultEntityFull;
import uk.gov.hmcts.opal.entity.result.ResultEntityFull_;
import uk.gov.hmcts.opal.entity.result.ResultEntityLite;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceData;
import uk.gov.hmcts.opal.entity.result.ResultEntityLite_;
import uk.gov.hmcts.opal.mapper.ResultMapper;
import uk.gov.hmcts.opal.repository.ResultFullRepository;
import uk.gov.hmcts.opal.repository.ResultLiteRepository;
import uk.gov.hmcts.opal.repository.jpa.ResultSpecsFull;
import uk.gov.hmcts.opal.repository.jpa.ResultSpecsLite;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Qualifier("resultService")
public class ResultService {

    private final ResultLiteRepository resultLiteRepository;
    private final ResultFullRepository resultFullRepository;
    private final ResultMapper resultMapper;

    private final ResultSpecsFull specsFull = new ResultSpecsFull();
    private final ResultSpecsLite specsLite = new ResultSpecsLite();

    public ResultEntityLite getLiteResultById(String resultId) {
        return resultLiteRepository.findById(resultId)
            .orElseThrow(() -> new EntityNotFoundException("'Result' not found with id: " + resultId));
    }

    public ResultReferenceData getResultRefDataById(String resultId) {
        return resultMapper.toRefData(getLiteResultById(resultId));
    }

    // @Cacheable(cacheNames = "resultReferenceDataByIds", key = "#resultIds.orElse('noIds'))")
    public ResultReferenceDataResponse getResultsByIds(Optional<List<String>> resultIds) {

        Sort idSort = Sort.by(Sort.Direction.ASC, ResultEntityLite_.RESULT_ID);

        Page<ResultEntityLite> page = resultLiteRepository
            .findBy(
                specsLite.referenceDataByIds(resultIds),
                    ffq -> ffq
                        .sortBy(idSort)
                        .page(Pageable.unpaged()));

        return resultMapper.toReferenceDataResponse(page.getContent());
    }

    public List<ResultEntityFull> searchResults(ResultSearchDto criteria) {
        Page<ResultEntityFull> page = resultFullRepository
            .findBy(
                specsFull.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

    @Cacheable(cacheNames = "resultReferenceDataCache", key = "#filter.orElse('noFilter')")
    public List<ResultReferenceData> getReferenceData(Optional<String> filter) {

        Sort nameSort = Sort.by(Sort.Direction.ASC, ResultEntityFull_.RESULT_TITLE);

        Page<ResultEntityFull> page = resultFullRepository
            .findBy(
                specsFull.referenceDataFilter(filter),
                    ffq -> ffq
                        .sortBy(nameSort)
                        .page(Pageable.unpaged()));

        return page.getContent().stream().map(resultMapper::toRefDataFromFull).toList();
    }

}
