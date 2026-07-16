package uk.gov.hmcts.opal.service.opal;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.core.TypedPropertyPath;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.util.SecurityUtil;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementAccountTypeEntity;
import uk.gov.hmcts.opal.generated.model.EnforcementAccountTypeCommon;
import uk.gov.hmcts.opal.mapper.EnforcementAccountTypeMapper;
import uk.gov.hmcts.opal.repository.EnforcementAccountTypeRepository;

@Service
@RequiredArgsConstructor
public class EnforcementAccountTypeService {

    private final EnforcementAccountTypeRepository repository;
    private final EnforcementAccountTypeMapper mapper;

    @Transactional(readOnly = true)
    public List<EnforcementAccountTypeCommon> getAllEnforcementAccountTypes() {
        checkPermissions();
        List<EnforcementAccountTypeEntity> enforcementAccountTypes = repository.findAll(
            Sort.by(Sort.Direction.ASC, TypedPropertyPath.of(EnforcementAccountTypeEntity::getEnforcementAccountTypeId))
        );
        return mapper.toEnforcementAccountTypeCommonList(enforcementAccountTypes);
    }

    private void checkPermissions() {
        if (!SecurityUtil.getOpalJwtAuthenticationTokenForCurrentUser()
            .hasPermission(FinesPermission.AUTO_ENFORCEMENT)) {
            throw new PermissionNotAllowedException(FinesPermission.AUTO_ENFORCEMENT);
        }
    }

}
