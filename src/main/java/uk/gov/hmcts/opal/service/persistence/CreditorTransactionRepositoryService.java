package uk.gov.hmcts.opal.service.persistence;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.repository.CreditorTransactionRepository;
import uk.gov.hmcts.opal.repository.projection.MinorCreditorTransactionHistoryProjection;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.CreditorTransactionRepositoryService")
public class CreditorTransactionRepositoryService {

    private final CreditorTransactionRepository creditorTransactionRepository;

    @Transactional(readOnly = true)
    public List<MinorCreditorTransactionHistoryProjection> findCreditorTransactionHistory(
        Long creditorAccountId,
        LocalDateTime postedFromInclusive,
        LocalDateTime postedToExclusive
    ) {
        return creditorTransactionRepository.findMinorCreditorHistory(
            creditorAccountId,
            postedFromInclusive,
            postedToExclusive
        );
    }
}
