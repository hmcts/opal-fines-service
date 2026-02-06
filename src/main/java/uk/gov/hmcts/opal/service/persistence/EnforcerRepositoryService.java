package uk.gov.hmcts.opal.service.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.repository.EnforcerRepository;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j(topic = "opal.EnforcerRepositoryService")
@RequiredArgsConstructor
public class EnforcerRepositoryService {

    private final EnforcerRepository enforcerRepository;

    @Transactional(readOnly = true)
    public Optional<EnforcerEntity> findById(Long enforcerId) {
        return Optional.ofNullable(enforcerId)
            .flatMap(enforcerRepository::findById)
            .filter(enf -> Objects.nonNull(enf.getEnforcerId()));
    }
}
