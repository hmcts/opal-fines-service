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
import uk.gov.hmcts.opal.entity.OpalProsecutorEntity;
import uk.gov.hmcts.opal.entity.OpalProsecutorEntity_;
import uk.gov.hmcts.opal.generated.model.ProsecutorsObject;
import uk.gov.hmcts.opal.generated.model.ProsecutorsReferenceDataResponse;
import uk.gov.hmcts.opal.mapper.OpalProsecutorMapper;
import uk.gov.hmcts.opal.repository.OpalProsecutorRepository;
import uk.gov.hmcts.opal.repository.jpa.OpalProsecutorSpecs;
import uk.gov.hmcts.opal.service.iface.ProsecutorServiceInterface;

@Service
@RequiredArgsConstructor
public class OpalProsecutorService implements ProsecutorServiceInterface {

    private final OpalProsecutorRepository opalProsecutorRepository;
    private final OpalProsecutorMapper opalProsecutorMapper;
    private final OpalProsecutorSpecs specs = new OpalProsecutorSpecs();

    @Override
    @Transactional(readOnly = true)
    public ProsecutorsObject getProsecutorByKey(String prosecutorKey) {
        OpalProsecutorEntity entity = opalProsecutorRepository.findById(prosecutorKey)
            .orElseThrow(() -> prosecutorNotFound(prosecutorKey));
        return opalProsecutorMapper.toProsecutor(entity);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "opalProsecutorReferenceDataCache", key = "#q == null ? 'noFilter' : #q")
    public ProsecutorsReferenceDataResponse getProsecutorsRefData(@Nullable String q) {
        Sort nameSort = Sort.by(Sort.Direction.ASC, OpalProsecutorEntity_.NAME);
        Page<OpalProsecutorEntity> page = opalProsecutorRepository.findBy(
            specs.referenceDataFilter(Optional.ofNullable(q)),
            query -> query.sortBy(nameSort).page(Pageable.unpaged()));
        List<ProsecutorsObject> refData = opalProsecutorMapper.toProsecutors(page.getContent());
        return ProsecutorsReferenceDataResponse.builder().count(refData.size()).refData(refData).build();
    }

    private EntityNotFoundException prosecutorNotFound(String prosecutorKey) {
        return new EntityNotFoundException("Prosecutor not found with key: " + prosecutorKey);
    }
}
