package uk.gov.hmcts.opal.service.legacy;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaLegacyEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaLegacyEntity_;
import uk.gov.hmcts.opal.generated.model.LocalJusticeArea;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreaSummary;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreaType;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreasResponse;
import uk.gov.hmcts.opal.mapper.LocalJusticeAreaLegacyMapper;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaLegacyRepository;
import uk.gov.hmcts.opal.repository.jpa.LocalJusticeAreaLegacySpecs;
import uk.gov.hmcts.opal.service.iface.LocalJusticeAreaServiceInterface;

@Service
@RequiredArgsConstructor
public class LegacyLocalJusticeAreaService implements LocalJusticeAreaServiceInterface {

    private final LocalJusticeAreaLegacyRepository localJusticeAreaLegacyRepository;
    private final LocalJusticeAreaLegacyMapper localJusticeAreaLegacyMapper;
    private final Clock clock;

    private final LocalJusticeAreaLegacySpecs specs = new LocalJusticeAreaLegacySpecs();

    @Override
    @Transactional(readOnly = true)
    public LocalJusticeArea getLocalJusticeAreaById(Short localJusticeAreaId) {
        return localJusticeAreaLegacyMapper.toLocalJusticeArea(getLocalJusticeAreaEntityById(localJusticeAreaId));
    }

    @Transactional(readOnly = true)
    public LocalJusticeAreaLegacyEntity getLocalJusticeAreaEntityById(Short localJusticeAreaId) {
        return localJusticeAreaLegacyRepository.findById(localJusticeAreaId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Local Justice Area not found with id: " + localJusticeAreaId));
    }

    @Transactional(readOnly = true)
    public List<LocalJusticeAreaLegacyEntity> searchLocalJusticeAreas(LocalJusticeAreaSearchDto criteria) {
        Sort nameSort = Sort.by(Sort.Direction.ASC, LocalJusticeAreaLegacyEntity_.NAME);
        Page<LocalJusticeAreaLegacyEntity> page = localJusticeAreaLegacyRepository.findBy(
            specs.findBySearchCriteria(criteria),
            query -> query.sortBy(nameSort).page(Pageable.unpaged()));
        return page.getContent();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "legacyLjaReferenceDataCache", keyGenerator = "KeyGeneratorForOptionalList")
    public LocalJusticeAreasResponse getLocalJusticeAreaRefData(
        @Nullable String q, @Nullable List<LocalJusticeAreaType> ljaTypes) {

        Sort nameSort = Sort.by(Sort.Direction.ASC, LocalJusticeAreaLegacyEntity_.NAME);
        Page<LocalJusticeAreaLegacyEntity> page = localJusticeAreaLegacyRepository.findBy(
            specs.referenceDataFilter(q, ljaTypes, LocalDateTime.now(clock)),
            query -> query.sortBy(nameSort).page(Pageable.unpaged()));
        List<LocalJusticeAreaSummary> refData = localJusticeAreaLegacyMapper
            .toLocalJusticeAreaSummaries(page.getContent());

        return LocalJusticeAreasResponse.builder().count(refData.size()).refData(refData).build();
    }
}
