package uk.gov.hmcts.opal.service.opal;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.OffenceSearchDto;
import uk.gov.hmcts.opal.entity.offence.OffenceEntity;
import uk.gov.hmcts.opal.entity.offence.OffenceEntityFull;
import uk.gov.hmcts.opal.entity.offence.OffenceEntity_;
import uk.gov.hmcts.opal.entity.projection.OffenceReferenceData;
import uk.gov.hmcts.opal.entity.projection.OffenceSearchData;
import uk.gov.hmcts.opal.repository.OffenceRepository;
import uk.gov.hmcts.opal.repository.OffenceRepositoryFull;
import uk.gov.hmcts.opal.repository.jpa.OffenceSpecs;
import uk.gov.hmcts.opal.service.OffenceServiceInterface;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Qualifier("offenceService")
public class OffenceService implements OffenceServiceInterface {

    private static final int NO_REQUESTED_LIMIT = 0;

    private final OffenceRepository offenceRepository;

    private final OffenceRepositoryFull offenceRepositoryFull;

    private final OffenceSpecs specs = new OffenceSpecs();

    public OffenceEntity getOffenceById(long offenceId) {
        return offenceRepository.findById(offenceId)
            .orElseThrow(() -> new EntityNotFoundException("Offence not found with id: "
                                                               + offenceId));
    }

    public OffenceEntityFull getOffenceByIdFull(long offenceId) {
        return offenceRepositoryFull.findById(offenceId)
            .orElseThrow(() -> new EntityNotFoundException("Offence not found with id: "
                                                               + offenceId));
    }

    @Cacheable(cacheNames = "offenceSearchDataCache", key = "#criteria")
    public List<OffenceSearchData> searchOffences(OffenceSearchDto criteria) {
        Sort codeSort = Sort.by(Sort.Direction.ASC, OffenceEntity_.CJS_CODE);
        int limit = Optional.ofNullable(criteria.getMaxResults()).orElse(NO_REQUESTED_LIMIT);

        Page<OffenceEntity.Lite> page = offenceRepository
            .findBy(
                specs.findBySearchCriteria(criteria),
                ffq -> ffq
                    .sortBy(codeSort)
                    .limit(limit)
                    .page(Pageable.unpaged())
            );

        return page.getContent().stream().map(this::toSearchData).toList();
    }

    @Cacheable(
        cacheNames = "offenceReferenceDataCache",
        key = "#filter.orElse('noFilter') + '_' + #businessUnitId.orElse('noBU')"
    )
    public List<OffenceReferenceData> getReferenceData(Optional<String> filter, Optional<Short> businessUnitId) {

        Sort codeSort = Sort.by(Sort.Direction.ASC, OffenceEntity_.CJS_CODE);

        Page<OffenceEntity.Lite> page = offenceRepository
            .findBy(
                specs.referenceDataFilter(filter, businessUnitId),
                ffq -> ffq
                    .sortBy(codeSort)
                    .page(Pageable.unpaged())
            );

        return page.getContent().stream().map(this::toRefData).toList();
    }

    private OffenceReferenceData toRefData(OffenceEntity entity) {
        return new OffenceReferenceData(
            entity.getOffenceId(),
            entity.getCjsCode(),
            Optional.ofNullable(entity.getBusinessUnitId()).orElse(null),
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
