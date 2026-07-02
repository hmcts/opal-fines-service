package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.util.SecurityUtil;
import uk.gov.hmcts.opal.entity.LowHighValue;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementAccountTypeEntity;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.generated.model.EnforcementAccountTypeCommon;
import uk.gov.hmcts.opal.generated.model.PatchEnforcementAccountTypeRequestInner;
import uk.gov.hmcts.opal.mapper.EnforcementAccountTypeMapper;
import uk.gov.hmcts.opal.repository.EnforcementAccountTypeRepository;

import java.util.ArrayList;
import java.util.List;
import uk.gov.hmcts.opal.util.VersionUtils;

@Service
@Slf4j(topic = "opal.EnforcementAccountTypesService")
@RequiredArgsConstructor
public class EnforcementAccountTypesService {

    private final EnforcementAccountTypeRepository enforcementAccountTypeRepository;
    private final EnforcementAccountTypeMapper enforcementAccountTypeMapper;

    @Transactional
    public List<EnforcementAccountTypeCommon> updateEnforcementAccountType(
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

            VersionUtils.verifyVersions(entity, BigInteger.valueOf(requestObject.getVersion().longValue()),
                entity.getEnforcementAccountTypeId(), "updateEnforcementAccountType");

            entity.setMinimumBalance(requestObject.getMinimumBalance());
            entity.setVersionNumber(entity.getVersion().add(BigInteger.ONE).longValueExact());

            entities.add(entity);
        }

        return enforcementAccountTypeMapper.toDtos(entities);
    }

    private void checkPermissions() {
        if (!SecurityUtil.getOpalJwtAuthenticationTokenForCurrentUser()
            .hasPermission(FinesPermission.AUTO_ENFORCEMENT)) {
            throw new PermissionNotAllowedException(FinesPermission.AUTO_ENFORCEMENT);
        }
    }
}
