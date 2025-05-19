package uk.gov.hmcts.opal.service.opal;


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
import uk.gov.hmcts.opal.mapper.ResultMapper;
import uk.gov.hmcts.opal.repository.ResultFullRepository;
import uk.gov.hmcts.opal.repository.ResultLiteRepository;
import uk.gov.hmcts.opal.repository.jpa.ResultSpecs;
import uk.gov.hmcts.opal.service.ResultServiceInterface;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Qualifier("resultService")
public class ResultService implements ResultServiceInterface {

    private final ResultLiteRepository resultLiteRepository;
    private final ResultFullRepository resultFullRepository;
    private final ResultMapper resultMapper;

    private final ResultSpecs specs = new ResultSpecs();

    @Override
    public ResultEntityLite getResult(String resultId) {
        return resultLiteRepository.getReferenceById(resultId);
    }

    public ResultReferenceData getResultReferenceData(String resultId) {
        return resultMapper.toRefData(resultLiteRepository.getReferenceById(resultId));
    }

    @Cacheable(
        cacheNames = "resultReferenceDataCache",
        key = "#root.method.name"
    )
    public ResultReferenceDataResponse getAllResults() {
        return resultMapper.toReferenceDataResponse(
            resultLiteRepository.findAll());
    }

    @Cacheable(
        cacheNames = "resultReferenceDataCache",
        key = "#root.method.name+ '_' + T(String).join(',', #resultIds)"
    )
    public ResultReferenceDataResponse getResultsByIds(List<String> resultIds) {
        return resultMapper.toReferenceDataResponse(
            resultLiteRepository.findByResultIdIn(resultIds));
    }



    @Override
    public List<ResultEntityFull> searchResults(ResultSearchDto criteria) {
        Page<ResultEntityFull> page = resultFullRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

    @Cacheable(
        cacheNames = "resultReferenceDataCache",
        key = "#filter.orElse('noFilter')"
    )
    public List<ResultReferenceData> getReferenceData(Optional<String> filter) {

        Sort nameSort = Sort.by(Sort.Direction.ASC, ResultEntityFull_.RESULT_TITLE);

        Page<ResultEntityFull> page = resultFullRepository
            .findBy(specs.referenceDataFilter(filter),
                    ffq -> ffq
                        .sortBy(nameSort)
                        .page(Pageable.unpaged()));

        return page.getContent().stream().map(resultMapper::toRefDataFromFull).toList();
    }

}
