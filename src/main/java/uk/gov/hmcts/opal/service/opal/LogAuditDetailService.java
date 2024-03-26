package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.LogAuditDetailSearchDto;
import uk.gov.hmcts.opal.entity.LogAuditDetailEntity;
import uk.gov.hmcts.opal.repository.LogAuditDetailRepository;
import uk.gov.hmcts.opal.repository.jpa.LogAuditDetailSpecs;
import uk.gov.hmcts.opal.service.LogAuditDetailServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("logAuditDetailService")
public class LogAuditDetailService implements LogAuditDetailServiceInterface {

    private final LogAuditDetailRepository logAuditDetailRepository;

    private final LogAuditDetailSpecs specs = new LogAuditDetailSpecs();

    @Override
    public LogAuditDetailEntity getLogAuditDetail(long logAuditDetailId) {
        return logAuditDetailRepository.getReferenceById(logAuditDetailId);
    }

    @Override
    public List<LogAuditDetailEntity> searchLogAuditDetails(LogAuditDetailSearchDto criteria) {
        Page<LogAuditDetailEntity> page = logAuditDetailRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
