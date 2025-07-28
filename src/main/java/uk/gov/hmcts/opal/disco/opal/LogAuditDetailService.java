package uk.gov.hmcts.opal.disco.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.AddLogAuditDetailDto;
import uk.gov.hmcts.opal.dto.search.LogAuditDetailSearchDto;
import uk.gov.hmcts.opal.entity.LogAuditDetailEntity;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.LogActionRepository;
import uk.gov.hmcts.opal.repository.LogAuditDetailRepository;
import uk.gov.hmcts.opal.repository.jpa.LogAuditDetailSpecs;
import uk.gov.hmcts.opal.disco.LogAuditDetailServiceInterface;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Qualifier("logAuditDetailService")
public class LogAuditDetailService implements LogAuditDetailServiceInterface {

    private final LogAuditDetailRepository logAuditDetailRepository;

    private final LogActionRepository logActionRepository;

    private final BusinessUnitRepository businessUnitRepository;

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

    public void writeLogAuditDetail(AddLogAuditDetailDto dto) {
        logAuditDetailRepository.save(toEntity(dto));
    }

    public LogAuditDetailEntity toEntity(AddLogAuditDetailDto dto) {
        return LogAuditDetailEntity.builder()
            .userId(dto.getUserId())
            .logAction(logActionRepository.getReferenceById(dto.getLogAction().id))
            .accountNumber(dto.getAccountNumber())
            .businessUnit(Optional.ofNullable(dto.getBusinessUnitId())
                              .map(businessUnitRepository::getReferenceById).orElse(null))
            .jsonRequest(dto.getJsonRequest())
            .logTimestamp(LocalDateTime.now())
            .build();
    }


}
