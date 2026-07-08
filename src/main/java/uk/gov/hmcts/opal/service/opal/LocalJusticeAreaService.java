package uk.gov.hmcts.opal.service.opal;


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
import uk.gov.hmcts.opal.dto.reference.LjaReferenceData;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity_;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.repository.jpa.LocalJusticeAreaSpecs;
import uk.gov.hmcts.opal.util.SearchResultLimits;

@Service
@RequiredArgsConstructor
@Qualifier("localJusticeAreaService")
public class LocalJusticeAreaService {

    private final LocalJusticeAreaRepository localJusticeAreaRepository;

    private final Clock clock;

    private final LocalJusticeAreaSpecs specs = new LocalJusticeAreaSpecs();

    public LocalJusticeAreaEntity getLocalJusticeAreaById(short ljaId) {
        return localJusticeAreaRepository.findById(ljaId)
            .orElseThrow(() -> new EntityNotFoundException("Local Justice Area not found with id: " + ljaId));
    }

    public List<LocalJusticeAreaEntity> searchLocalJusticeAreas(LocalJusticeAreaSearchDto criteria) {

        Sort nameSort = Sort.by(Sort.Direction.ASC, LocalJusticeAreaEntity_.NAME);

        Page<LocalJusticeAreaEntity> page = localJusticeAreaRepository
            .findBy(specs.findBySearchCriteria(criteria),
                ffq -> ffq
                    .sortBy(nameSort)
                    .page(SearchResultLimits.defaultPage()));

        return page.getContent();
    }

    @Cacheable(cacheNames = "ljaReferenceDataCache", keyGenerator = "KeyGeneratorForOptionalList")
    public List<LjaReferenceData> getReferenceData(Optional<String> filter, Optional<List<String>> ljaType) {

        Sort nameSort = Sort.by(Sort.Direction.ASC, LocalJusticeAreaEntity_.NAME);

        Page<LocalJusticeAreaEntity> page = localJusticeAreaRepository
            .findBy(specs.referenceDataFilter(filter, ljaType, LocalDateTime.now(clock)),
                ffq -> ffq
                    .sortBy(nameSort)
                    .page(Pageable.unpaged()));

        return page.getContent().stream().map(this::toReferenceData).toList();
    }

    private LjaReferenceData toReferenceData(LocalJusticeAreaEntity entity) {
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
