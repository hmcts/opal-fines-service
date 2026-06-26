package uk.gov.hmcts.opal.service;

import jakarta.persistence.EntityNotFoundException;

import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.util.SecurityUtil;
import uk.gov.hmcts.opal.entity.LowHighValue;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementAccountTypeEntity;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.generated.model.PatchEnforcementAccountType200Response;
import uk.gov.hmcts.opal.generated.model.PatchEnforcementAccountTypeRequestInner;
import uk.gov.hmcts.opal.mapper.EnforcementAccountTypeMapper;
import uk.gov.hmcts.opal.repository.EnforcementAccountTypeRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j(topic = "opal.EnforcementAccountTypesService")
@RequiredArgsConstructor
public class EnforcementAccountTypesService {

    private final EnforcementAccountTypeRepository enforcementAccountTypeRepository;
    private final UserStateService userStateService;
    private final EnforcementAccountTypeMapper enforcementAccountTypeMapper;

    public PatchEnforcementAccountType200Response updateEnforcementAccountType(
        List<PatchEnforcementAccountTypeRequestInner> request) {

        checkPermissions();

        List<EnforcementAccountTypeEntity> entities = new ArrayList<EnforcementAccountTypeEntity>();
        for (PatchEnforcementAccountTypeRequestInner requestObject : request) {

            EnforcementAccountTypeEntity entity =
                enforcementAccountTypeRepository.findById(requestObject.getId().longValue())
                    .orElseThrow(() -> new EntityNotFoundException("Enforcement account type not found"));

            if (entity.getAccountTypePath() == LowHighValue.L
                && requestObject.getMinimumBalance() == null) {
                throw new UnprocessableException("Can not update enforcement account type minimum balance for a low "
                                                     + "enforcement path");
            }

            if (requestObject.getMinimumBalance() != null
                && Optional.of(requestObject.getMinimumBalance()).orElse(new BigDecimal(0)).longValue() < 0L) {
                throw new UnprocessableException("Can not set minimum balance to a negative value");
            }

            if (entity.getVersionNumber() != requestObject.getVersion().longValue()) {
                throw new ObjectOptimisticLockingFailureException("versionNumber", entity.getVersionNumber(),
                                                                  "Version numbers do not match", null);
            }

            entity.setMinimumBalance(requestObject.getMinimumBalance());
            entity.setVersionNumber(requestObject.getVersion().longValue() + 1);

            entities.add(entity);
        }
        List<EnforcementAccountTypeEntity> updatedEntities = enforcementAccountTypeRepository.saveAll(entities);

        var resp = new PatchEnforcementAccountType200Response();
        resp.setEnforcementAccountTypes(enforcementAccountTypeMapper.toDtos(updatedEntities));
        return resp;
    }

    private void checkPermissions() {
        if (!SecurityUtil.getOpalJwtAuthenticationTokenForCurrentUser()
            .hasPermission(FinesPermission.AUTO_ENFORCEMENT)) {
            throw new PermissionNotAllowedException(FinesPermission.AUTO_ENFORCEMENT);
        }
    }
}
