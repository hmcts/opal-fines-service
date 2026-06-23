package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.core.TypedPropertyPath;
import org.springframework.data.domain.Sort;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationToken;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.util.SecurityUtil;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementAccountTypeEntity;
import uk.gov.hmcts.opal.mapper.EnforcementAccountTypeMapper;
import uk.gov.hmcts.opal.repository.EnforcementAccountTypeRepository;
import uk.gov.hmcts.opal.service.opal.EnforcementAccountTypeService;

@ExtendWith(MockitoExtension.class)
public class EnforcementAccountTypeServiceTest {

    @Mock
    private EnforcementAccountTypeMapper mapper;

    @Mock
    private EnforcementAccountTypeRepository repository;

    @Mock
    private OpalJwtAuthenticationToken authToken;

    @InjectMocks
    private EnforcementAccountTypeService service;


    @Test
    public void getEnforcementAccountTypes_shouldOrchestrateCallCorrectly() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            when(authToken.hasPermission(FinesPermission.AUTO_ENFORCEMENT)).thenReturn(true);
            securityUtil.when(SecurityUtil::getOpalJwtAuthenticationTokenForCurrentUser).thenReturn(authToken);
            List<EnforcementAccountTypeEntity> enfAccountTypes = List.of(
                mock(EnforcementAccountTypeEntity.class)
            );
            when(repository.findAll(
                    Sort.by(
                        Sort.Direction.ASC, TypedPropertyPath.of(EnforcementAccountTypeEntity::getEnforcementAccountTypeId))
                )
            ).thenReturn(enfAccountTypes);

            service.getAllEnforcementAccountTypes();

            verify(mapper).toEnforcementAccountTypeCommonList(enfAccountTypes);
        }
    }

    @Test
    public void getEnforcementAccountTypes_unauthorisedUser_shouldThrowPermissionsException() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            when(authToken.hasPermission(FinesPermission.AUTO_ENFORCEMENT)).thenReturn(false);
            securityUtil.when(SecurityUtil::getOpalJwtAuthenticationTokenForCurrentUser).thenReturn(authToken);

            assertThrows(PermissionNotAllowedException.class, () -> service.getAllEnforcementAccountTypes());
            verifyNoInteractions(repository);
            verifyNoInteractions(mapper);
        }
    }
}
