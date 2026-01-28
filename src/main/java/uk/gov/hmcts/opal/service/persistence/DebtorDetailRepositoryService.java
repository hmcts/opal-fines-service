package uk.gov.hmcts.opal.service.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;

import java.util.Optional;

@Service
@Slf4j(topic = "opal.DebtorDetailRepositoryService")
@RequiredArgsConstructor
public class DebtorDetailRepositoryService {

    private final DebtorDetailRepository debtorDetailRepository;

    @Transactional(readOnly = true)
    public Optional<DebtorDetailEntity> findById(Long id) {
        return debtorDetailRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<DebtorDetailEntity> findByPartyId(Long partyId) {
        return debtorDetailRepository.findByPartyId(partyId);
    }

    @Transactional
    public DebtorDetailEntity save(DebtorDetailEntity debtorDetailEntity) {
        return debtorDetailRepository.save(debtorDetailEntity);
    }
}
