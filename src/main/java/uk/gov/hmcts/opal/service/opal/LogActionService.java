package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.LogActionSearchDto;
import uk.gov.hmcts.opal.entity.LogActionEntity;
import uk.gov.hmcts.opal.repository.LogActionRepository;
import uk.gov.hmcts.opal.repository.jpa.LogActionSpecs;
import uk.gov.hmcts.opal.service.LogActionServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("logActionService")
public class LogActionService implements LogActionServiceInterface {

    private final LogActionRepository logActionRepository;

    private final LogActionSpecs specs = new LogActionSpecs();

    @Override
    public LogActionEntity getLogAction(short logActionId) {
        return logActionRepository.getReferenceById(logActionId);
    }

    @Override
    public List<LogActionEntity> searchLogActions(LogActionSearchDto criteria) {
        Page<LogActionEntity> page = logActionRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
