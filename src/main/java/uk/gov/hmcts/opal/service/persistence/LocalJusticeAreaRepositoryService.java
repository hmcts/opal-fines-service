package uk.gov.hmcts.opal.service.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;

import java.util.Optional;

@Service
@Slf4j(topic = "opal.LocalJusticeAreaRepositoryService")
@RequiredArgsConstructor
public class LocalJusticeAreaRepositoryService {
    private final LocalJusticeAreaRepository localJusticeAreaRepository;

    @Transactional(readOnly = true)
    public Optional<LocalJusticeAreaEntity> getLjaById(short id) {
        return Optional.ofNullable(id)
            .flatMap(localJusticeAreaRepository::findById);
    }
}
