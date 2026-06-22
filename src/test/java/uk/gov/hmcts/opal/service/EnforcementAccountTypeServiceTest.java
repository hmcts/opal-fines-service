package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.core.TypedPropertyPath;
import org.springframework.data.domain.Sort;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
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
    private UserStateService userStateService;

    @Mock
    private UserState userState;

    @InjectMocks
    private EnforcementAccountTypeService service;

    @BeforeEach
    void beforeEach() {
        when(userStateService.checkForAuthorisedUser()).thenReturn(userState);
    }


    @Test
    public void getEnforcementAccountTypes_shouldOrchestrateCallCorrectly() {
        List<EnforcementAccountTypeEntity> eAccountTypes = List.of(
            mock(EnforcementAccountTypeEntity.class)
        );
        when(repository.findAll(
            Sort.by(Sort.Direction.ASC, TypedPropertyPath.of(EnforcementAccountTypeEntity::getEnforcementAccountTypeId)))
        ).thenReturn(eAccountTypes);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.AUTO_ENFORCEMENT)).thenReturn(true); //TODO see comment in service

        service.getAllEnforcementAccountTypes();

        verify(mapper).toDtos(eAccountTypes);
    }

    @Test
    public void getEnforcementAccountTypes_unauthorisedUser_shouldThrowPermissionsException() {
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.AUTO_ENFORCEMENT)).thenReturn(false); //TODO see comment in service

        assertThrows(PermissionNotAllowedException.class, () -> service.getAllEnforcementAccountTypes());
        verifyNoInteractions(repository);
        verifyNoInteractions(mapper);
    }
}
