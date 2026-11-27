package uk.gov.hmcts.opal.service.opal;

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
import uk.gov.hmcts.opal.entity.OpalLocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.OpalLocalJusticeAreaEntity_;
import uk.gov.hmcts.opal.generated.model.LocalJusticeArea;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreaSummary;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreaType;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreasResponse;
import uk.gov.hmcts.opal.mapper.OpalLocalJusticeAreaMapper;
import uk.gov.hmcts.opal.repository.OpalLocalJusticeAreaRepository;
import uk.gov.hmcts.opal.repository.jpa.OpalLocalJusticeAreaSpecs;
import uk.gov.hmcts.opal.service.iface.LocalJusticeAreaServiceInterface;

@Service
@RequiredArgsConstructor
public class OpalLocalJusticeAreaService implements LocalJusticeAreaServiceInterface {

    private final OpalLocalJusticeAreaRepository opalLocalJusticeAreaRepository;
    private final OpalLocalJusticeAreaMapper opalLocalJusticeAreaMapper;
    private final Clock clock;

    private final OpalLocalJusticeAreaSpecs specs = new OpalLocalJusticeAreaSpecs();

    @Override
    @Transactional(readOnly = true)
    public LocalJusticeArea getLocalJusticeAreaById(Short localJusticeAreaId) {
        OpalLocalJusticeAreaEntity entity = opalLocalJusticeAreaRepository.findById(localJusticeAreaId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Local Justice Area not found with id: " + localJusticeAreaId));
        return opalLocalJusticeAreaMapper.toLocalJusticeArea(entity);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "opalLjaReferenceDataCache", keyGenerator = "KeyGeneratorForOptionalList")
    public LocalJusticeAreasResponse getLocalJusticeAreaRefData(
        @Nullable String q, @Nullable List<LocalJusticeAreaType> ljaTypes) {

        Sort nameSort = Sort.by(Sort.Direction.ASC, OpalLocalJusticeAreaEntity_.NAME);
        Page<OpalLocalJusticeAreaEntity> page = opalLocalJusticeAreaRepository.findBy(
            specs.referenceDataFilter(q, ljaTypes, LocalDateTime.now(clock)),
            query -> query.sortBy(nameSort).page(Pageable.unpaged()));
        List<LocalJusticeAreaSummary> refData = opalLocalJusticeAreaMapper
            .toLocalJusticeAreaSummaries(page.getContent());

        return LocalJusticeAreasResponse.builder().count(refData.size()).refData(refData).build();
    }
}
