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
import uk.gov.hmcts.opal.dto.reference.ProsecutorReferenceData;
import uk.gov.hmcts.opal.entity.ProsecutorEntity;
import uk.gov.hmcts.opal.entity.ProsecutorEntity_;
import uk.gov.hmcts.opal.mapper.ProsecutorMapper;
import uk.gov.hmcts.opal.repository.ProsecutorRepository;
import uk.gov.hmcts.opal.repository.jpa.ProsecutorSpecs;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.ProsecutorService")
@Qualifier("prosecutorService")
public class ProsecutorService {

    private final ProsecutorRepository prosecutorRepository;

    private final ProsecutorMapper prosecutorMapper;
    private final ProsecutorSpecs specs = new ProsecutorSpecs();


    public ProsecutorEntity getProsecutorById(long prosecutorId) {
        return prosecutorRepository.findById(prosecutorId)
            .orElseThrow(() -> new EntityNotFoundException("Prosecutor not found with id: " + prosecutorId));
    }


    @Cacheable(cacheNames = "prosecutorReferenceDataCache", key = "#filter.orElse('noFilter')")
    public List<ProsecutorReferenceData> getReferenceData(Optional<String> filter) {

        Sort nameSort = Sort.by(Sort.Direction.ASC, ProsecutorEntity_.NAME);

        Page<ProsecutorEntity> page = prosecutorRepository
            .findBy(specs.referenceDataFilter(filter),
                    ffq -> ffq
                        .sortBy(nameSort)
                        .page(Pageable.unpaged()));

        return page.getContent().stream().map(prosecutorMapper::toRefData).toList();
    }

}
