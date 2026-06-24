package uk.gov.hmcts.opal.service.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.repository.ImpositionRepository;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.ImpositionRepositoryService")
public class ImpositionRepositoryService {

    private final ImpositionRepository impositionRepository;

    @Transactional(readOnly = true)
    public List<ImpositionEntity> findAllById(Iterable<Long> impositionIds) {
        return impositionRepository.findAllById(impositionIds);
    }
}
