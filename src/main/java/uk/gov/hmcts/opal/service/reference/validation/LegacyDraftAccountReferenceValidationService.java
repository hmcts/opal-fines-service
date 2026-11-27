package uk.gov.hmcts.opal.service.reference.validation;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.ProsecutorLegacyEntity;
import uk.gov.hmcts.opal.repository.CourtLiteRepository;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.OffenceRepository;
import uk.gov.hmcts.opal.repository.ProsecutorLegacyRepository;
import uk.gov.hmcts.opal.repository.ResultRepository;
import uk.gov.hmcts.opal.util.JsonPathUtil.DocContext;

@Service
@RequiredArgsConstructor
public class LegacyDraftAccountReferenceValidationService extends AbstractDraftAccountReferenceValidationService {
    private final OpalDraftAccountReferenceValidationService opalService;
    private final CourtLiteRepository courtLiteRepository;
    private final OffenceRepository offenceRepository;
    private final ResultRepository resultRepository;
    private final CreditorAccountRepository creditorAccountRepository;
    private final ProsecutorLegacyRepository prosecutorLegacyRepository;

    // Created in PO-10909 - Subsequent tickets will move each method over to use legacy repositories incrementally

    @Override
    void validateEnforcementCourt(DocContext docContext, List<String> failures) {
        opalService.validateEnforcementCourt(docContext, failures);
    }

    @Override
    CourtLiteRepository courtLiteRepository() {
        return courtLiteRepository;
    }

    @Override
    OffenceRepository offenceRepository() {
        return offenceRepository;
    }

    @Override
    CreditorAccountRepository creditorAccountRepository() {
        return creditorAccountRepository;
    }

    @Override
    Optional<ResultView> lookupResult(String resultId) {
        return resultRepository.findById(resultId).map(result -> new ResultView(
            result.isActive(), result.isImposition(), result.isEnforcement(), result.getImpositionCreditor()));
    }

    @Override
    void validateOriginator(DocContext docContext, List<String> failures) {
        opalService.validateOriginator(docContext, failures, prosecutorKey ->
            Optional.ofNullable(opalService.toShort(prosecutorKey))
                .flatMap(prosecutorLegacyRepository::findById)
                .map(ProsecutorLegacyEntity::getName));
    }
}
