package uk.gov.hmcts.opal.service.reference.validation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.ProsecutorLegacyEntity;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.repository.ProsecutorLegacyRepository;
import uk.gov.hmcts.opal.util.JsonPathUtil.DocContext;

@Service
@RequiredArgsConstructor
public class LegacyDraftAccountReferenceValidationService extends AbstractDraftAccountReferenceValidationService {
    private final OpalDraftAccountReferenceValidationService opalService;
    private final ProsecutorLegacyRepository prosecutorLegacyRepository;

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
        Map<String, Optional<ResultEntity>> resultCache) {
        opalService.validatePaymentTermsEnforcements(docContext, failures, resultCache);
    }

    @Override
    Optional<String> findLocalJusticeAreaName(Short localJusticeAreaId) {
        return opalService.findLocalJusticeAreaName(localJusticeAreaId);
    }

    @Override
    Optional<String> findProsecutorName(Long prosecutorKey) {
        return prosecutorLegacyRepository.findById(prosecutorKey).map(ProsecutorLegacyEntity::getName);
    }
}
