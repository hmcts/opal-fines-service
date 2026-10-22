package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.AlternatePaymentReferenceEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.generated.model.AlternatePaymentReferencesItem;
import uk.gov.hmcts.opal.mapper.AlternatePaymentReferenceMapper;
import uk.gov.hmcts.opal.repository.AlternatePaymentReferenceRepository;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.util.PermissionUtil;

@ExtendWith(MockitoExtension.class)
@DisplayName("Alternate payment reference service")
class AlternatePaymentReferenceServiceTest {

    private static final Long DEFENDANT_ACCOUNT_ID = 123L;
    private static final Short BUSINESS_UNIT_ID = 50;

    @Mock
    private DefendantAccountRepositoryService defendantAccountRepositoryService;

    @Mock
    private AlternatePaymentReferenceRepository repository;

    @Mock
    private AlternatePaymentReferenceMapper alternatePaymentReferenceMapper;

    @Spy
    @InjectMocks
    private AlternatePaymentReferenceService service;

    @Nested
    @DisplayName("Verify the defendant account exists and the user has the required permission")
    class VerifyDefendantAccountEntityExistAndUserHasCorrectPermission {

        @Test
        @DisplayName("Returns the account when the user has the required permission in its business unit")
        void returnsAccountWhenUserHasRequiredPermissionInBusinessUnit() {
            DefendantAccountEntity account = createDefendantAccount();
            when(defendantAccountRepositoryService.findById(DEFENDANT_ACCOUNT_ID)).thenReturn(account);

            try (MockedStatic<PermissionUtil> permissionUtil = mockStatic(PermissionUtil.class)) {
                DefendantAccountEntity actual =
                    service.verifyDefendantAccountEntityExistAndUserHasCorrectPermission(DEFENDANT_ACCOUNT_ID);

                assertSame(account, actual);
                verify(defendantAccountRepositoryService).findById(DEFENDANT_ACCOUNT_ID);
                permissionUtil.verify(() -> PermissionUtil.checkPermissionInBusinessUnit(
                    FinesPermission.MANAGE_ALTERNATE_PAYMENT_REFERENCES, BUSINESS_UNIT_ID));
                verifyNoInteractions(repository, alternatePaymentReferenceMapper);
            }
        }

        @Test
        @DisplayName("Propagates access denied when the user lacks the required permission")
        void throwsAccessDeniedExceptionWhenUserLacksRequiredPermission() {
            when(defendantAccountRepositoryService.findById(DEFENDANT_ACCOUNT_ID)).thenReturn(createDefendantAccount());
            AccessDeniedException expected = new AccessDeniedException("User does not have the required permission");

            try (MockedStatic<PermissionUtil> permissionUtil = mockStatic(PermissionUtil.class)) {
                permissionUtil.when(() -> PermissionUtil.checkPermissionInBusinessUnit(
                    FinesPermission.MANAGE_ALTERNATE_PAYMENT_REFERENCES, BUSINESS_UNIT_ID)).thenThrow(expected);

                AccessDeniedException actual = assertThrows(
                    AccessDeniedException.class,
                    () -> service.verifyDefendantAccountEntityExistAndUserHasCorrectPermission(DEFENDANT_ACCOUNT_ID));

                assertSame(expected, actual);
                verify(defendantAccountRepositoryService).findById(DEFENDANT_ACCOUNT_ID);
                permissionUtil.verify(() -> PermissionUtil.checkPermissionInBusinessUnit(
                    FinesPermission.MANAGE_ALTERNATE_PAYMENT_REFERENCES, BUSINESS_UNIT_ID));
                verifyNoInteractions(repository, alternatePaymentReferenceMapper);
            }
        }
    }

    @Nested
    @DisplayName("Retrieve alternate payment references for a defendant account")
    class GetAlternativePaymentReferenceEntities {

        @Test
        @DisplayName("Returns the alternate payment references found for the defendant account")
        void returnsReferencesWhenRepositoryFindsMatchesForDefendantAccount() {
            List<AlternatePaymentReferenceEntity> expected = List.of(
                AlternatePaymentReferenceEntity.builder().alternatePaymentReferenceId(1L).build(),
                AlternatePaymentReferenceEntity.builder().alternatePaymentReferenceId(2L).build()
            );
            when(repository.findByDefendantAccount_defendantAccountId(DEFENDANT_ACCOUNT_ID)).thenReturn(expected);

            List<AlternatePaymentReferenceEntity> actual =
                service.getAlternativePaymentReferenceEntities(DEFENDANT_ACCOUNT_ID);

            assertEquals(expected, actual);
            verify(repository).findByDefendantAccount_defendantAccountId(DEFENDANT_ACCOUNT_ID);
            verifyNoInteractions(defendantAccountRepositoryService, alternatePaymentReferenceMapper);
        }

