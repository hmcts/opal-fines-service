package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationToken;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.util.SecurityUtil;
import uk.gov.hmcts.opal.entity.LowHighValue;
import uk.gov.hmcts.opal.entity.enforcement.AccountType;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementAccountType;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementAccountTypeEntity;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.generated.model.PatchEnforcementAccountTypeRequestInner;
import uk.gov.hmcts.opal.mapper.EnforcementAccountTypeMapper;
import uk.gov.hmcts.opal.repository.EnforcementAccountTypeRepository;
import uk.gov.hmcts.opal.service.opal.EnforcementAccountTypesService;

@ExtendWith(MockitoExtension.class)
public class EnforcementAccountTypesServiceTest {

    @Mock
    private EnforcementAccountTypeMapper mapper;

    @Mock
    private EnforcementAccountTypeRepository repository;

    @Mock
    private OpalJwtAuthenticationToken authToken;

    @InjectMocks
    private EnforcementAccountTypesService service;


    @Test
    public void updateEnforcementAccountTypes_shouldOrchestrateCallCorrectly() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            when(authToken.hasPermission(FinesPermission.AUTO_ENFORCEMENT)).thenReturn(true);
            securityUtil.when(SecurityUtil::getOpalJwtAuthenticationTokenForCurrentUser).thenReturn(authToken);
            List<PatchEnforcementAccountTypeRequestInner> request = List.of(
                mock(PatchEnforcementAccountTypeRequestInner.class)
            );
            EnforcementAccountTypeEntity mockEntity = mock(EnforcementAccountTypeEntity.class);
            when(mockEntity.getVersion()).thenReturn(BigInteger.ZERO);

            when(repository.findById(any())).thenReturn(Optional.of(mockEntity));

            service.updateEnforcementAccountType(request);

            verify(mapper).toDtos(any());
        }
    }

    @Test
    public void updateEnforcementAccountTypes_missingIdThrowsEntityNotFoundException() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            when(authToken.hasPermission(FinesPermission.AUTO_ENFORCEMENT)).thenReturn(true);
            securityUtil.when(SecurityUtil::getOpalJwtAuthenticationTokenForCurrentUser).thenReturn(authToken);
            List<PatchEnforcementAccountTypeRequestInner> request = List.of(
                mock(PatchEnforcementAccountTypeRequestInner.class)
            );

            when(repository.findById(any())).thenReturn(Optional.ofNullable(null));

            assertThrows(EntityNotFoundException.class, () -> service.updateEnforcementAccountType(request));
            verifyNoInteractions(mapper);
            verify(repository).findById(any());
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    public void updateEnforcementAccountTypes_unauthorisedUser_shouldThrowPermissionsException() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            when(authToken.hasPermission(FinesPermission.AUTO_ENFORCEMENT)).thenReturn(false);
            securityUtil.when(SecurityUtil::getOpalJwtAuthenticationTokenForCurrentUser).thenReturn(authToken);
            List<PatchEnforcementAccountTypeRequestInner> request = List.of(
                mock(PatchEnforcementAccountTypeRequestInner.class)
            );

            assertThrows(PermissionNotAllowedException.class, () -> service.updateEnforcementAccountType(request));
            verifyNoInteractions(repository);
            verifyNoInteractions(mapper);
        }
    }

    @Test
    public void updateEnforcementAccountTypes_invalidRequestThrowsUnprocessableException() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            when(authToken.hasPermission(FinesPermission.AUTO_ENFORCEMENT)).thenReturn(true);
            securityUtil.when(SecurityUtil::getOpalJwtAuthenticationTokenForCurrentUser).thenReturn(authToken);
            List<PatchEnforcementAccountTypeRequestInner> request = List.of(
                PatchEnforcementAccountTypeRequestInner.builder()
                    .id(1)
                    .minimumBalance(null)
                    .build()
            );

            when(repository.findById(any())).thenReturn(Optional.of(
                EnforcementAccountTypeEntity.builder()
                    .enforcementAccountTypeId(1L)
                    .enforcementAccountType(EnforcementAccountType.AH)
                    .accountType(AccountType.A)
                    .accountTypePath(LowHighValue.L)
                    .build()
            ));

            assertThrows(UnprocessableException.class, () -> service.updateEnforcementAccountType(request));
            verify(repository).findById(any());
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(mapper);
        }
    }

    @Test
    public void updateEnforcementAccountTypes_negativeMinBalanceThrowsUnprocessableException() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            when(authToken.hasPermission(FinesPermission.AUTO_ENFORCEMENT)).thenReturn(true);
            securityUtil.when(SecurityUtil::getOpalJwtAuthenticationTokenForCurrentUser).thenReturn(authToken);
            List<PatchEnforcementAccountTypeRequestInner> request = List.of(
                PatchEnforcementAccountTypeRequestInner.builder()
                    .id(1)
                    .minimumBalance(new BigDecimal("-100"))
                    .build()
            );

            when(repository.findById(any())).thenReturn(Optional.of(
                EnforcementAccountTypeEntity.builder()
                    .enforcementAccountTypeId(1L)
                    .enforcementAccountType(EnforcementAccountType.AH)
                    .accountType(AccountType.A)
                    .accountTypePath(LowHighValue.L)
                    .build()
            ));

            assertThrows(UnprocessableException.class, () -> service.updateEnforcementAccountType(request));
            verify(repository).findById(any());
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(mapper);
        }
    }

    @Test
    public void updateEnforcementAccountTypes_versionMisMatchThrowsException() {
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            when(authToken.hasPermission(FinesPermission.AUTO_ENFORCEMENT)).thenReturn(true);
            securityUtil.when(SecurityUtil::getOpalJwtAuthenticationTokenForCurrentUser).thenReturn(authToken);
            List<PatchEnforcementAccountTypeRequestInner> request = List.of(
                PatchEnforcementAccountTypeRequestInner.builder()
                    .id(1)
                    .minimumBalance(new BigDecimal("100"))
                    .version(5)
                    .build()
            );

            when(repository.findById(any())).thenReturn(Optional.of(
                EnforcementAccountTypeEntity.builder()
                    .enforcementAccountTypeId(1L)
                    .enforcementAccountType(EnforcementAccountType.AH)
                    .accountType(AccountType.A)
                    .accountTypePath(LowHighValue.L)
                    .versionNumber(2L)
                    .build()
            ));

            assertThrows(
                ObjectOptimisticLockingFailureException.class,
                () -> service.updateEnforcementAccountType(request)
            );
            verify(repository).findById(any());
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(mapper);
        }
    }
}