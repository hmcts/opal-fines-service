package uk.gov.hmcts.opal.service.legacy;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.ProsecutorLegacyEntity;
import uk.gov.hmcts.opal.entity.ProsecutorLegacyEntity_;
import uk.gov.hmcts.opal.generated.model.ProsecutorsObject;
import uk.gov.hmcts.opal.generated.model.ProsecutorsReferenceDataResponse;
import uk.gov.hmcts.opal.mapper.ProsecutorLegacyMapper;
import uk.gov.hmcts.opal.repository.ProsecutorLegacyRepository;
import uk.gov.hmcts.opal.repository.jpa.ProsecutorLegacySpecs;
import uk.gov.hmcts.opal.service.iface.ProsecutorServiceInterface;

@Service
@RequiredArgsConstructor
public class LegacyProsecutorService implements ProsecutorServiceInterface {

    private final ProsecutorLegacyRepository prosecutorLegacyRepository;
    private final ProsecutorLegacyMapper prosecutorLegacyMapper;
    private final ProsecutorLegacySpecs specs = new ProsecutorLegacySpecs();

    @Override
    @Transactional(readOnly = true)
    public ProsecutorsObject getProsecutorByKey(Long prosecutorKey) {
        ProsecutorLegacyEntity entity = prosecutorLegacyRepository.findById(prosecutorKey)
            .orElseThrow(() -> new EntityNotFoundException("Prosecutor not found with key: " + prosecutorKey));
        return prosecutorLegacyMapper.toProsecutor(entity);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "legacyProsecutorReferenceDataCache", key = "#q == null ? 'noFilter' : #q")
    public ProsecutorsReferenceDataResponse getProsecutorsRefData(@Nullable String q) {
        Sort nameSort = Sort.by(Sort.Direction.ASC, ProsecutorLegacyEntity_.NAME);
        Page<ProsecutorLegacyEntity> page = prosecutorLegacyRepository.findBy(
            specs.referenceDataFilter(Optional.ofNullable(q)),
            query -> query.sortBy(nameSort).page(Pageable.unpaged()));
        List<ProsecutorsObject> refData = prosecutorLegacyMapper.toProsecutors(page.getContent());
        return ProsecutorsReferenceDataResponse.builder().count(refData.size()).refData(refData).build();
    }
}
