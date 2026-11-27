package uk.gov.hmcts.opal.service.persistence;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.OpalLocalJusticeAreaEntity;
import uk.gov.hmcts.opal.repository.OpalLocalJusticeAreaRepository;

@Service
@Slf4j(topic = "opal.OpalLocalJusticeAreaRepositoryService")
@RequiredArgsConstructor
public class OpalLocalJusticeAreaRepositoryService {
    private final OpalLocalJusticeAreaRepository opalLocalJusticeAreaRepository;

    @Transactional(readOnly = true)
    public Optional<OpalLocalJusticeAreaEntity> getLjaById(short id) {
        return Optional.ofNullable(id)
            .flatMap(opalLocalJusticeAreaRepository::findById);
    }
}
