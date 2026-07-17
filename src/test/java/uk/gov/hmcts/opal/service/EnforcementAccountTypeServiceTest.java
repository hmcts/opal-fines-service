package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.core.TypedPropertyPath;
import org.springframework.data.domain.Sort;
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

    private MockedStatic<SecurityUtil> secutityUtilMock;

    @BeforeEach
    public void setup() {
        secutityUtilMock = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    public void teardown() {
        secutityUtilMock.close();
    }

    private void withPermission() {
        when(authToken.hasPermission(FinesPermission.AUTO_ENFORCEMENT)).thenReturn(true);
        secutityUtilMock.when(SecurityUtil::getOpalJwtAuthenticationTokenForCurrentUser).thenReturn(authToken);
    }

    private void withoutPermission() {
        when(authToken.hasPermission(FinesPermission.AUTO_ENFORCEMENT)).thenReturn(false);
        secutityUtilMock.when(SecurityUtil::getOpalJwtAuthenticationTokenForCurrentUser).thenReturn(authToken);
    }

    private void withData(long id, LowHighValue accountTypePath, long version) {
        when(repository.findById(eq(id))).thenReturn(Optional.of(
            EnforcementAccountTypeEntity.builder()
                .enforcementAccountTypeId(id)
                .enforcementAccountType(EnforcementAccountType.ADULT_NO_COLLECTION_ORDER_HIGH)
                .accountType(AccountType.ADULT_NO_COLLECTION_ORDER)
                .accountTypePath(accountTypePath)
                .versionNumber(version)
                .build()
        ));
    }

    @Test
    public void getEnforcementAccountTypes_shouldOrchestrateCallCorrectly() {
        withPermission();

        List<EnforcementAccountTypeEntity> enfAccountTypes = List.of(
            mock(EnforcementAccountTypeEntity.class)
        );
        when(repository.findAll(Sort.by(
                    Sort.Direction.ASC, TypedPropertyPath.of(
                        EnforcementAccountTypeEntity::getEnforcementAccountTypeId)
                )
            )
        ).thenReturn(enfAccountTypes);

        service.getAllEnforcementAccountTypes();

        verify(mapper).toEnforcementAccountTypeCommonList(enfAccountTypes);

    }

    @Test
    public void getEnforcementAccountTypes_unauthorisedUser_shouldThrowPermissionsException() {
        withoutPermission();

        assertThrows(PermissionNotAllowedException.class, () -> service.getAllEnforcementAccountTypes());
        verifyNoInteractions(repository);
        verifyNoInteractions(mapper);
    }


    @Test
    public void updateEnforcementAccountTypes_shouldOrchestrateCallCorrectly() {
        withPermission();

        List<PatchEnforcementAccountTypeRequestInner> request = List.of(
            mock(PatchEnforcementAccountTypeRequestInner.class)
        );
        EnforcementAccountTypeEntity mockEntity = mock(EnforcementAccountTypeEntity.class);
        when(mockEntity.getVersion()).thenReturn(BigInteger.ZERO);

        when(repository.findById(eq(0L))).thenReturn(Optional.of(mockEntity));

        service.updateEnforcementAccountType(request);

        verify(mapper).toEnforcementAccountTypeCommonList(List.of(mockEntity));
    }

    @Test
    public void updateEnforcementAccountTypes_missingIdThrowsEntityNotFoundException() {
        withPermission();
        List<PatchEnforcementAccountTypeRequestInner> request = List.of(
            PatchEnforcementAccountTypeRequestInner.builder().id(1L).build()
        );

        when(repository.findById(eq(1L))).thenReturn(Optional.empty());

        EntityNotFoundException e = assertThrows(
            EntityNotFoundException.class,
            () -> service.updateEnforcementAccountType(request)
        );
        assertEquals("Enforcement account type not found", e.getMessage());

        verifyNoInteractions(mapper);
        verify(repository).findById(eq(1L));
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void updateEnforcementAccountTypes_unauthorisedUser_shouldThrowPermissionsException() {
        withoutPermission();

        List<PatchEnforcementAccountTypeRequestInner> request = List.of(
            mock(PatchEnforcementAccountTypeRequestInner.class)
        );

        PermissionNotAllowedException e = assertThrows(
            PermissionNotAllowedException.class,
            () -> service.updateEnforcementAccountType(request)
        );
        assertEquals("[AUTO_ENFORCEMENT] permission(s) are not enabled for the user.", e.getMessage());
        verifyNoInteractions(repository);
        verifyNoInteractions(mapper);
    }

    @Test
    public void updateEnforcementAccountTypes_invalidRequestThrowsUnprocessableException() {
        withPermission();

        List<PatchEnforcementAccountTypeRequestInner> request = List.of(
            PatchEnforcementAccountTypeRequestInner.builder()
                .id(1L)
                .minimumBalance(null)
                .build()
        );

        withData(1L, LowHighValue.LOW, 1L);

        UnprocessableException e = assertThrows(
            UnprocessableException.class,
            () -> service.updateEnforcementAccountType(request)
        );
        assertEquals(
            "Can not update enforcement account type minimum balance for a low enforcement path",
            e.getDetailedReason()
        );

        verify(repository).findById(eq(1L));
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(mapper);
    }

    @Test
    public void updateEnforcementAccountTypes_negativeMinBalanceThrowsUnprocessableException() {
        withPermission();
        List<PatchEnforcementAccountTypeRequestInner> request = List.of(
            PatchEnforcementAccountTypeRequestInner.builder()
                .id(1L)
                .minimumBalance(new BigDecimal("-100"))
                .build()
        );

        withData(1L, LowHighValue.LOW, 1L);

        UnprocessableException e = assertThrows(
            UnprocessableException.class,
            () -> service.updateEnforcementAccountType(request)
        );
        assertEquals("Can not set minimum balance to a negative value", e.getDetailedReason());

        verify(repository).findById(eq(1L));
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(mapper);
    }

    @Test
    public void updateEnforcementAccountTypes_versionMisMatchThrowsException() {
        withPermission();

        List<PatchEnforcementAccountTypeRequestInner> request = List.of(
            PatchEnforcementAccountTypeRequestInner.builder()
                .id(1L)
                .minimumBalance(new BigDecimal("100"))
                .version(5L)
                .build()
        );

        withData(1L, LowHighValue.LOW, 2L);

        ObjectOptimisticLockingFailureException e = assertThrows(
            ObjectOptimisticLockingFailureException.class,
            () -> service.updateEnforcementAccountType(request)
        );
        assertEquals(
            ":updateEnforcementAccountType: Versions do not match for: EnforcementAccountTypeEntity '1'; "
                + "DB version: 2, supplied update version: 5",
            e.getMessage()
        );

        verify(repository).findById(eq(1L));
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(mapper);
    }
}
