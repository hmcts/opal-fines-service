package uk.gov.hmcts.opal.service.opal;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.ResultDto;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceDataResponse;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.entity.result.ResultEntity.Lite;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceData;
import uk.gov.hmcts.opal.mapper.ResultMapper;
import uk.gov.hmcts.opal.repository.ResultLiteRepository;
import uk.gov.hmcts.opal.repository.jpa.ResultSpecsLite;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Qualifier("resultService")
public class ResultService {

    private final ResultLiteRepository resultLiteRepository;
    private final ResultMapper resultMapper;
    private final ResultSpecsLite specsLite = new ResultSpecsLite();

    public ResultEntity.Lite getLiteResultById(String resultId) {
        return resultLiteRepository.findById(resultId)
            .orElseThrow(() -> new EntityNotFoundException("'Result' not found with id: " + resultId));
    }

    public ResultReferenceData getResultRefDataById(String resultId) {
        return resultMapper.toRefData(getLiteResultById(resultId));
    }

    public ResultDto getResultById(String resultId) {
        // Fetch the full Lite entity (same logic as existing)
        ResultEntity.Lite entity = resultLiteRepository.findById(resultId)
            .orElseThrow(() -> new EntityNotFoundException("'Result' not found with id: " + resultId));

        // Map to full DTO
        return resultMapper.toDto(entity);
    }

    // @Cacheable(cacheNames = "resultReferenceDataByIds", key = "#resultIds.orElse('noIds'))")
    public ResultReferenceDataResponse getResultsByIds(Optional<List<String>> resultIds,
        boolean active,
        boolean manualEnforcement,
        boolean generatesHearing,
        boolean enforcement) {

        Sort idSort = Sort.by(Sort.Direction.ASC, "resultId");

        Page<Lite> page = resultLiteRepository.findBy(
            specsLite.referenceDataByIds(resultIds, active, manualEnforcement, generatesHearing, enforcement),
            ffq -> ffq
                .sortBy(idSort)
                .page(Pageable.unpaged())
        );

        return resultMapper.toReferenceDataResponse(page.getContent());
    }

    public List<ResultEntity.Lite> searchResults(ResultSearchDto criteria) {
        Page<ResultEntity.Lite> page = resultLiteRepository
            .findBy(
                specsLite.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

    @Cacheable(cacheNames = "resultReferenceDataCache", key = "#filter.orElse('noFilter')")
    public List<ResultReferenceData> getReferenceData(Optional<String> filter) {

        Sort nameSort = Sort.by(Sort.Direction.ASC, "resultTitle");

        Page<ResultEntity.Lite> page = resultLiteRepository
            .findBy(
                specsLite.referenceDataFilter(filter),
                    ffq -> ffq
                        .sortBy(nameSort)
                        .page(Pageable.unpaged()));

        return page.getContent().stream().map(resultMapper::toRefData).toList();
    }

}
