package uk.gov.hmcts.opal.service.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.result.ResultEntity.Lite;
import uk.gov.hmcts.opal.repository.ResultRepository;

import java.util.Optional;

@Service
@Slf4j(topic = "opal.ResultRepositoryService")
@RequiredArgsConstructor
public class ResultRepositoryService {

    private final ResultRepository resultRepository;

    @Transactional(readOnly = true)
    public Optional<Lite> getResultById(String resultId) {
        return Optional.ofNullable(resultId).flatMap(resultRepository::findById);
    }
}
