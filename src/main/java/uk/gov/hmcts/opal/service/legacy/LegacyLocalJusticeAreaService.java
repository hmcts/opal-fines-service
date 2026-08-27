package uk.gov.hmcts.opal.service.legacy;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.reference.LjaReferenceData;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LegacyLocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.LegacyLocalJusticeAreaEntity_;
import uk.gov.hmcts.opal.repository.LegacyLocalJusticeAreaRepository;
import uk.gov.hmcts.opal.repository.jpa.LegacyLocalJusticeAreaSpecs;

@Service
@RequiredArgsConstructor
@Qualifier("legacyLocalJusticeAreaService")
@Transactional(readOnly = true)
public class LegacyLocalJusticeAreaService {

    private final LegacyLocalJusticeAreaRepository localJusticeAreaRepository;

    private final Clock clock;

    private final LegacyLocalJusticeAreaSpecs specs = new LegacyLocalJusticeAreaSpecs();

    public LegacyLocalJusticeAreaEntity getLocalJusticeAreaById(short ljaId) {
        return localJusticeAreaRepository.findById(ljaId)
            .orElseThrow(() -> new EntityNotFoundException("Local Justice Area not found with id: " + ljaId));
    }

    public List<LegacyLocalJusticeAreaEntity> searchLocalJusticeAreas(LocalJusticeAreaSearchDto criteria) {

        Sort nameSort = Sort.by(Sort.Direction.ASC, LegacyLocalJusticeAreaEntity_.NAME);

        Page<LegacyLocalJusticeAreaEntity> page = localJusticeAreaRepository
            .findBy(specs.findBySearchCriteria(criteria),
                ffq -> ffq
                    .sortBy(nameSort)
                    .page(Pageable.unpaged()));

        return page.getContent();
    }

    @Cacheable(cacheNames = "legacyLjaReferenceDataCache", keyGenerator = "KeyGeneratorForOptionalList")
    public List<LjaReferenceData> getReferenceData(Optional<String> filter, Optional<List<String>> ljaType) {

        Sort nameSort = Sort.by(Sort.Direction.ASC, LegacyLocalJusticeAreaEntity_.NAME);

        Page<LegacyLocalJusticeAreaEntity> page = localJusticeAreaRepository
            .findBy(specs.referenceDataFilter(filter, ljaType, LocalDateTime.now(clock)),
                ffq -> ffq
                    .sortBy(nameSort)
                    .page(Pageable.unpaged()));

        return page.getContent().stream().map(this::toReferenceData).toList();
    }

    private LjaReferenceData toReferenceData(LegacyLocalJusticeAreaEntity entity) {
        return new LjaReferenceData(
            entity.getLocalJusticeAreaId(),
            entity.getLjaCode(),
            Optional.ofNullable(entity.getLjaType()).map(Enum::name).orElse(null),
            entity.getName(),
            entity.getAddressLine1(),
            entity.getPostcode()
        );
    }
}
