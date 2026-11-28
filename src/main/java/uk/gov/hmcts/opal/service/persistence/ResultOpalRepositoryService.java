package uk.gov.hmcts.opal.service.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.result.ResultOpalEntity;
import uk.gov.hmcts.opal.repository.ResultOpalRepository;

import java.util.Optional;

@Service
@Slf4j(topic = "opal.ResultOpalRepositoryService")
@RequiredArgsConstructor
public class ResultOpalRepositoryService {

    private final ResultOpalRepository resultRepository;

    @Transactional(readOnly = true)
    public Optional<ResultOpalEntity> getResultById(String resultId) {
        return Optional.ofNullable(resultId).flatMap(resultRepository::findById);
    }
}
