package uk.gov.hmcts.opal.service.reference.validation;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaLegacyEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaTypeLegacy;
import uk.gov.hmcts.opal.repository.CourtLiteRepository;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.OffenceRepository;
import uk.gov.hmcts.opal.repository.ResultLegacyRepository;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaLegacyRepository;
import uk.gov.hmcts.opal.util.JsonPathUtil.DocContext;

@Service
@RequiredArgsConstructor
public class LegacyDraftAccountReferenceValidationService extends AbstractDraftAccountReferenceValidationService {
    private final OpalDraftAccountReferenceValidationService opalService;
    private final LocalJusticeAreaLegacyRepository localJusticeAreaLegacyRepository;
    private final CourtLiteRepository courtLiteRepository;
    private final OffenceRepository offenceRepository;
    private final CreditorAccountRepository creditorAccountRepository;
    private final ResultLegacyRepository resultRepository;

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
    Optional<ResultView> lookupResult(String id) {
        return resultRepository.findById(id).map(result -> new ResultView(
            result.isActive(), result.isImposition(), result.isEnforcement(), result.getImpositionCreditor()));
    }

    @Override
    Optional<String> findLocalJusticeAreaName(Short localJusticeAreaId) {
        return localJusticeAreaLegacyRepository.findById(localJusticeAreaId)
            .filter(entity -> entity.getLjaType() == LocalJusticeAreaTypeLegacy.LJA
                || entity.getLjaType() == LocalJusticeAreaTypeLegacy.CRWCRT)
            .map(LocalJusticeAreaLegacyEntity::getName);
    }

    @Override
    Optional<String> findProsecutorName(Long prosecutorId) {
        return opalService.findProsecutorName(prosecutorId);
    }
}
