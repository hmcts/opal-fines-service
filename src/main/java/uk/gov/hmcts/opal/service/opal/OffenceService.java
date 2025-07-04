package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.reference.OffenceReferenceData;
import uk.gov.hmcts.opal.dto.reference.OffenceSearchData;
import uk.gov.hmcts.opal.dto.search.OffenceSearchDto;
import uk.gov.hmcts.opal.entity.offence.OffenceEntity;
import uk.gov.hmcts.opal.entity.offence.OffenceEntityFull;
import uk.gov.hmcts.opal.entity.offence.OffenceEntity_;
import uk.gov.hmcts.opal.mapper.OffenceMapper;
import uk.gov.hmcts.opal.repository.OffenceRepository;
import uk.gov.hmcts.opal.repository.OffenceRepositoryFull;
import uk.gov.hmcts.opal.repository.jpa.OffenceSpecs;
import uk.gov.hmcts.opal.service.OffenceServiceInterface;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Qualifier("offenceService")
public class OffenceService implements OffenceServiceInterface {

    private static final int NO_REQUESTED_LIMIT = 0;

    private final OffenceRepository offenceRepository;

    private final OffenceRepositoryFull offenceRepositoryFull;

    private final OffenceMapper offenceMapper;

    private final OffenceSpecs specs = new OffenceSpecs();

    public OffenceEntityFull getOffence(long offenceId) {
        return offenceRepositoryFull.findById(offenceId).orElse(null);
    }

    @Cacheable(cacheNames = "offenceSearchDataCache", key = "#criteria")
    public List<OffenceSearchData> searchOffences(OffenceSearchDto criteria) {
        Sort codeSort = Sort.by(Sort.Direction.ASC, OffenceEntity_.CJS_CODE);
        int limit = Optional.ofNullable(criteria.getMaxResults()).orElse(NO_REQUESTED_LIMIT);

        Page<OffenceEntity> page = offenceRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq
                        .sortBy(codeSort)
                        .limit(limit)
                        .page(Pageable.unpaged()));

        return page.getContent().stream().map(offenceMapper::toSearchData).toList();
    }


    @Cacheable(
        cacheNames = "offenceReferenceDataCache",
        key = "#filter.orElse('noFilter') + '_' + "
            + "#businessUnitId.orElse('noBU') + '_' + "
            + "#optionalCjsCode.orElse(T(java.util.Collections).emptyList())"
            + ".stream().collect(T(java.util.stream.Collectors).joining(','))"
    )
    public List<OffenceReferenceData> getReferenceData(Optional<String> filter, Optional<Short> businessUnitId,
                                                       Optional<List<String>> optionalCjsCode) {

        List<String> cjsCode = optionalCjsCode.orElse(Collections.emptyList());

        Sort codeSort = Sort.by(Sort.Direction.ASC, OffenceEntity_.CJS_CODE);

        Page<OffenceEntity> page = offenceRepository
            .findBy(specs.referenceDataFilter(filter, businessUnitId, cjsCode),
                    ffq -> ffq
                        .sortBy(codeSort)
                        .page(Pageable.unpaged()));

        return page.getContent().stream().map(offenceMapper::toRefData).toList();
    }

}
