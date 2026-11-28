package uk.gov.hmcts.opal.service.reference.validation;

import static uk.gov.hmcts.opal.util.JsonPathUtil.safeReadLong;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.OpalLocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.OpalLocalJusticeAreaType;
import uk.gov.hmcts.opal.entity.ProsecutorLegacyEntity;
import uk.gov.hmcts.opal.repository.CourtLiteRepository;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.OpalLocalJusticeAreaRepository;
import uk.gov.hmcts.opal.repository.OffenceRepository;
import uk.gov.hmcts.opal.repository.ProsecutorRepository;
import uk.gov.hmcts.opal.repository.ResultOpalRepository;
import uk.gov.hmcts.opal.util.JsonPathUtil;

@Service
@RequiredArgsConstructor
public class OpalDraftAccountReferenceValidationService extends AbstractDraftAccountReferenceValidationService {

    private static final String ROOT_PATH = "$";
    private static final String ENFORCEMENT_COURT_ID_PATH = ROOT_PATH + ".enforcement_court_id";
    private static final String ENFORCEMENT_COURT_ID_ERROR_PATH = "account.enforcement_court_id";
    private static final String DOES_NOT_EXIST = " does not exist";
    private static final Set<OpalLocalJusticeAreaType> VALID_ORIGINATOR_LJA_TYPES =
        Set.of(OpalLocalJusticeAreaType.LJA, OpalLocalJusticeAreaType.CRWCRT);

    private final CourtLiteRepository courtLiteRepository;
    private final OffenceRepository offenceRepository;
    private final ResultOpalRepository resultRepository;
    private final CreditorAccountRepository creditorAccountRepository;
    private final OpalLocalJusticeAreaRepository opalLocalJusticeAreaRepository;
    private final ProsecutorRepository prosecutorRepository;


    @Override
    void validateEnforcementCourt(JsonPathUtil.DocContext docContext, List<String> failures) {
        Long enforcementCourtId = safeReadLong(docContext, ENFORCEMENT_COURT_ID_PATH);
        if (enforcementCourtId == null) {
            return;
        }

        if (!courtLiteRepository.existsById(enforcementCourtId)) {
            failures.add(ENFORCEMENT_COURT_ID_ERROR_PATH + ": court id " + enforcementCourtId + DOES_NOT_EXIST);
        }
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
        return opalLocalJusticeAreaRepository.findById(localJusticeAreaId)
            .filter(entity -> VALID_ORIGINATOR_LJA_TYPES.contains(entity.getLjaType()))
            .map(OpalLocalJusticeAreaEntity::getName);
    }

    @Override
    Optional<String> findProsecutorName(Long prosecutorId) {
        return prosecutorRepository.findById(prosecutorId).map(ProsecutorLegacyEntity::getName);
    }
}
