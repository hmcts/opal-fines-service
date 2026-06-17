package uk.gov.hmcts.opal.service;

import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountHeaderViewEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountHeaderViewRepository;

@Service
@Slf4j(topic = "opal.DefendantAccountHeaderViewService")
@RequiredArgsConstructor
public class DefendantAccountHeaderViewService {

    private final DefendantAccountHeaderViewRepository repository;

    public BigDecimal getArrearsTotalForDefendantAccount(Long defendantAccountId) {
        Optional<DefendantAccountHeaderViewEntity> entity = repository.findById(defendantAccountId);
        if (entity.isPresent()) {
            return entity.get().getArrears();
        }
        return BigDecimal.ZERO;
    }
}
