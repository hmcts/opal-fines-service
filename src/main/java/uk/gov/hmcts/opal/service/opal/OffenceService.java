package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.OffenceSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.OffenceEntity;
import uk.gov.hmcts.opal.entity.OffenceEntity_;
import uk.gov.hmcts.opal.entity.projection.OffenceReferenceData;
import uk.gov.hmcts.opal.entity.projection.OffenceSearchData;
import uk.gov.hmcts.opal.repository.OffenceRepository;
import uk.gov.hmcts.opal.repository.jpa.OffenceSpecs;
import uk.gov.hmcts.opal.service.OffenceServiceInterface;

import java.util.List;
import java.util.Optional;
import java.util.Collections;


@Service
@RequiredArgsConstructor
@Qualifier("offenceService")
public class OffenceService implements OffenceServiceInterface {

    private static final int NO_REQUESTED_LIMIT = 0;

    private final OffenceRepository offenceRepository;

    private final OffenceSpecs specs = new OffenceSpecs();

    public OffenceEntity getOffence(long offenceId) {
        return offenceRepository.findById(offenceId).orElse(null);
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

        return page.getContent().stream().map(this::toSearchData).toList();
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

        return page.getContent().stream().map(OffenceService::toRefData).toList();
    }

    public static OffenceReferenceData toRefData(OffenceEntity entity) {
        return new OffenceReferenceData(
            entity.getOffenceId(),
            entity.getCjsCode(),
            Optional.ofNullable(entity.getBusinessUnit()).map(BusinessUnitEntity::getBusinessUnitId).orElse(null),
            entity.getOffenceTitle(),
            entity.getOffenceTitleCy(),
            entity.getDateUsedFrom(),
            entity.getDateUsedTo(),
            entity.getOffenceOas(),
            entity.getOffenceOasCy()
        );
    }

    private OffenceSearchData toSearchData(OffenceEntity entity) {
        return new OffenceSearchData(
            entity.getOffenceId(),
            entity.getCjsCode(),
            entity.getOffenceTitle(),
            entity.getOffenceTitleCy(),
            entity.getDateUsedFrom(),
            entity.getDateUsedTo(),
            entity.getOffenceOas(),
            entity.getOffenceOasCy()
        );
    }
}
