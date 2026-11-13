package uk.gov.hmcts.opal.service.reference.validation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaLegacyEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaTypeLegacy;
import uk.gov.hmcts.opal.entity.result.ResultLegacyEntity;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaLegacyRepository;
import uk.gov.hmcts.opal.util.JsonPathUtil.DocContext;

@Service
@RequiredArgsConstructor
public class LegacyDraftAccountReferenceValidationService extends AbstractDraftAccountReferenceValidationService {
    private final OpalDraftAccountReferenceValidationService opalService;
    private final LocalJusticeAreaLegacyRepository localJusticeAreaLegacyRepository;

    // Created in PO-10909 - Subsequent tickets will move each method over to use legacy repositories incrementally

    @Override
    void validateEnforcementCourt(DocContext docContext, List<String> failures) {
        opalService.validateEnforcementCourt(docContext, failures);
    }

    @Override
    void validateOffences(Short businessUnitId, DocContext docContext, List<String> failures) {
        opalService.validateOffences(businessUnitId, docContext, failures);
    }

    @Override
    void validatePaymentTermsEnforcements(DocContext docContext, List<String> failures,
        Map<String, Optional<ResultLegacyEntity>> resultCache) {
        opalService.validatePaymentTermsEnforcements(docContext, failures, resultCache);
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
