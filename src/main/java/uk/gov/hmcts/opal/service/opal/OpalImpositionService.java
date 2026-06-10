package uk.gov.hmcts.opal.service.opal;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.projection.DefendantAccountImpositionData;
import uk.gov.hmcts.opal.mapper.DefendantAccountImpositionMapper;
import uk.gov.hmcts.opal.repository.ImpositionRepository;
import uk.gov.hmcts.opal.service.iface.ImpositionServiceInterface;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;

@Service
@Slf4j(topic = "opal.OpalImpositionService")
@RequiredArgsConstructor
public class OpalImpositionService implements ImpositionServiceInterface {

    private final DefendantAccountRepositoryService defendantAccountRepositoryService;

    private final ImpositionRepository impositionRepository;

    private final DefendantAccountImpositionMapper defendantAccountImpositionMapper;

    @Override
    @Transactional(readOnly = true)
    public GetDefendantAccountImpositionsResponse getImpositions(Long defendantAccountId) {
        log.debug(":getImpositions (Opal): id={}", defendantAccountId);

        // Find the DefendantAccountEntity by ID
        DefendantAccountEntity account = defendantAccountRepositoryService.findById(defendantAccountId);

        List<DefendantAccountImpositionData> impositions =
            impositionRepository.findDefendantAccountImpositionsByDefendantAccountId(
                defendantAccountId);

        return GetDefendantAccountImpositionsResponse.builder()
            .payload(defendantAccountImpositionMapper.toResponse(impositions))
            .version(account.getVersion())
            .build();
    }
}
