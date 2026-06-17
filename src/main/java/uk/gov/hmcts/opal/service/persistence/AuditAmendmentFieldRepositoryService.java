package uk.gov.hmcts.opal.service.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.auditamendmentfield.AuditAmendmentFieldEntity;
import uk.gov.hmcts.opal.repository.AuditAmendmentFieldRepository;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.AuditAmendmentFieldRepositoryService")
public class AuditAmendmentFieldRepositoryService {

    private final AuditAmendmentFieldRepository auditAmendmentFieldRepository;

    @Transactional(readOnly = true)
    public List<AuditAmendmentFieldEntity> findAllById(Iterable<Short> fieldCodes) {
        return auditAmendmentFieldRepository.findAllById(fieldCodes);
    }
}