        @Test
        @DisplayName("Returns an empty list when the defendant account has no alternate payment references")
        void returnsEmptyListWhenRepositoryFindsNoReferencesForDefendantAccount() {
            when(repository.findByDefendantAccount_defendantAccountId(DEFENDANT_ACCOUNT_ID)).thenReturn(List.of());

            List<AlternatePaymentReferenceEntity> actual =
                service.getAlternativePaymentReferenceEntities(DEFENDANT_ACCOUNT_ID);

            assertEquals(List.of(), actual);
            verify(repository).findByDefendantAccount_defendantAccountId(DEFENDANT_ACCOUNT_ID);
            verifyNoInteractions(defendantAccountRepositoryService, alternatePaymentReferenceMapper);
        }
    }

    @Nested
    @DisplayName("Retrieve mapped alternate payment references for a defendant account")
    class GetAlternatePaymentReferences {

        @Test
        @DisplayName("Returns mapped payment references in repository order when the user has permission")
        void returnsMappedReferencesInRepositoryOrderWhenUserHasPermission() {
            doReturn(createDefendantAccount()).when(service)
                .verifyDefendantAccountEntityExistAndUserHasCorrectPermission(DEFENDANT_ACCOUNT_ID);

            AlternatePaymentReferenceEntity firstEntity = AlternatePaymentReferenceEntity.builder()
                .alternatePaymentReferenceId(2L).build();
            AlternatePaymentReferenceEntity secondEntity = AlternatePaymentReferenceEntity.builder()
                .alternatePaymentReferenceId(1L).build();

            AlternatePaymentReferencesItem firstItem = new AlternatePaymentReferencesItem()
                .alternatePaymentReferenceId(2L).aprText("REFERENCE-2");
            AlternatePaymentReferencesItem secondItem = new AlternatePaymentReferencesItem()
                .alternatePaymentReferenceId(1L).aprText("REFERENCE-1");

            doReturn(List.of(firstEntity, secondEntity)).when(service)
                .getAlternativePaymentReferenceEntities(DEFENDANT_ACCOUNT_ID);

            when(alternatePaymentReferenceMapper.toAlternatePaymentReferencesItem(firstEntity)).thenReturn(firstItem);
            when(alternatePaymentReferenceMapper.toAlternatePaymentReferencesItem(secondEntity)).thenReturn(secondItem);

            List<AlternatePaymentReferencesItem> actual = service.getAlternatePaymentReferences(DEFENDANT_ACCOUNT_ID);

            assertEquals(List.of(firstItem, secondItem), actual);
            verify(service).verifyDefendantAccountEntityExistAndUserHasCorrectPermission(DEFENDANT_ACCOUNT_ID);
            verify(service).getAlternativePaymentReferenceEntities(DEFENDANT_ACCOUNT_ID);
            verify(alternatePaymentReferenceMapper).toAlternatePaymentReferencesItem(firstEntity);
            verify(alternatePaymentReferenceMapper).toAlternatePaymentReferencesItem(secondEntity);
        }

        @Test
        @DisplayName("Returns an empty list without mapping when no alternate payment references exist")
        void returnsEmptyListWithoutMappingWhenNoReferencesExist() {
            doReturn(createDefendantAccount()).when(service)
                .verifyDefendantAccountEntityExistAndUserHasCorrectPermission(DEFENDANT_ACCOUNT_ID);
            doReturn(List.of()).when(service).getAlternativePaymentReferenceEntities(DEFENDANT_ACCOUNT_ID);

            List<AlternatePaymentReferencesItem> actual = service.getAlternatePaymentReferences(DEFENDANT_ACCOUNT_ID);

            assertEquals(List.of(), actual);
            verify(service).verifyDefendantAccountEntityExistAndUserHasCorrectPermission(DEFENDANT_ACCOUNT_ID);
            verify(service).getAlternativePaymentReferenceEntities(DEFENDANT_ACCOUNT_ID);
            verifyNoInteractions(alternatePaymentReferenceMapper);
        }

        @Test
        @DisplayName("Rejects an unauthorised user without retrieving or mapping payment references")
        void throwsAccessDeniedExceptionWithoutRetrievingOrMappingReferencesWhenUserLacksPermission() {
            AccessDeniedException expected = new AccessDeniedException("User does not have the required permission");
            doThrow(expected).when(service)
                .verifyDefendantAccountEntityExistAndUserHasCorrectPermission(DEFENDANT_ACCOUNT_ID);

            AccessDeniedException actual = assertThrows(
                AccessDeniedException.class,
                () -> service.getAlternatePaymentReferences(DEFENDANT_ACCOUNT_ID));

            assertSame(expected, actual);
            verify(service, never()).getAlternativePaymentReferenceEntities(DEFENDANT_ACCOUNT_ID);
            verifyNoInteractions(repository, alternatePaymentReferenceMapper);
        }
    }

    private static DefendantAccountEntity createDefendantAccount() {
        return DefendantAccountEntity.builder()
            .defendantAccountId(DEFENDANT_ACCOUNT_ID)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId(BUSINESS_UNIT_ID).build())
            .build();
    }
}
