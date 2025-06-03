package uk.gov.hmcts.opal.service.opal;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.EnforcerSearchDto;
import uk.gov.hmcts.opal.entity.AddressEntity_;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorEntity_;
import uk.gov.hmcts.opal.entity.projection.EnforcerReferenceData;
import uk.gov.hmcts.opal.repository.EnforcerRepository;
import uk.gov.hmcts.opal.repository.jpa.EnforcerSpecs;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.EnforcerService")
@Qualifier("enforcerService")
public class EnforcerService {

    private final EnforcerRepository enforcerRepository;

    private final EnforcerSpecs specs = new EnforcerSpecs();

    public EnforcerEntity getEnforcerById(long enforcerId) {
        return enforcerRepository.findById(enforcerId)
            .orElseThrow(() -> new EntityNotFoundException("Enforcer not found with id: " + enforcerId));
    }

    public List<EnforcerEntity> searchEnforcers(EnforcerSearchDto criteria) {
        log.info(":searchEnforcers: criteria: {}", criteria);

        Sort nameSort = Sort.by(Sort.Direction.ASC, AddressEntity_.NAME);

        Page<EnforcerEntity> page = enforcerRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq
                        .sortBy(nameSort)
                        .page(Pageable.unpaged()));

        return page.getContent();
    }

    @Cacheable(cacheNames = "enforcerReferenceDataCache", key = "#filter.orElse('noFilter')")
    public List<EnforcerReferenceData> getReferenceData(Optional<String> filter) {

        // Note - might we want to sort by Welsh name?
        Sort nameSort = Sort.by(Sort.Direction.ASC, MajorCreditorEntity_.NAME);

        Page<EnforcerEntity> page = enforcerRepository
            .findBy(specs.referenceDataFilter(filter),
                    ffq -> ffq
                        .sortBy(nameSort)
                        .page(Pageable.unpaged()));

        return page.getContent().stream().map(this::toRefData).toList();
    }

    private EnforcerReferenceData toRefData(EnforcerEntity entity) {
        return new EnforcerReferenceData(
            entity.getEnforcerId(),
            entity.getEnforcerCode(),
            entity.getName(),
            entity.getNameCy()
        );
    }
}
