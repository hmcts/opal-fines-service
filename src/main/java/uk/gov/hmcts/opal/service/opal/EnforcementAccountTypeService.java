package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.core.TypedPropertyPath;
import org.springframework.data.domain.Sort;
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
import uk.gov.hmcts.opal.util.VersionUtils;

@Service
@RequiredArgsConstructor
public class EnforcementAccountTypeService {

    private final EnforcementAccountTypeRepository repository;
    private final EnforcementAccountTypeMapper mapper;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<EnforcementAccountTypeCommon> getAllEnforcementAccountTypes() {
        checkPermissions();
        List<EnforcementAccountTypeEntity> enforcementAccountTypes = repository.findAll(
            Sort.by(Sort.Direction.ASC, TypedPropertyPath.of(EnforcementAccountTypeEntity::getEnforcementAccountTypeId))
        );
        return mapper.toEnforcementAccountTypeCommonList(enforcementAccountTypes);
    }

    @Transactional
    public List<EnforcementAccountTypeCommon> updateEnforcementAccountType(
        List<PatchEnforcementAccountTypeRequestInner> request) {

        checkPermissions();

        List<EnforcementAccountTypeEntity> entities = new ArrayList<EnforcementAccountTypeEntity>();
        for (PatchEnforcementAccountTypeRequestInner requestObject : request) {

            EnforcementAccountTypeEntity entity = repository.findById(requestObject.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Enforcement account type not found"));

            if (entity.getAccountTypePath() == LowHighValue.LOW
                && requestObject.getMinimumBalance() == null) {
                throw new UnprocessableException("Can not update enforcement account type minimum balance for a low "
                    + "enforcement path");
            }

            if (requestObject.getMinimumBalance() != null
                && Optional.of(requestObject.getMinimumBalance()).orElse(new BigDecimal(0)).longValue() < 0L) {
                throw new UnprocessableException("Can not set minimum balance to a negative value");
            }

            VersionUtils.verifyVersions(entity, BigInteger.valueOf(requestObject.getVersion()),
                entity.getEnforcementAccountTypeId(), "updateEnforcementAccountType");

            entity.setMinimumBalance(requestObject.getMinimumBalance());

            entities.add(entity);
        }

        List<EnforcementAccountTypeEntity> updatedEntities = repository.findAllById(
            entities.stream().mapToLong(x -> x.getEnforcementAccountTypeId()).boxed().toList());

        return mapper.toEnforcementAccountTypeCommonList(updatedEntities);
    }

    private void checkPermissions() {
        if (!SecurityUtil.getOpalJwtAuthenticationTokenForCurrentUser()
            .hasPermission(FinesPermission.AUTO_ENFORCEMENT)) {
            throw new PermissionNotAllowedException(FinesPermission.AUTO_ENFORCEMENT);
        }
    }

}
