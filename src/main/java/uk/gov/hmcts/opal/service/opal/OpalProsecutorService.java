package uk.gov.hmcts.opal.service.opal;

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
import uk.gov.hmcts.opal.entity.ProsecutorEntity;
import uk.gov.hmcts.opal.entity.ProsecutorEntity_;
import uk.gov.hmcts.opal.generated.model.ProsecutorsObject;
import uk.gov.hmcts.opal.generated.model.ProsecutorsReferenceDataResponse;
import uk.gov.hmcts.opal.mapper.ProsecutorMapper;
import uk.gov.hmcts.opal.repository.ProsecutorRepository;
import uk.gov.hmcts.opal.repository.jpa.ProsecutorSpecs;
import uk.gov.hmcts.opal.service.iface.ProsecutorServiceInterface;

@Service
@RequiredArgsConstructor
public class OpalProsecutorService implements ProsecutorServiceInterface {

    private final ProsecutorRepository prosecutorRepository;
    private final ProsecutorMapper prosecutorMapper;
    private final ProsecutorSpecs specs = new ProsecutorSpecs();

    @Override
    @Transactional(readOnly = true)
    public ProsecutorsObject getProsecutorByKey(Long prosecutorKey) {
        Short prosecutorCode = toProsecutorCode(prosecutorKey);
        ProsecutorEntity entity = prosecutorRepository.findById(prosecutorCode)
            .orElseThrow(() -> prosecutorNotFound(prosecutorKey));
        return prosecutorMapper.toProsecutor(entity);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "opalProsecutorReferenceDataCache", key = "#q == null ? 'noFilter' : #q")
    public ProsecutorsReferenceDataResponse getProsecutorsRefData(@Nullable String q) {
        Sort nameSort = Sort.by(Sort.Direction.ASC, ProsecutorEntity_.NAME);
        Page<ProsecutorEntity> page = prosecutorRepository.findBy(
            specs.referenceDataFilter(Optional.ofNullable(q)),
            query -> query.sortBy(nameSort).page(Pageable.unpaged()));
        List<ProsecutorsObject> refData = prosecutorMapper.toProsecutors(page.getContent());
        return ProsecutorsReferenceDataResponse.builder().count(refData.size()).refData(refData).build();
    }

    private Short toProsecutorCode(Long prosecutorKey) {
        if (prosecutorKey == null || prosecutorKey < Short.MIN_VALUE || prosecutorKey > Short.MAX_VALUE) {
            throw prosecutorNotFound(prosecutorKey);
        }
        return prosecutorKey.shortValue();
    }

    private EntityNotFoundException prosecutorNotFound(Long prosecutorKey) {
        return new EntityNotFoundException("Prosecutor not found with key: " + prosecutorKey);
    }
}
