package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.generated.model.PrisonsPrison;
import uk.gov.hmcts.opal.generated.model.PrisonsPrisonSummary;
import uk.gov.hmcts.opal.generated.model.PrisonsReferenceDataResponse;
import uk.gov.hmcts.opal.mapper.PrisonMapper;
import uk.gov.hmcts.opal.repository.PrisonRepository;

@Service
@RequiredArgsConstructor
public class PrisonService {

    private final PrisonRepository prisonRepository;
    private final PrisonMapper prisonMapper;

    @Transactional(readOnly = true)
    public PrisonsPrison getPrisonById(Long prisonId) {
        PrisonEntity prison = prisonRepository.findById(prisonId)
            .orElseThrow(() -> new EntityNotFoundException("Prison not found with id: " + prisonId));
        return prisonMapper.toDto(prison);
    }

    @Cacheable("prisonReferenceDataCache")
    @Transactional(readOnly = true)
    public PrisonsReferenceDataResponse getPrisonsRefData() {
        List<PrisonsPrisonSummary> prisons = prisonRepository.findAll().stream()
            .sorted(Comparator.comparing(PrisonEntity::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
            .map(prisonMapper::toSummary)
            .toList();
        return PrisonsReferenceDataResponse.builder().count(prisons.size()).prisons(prisons).build();
    }
}
