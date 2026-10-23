package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.common.exceptions.bespoke.BusinessRuleViolationException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.NoteType;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.AlternatePaymentReferenceEntity;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.Category;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.Relationship;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.generated.model.AlternatePaymentReferencesItem;
import uk.gov.hmcts.opal.generated.model.AlternatePaymentReferencesWriteRequest;
import uk.gov.hmcts.opal.mapper.AlternatePaymentReferenceMapper;
import uk.gov.hmcts.opal.repository.AlternatePaymentReferenceRepository;
import uk.gov.hmcts.opal.service.opal.OpalNotesService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.util.PermissionUtil;
import uk.gov.hmcts.opal.util.VersionUtils;

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

    @Mock
    private OpalNotesService notesService;

    @Mock
    private Clock clock;

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

    @Nested
    @DisplayName("Verify alternate payment reference text is unique within a business unit")
    class VerifyUniqueApr {

        @Test
        @DisplayName("Allows reference text that does not already exist in the business unit")
        void allowsReferenceTextWhenItIsUniqueWithinBusinessUnit() {
            BusinessUnitEntity businessUnit = BusinessUnitEntity.builder().businessUnitId(BUSINESS_UNIT_ID).build();
            String aprText = "REFERENCE-1";
            when(repository.existsByAprTextAndBusinessUnit_businessUnitId(aprText, BUSINESS_UNIT_ID)).thenReturn(false);

            assertDoesNotThrow(() -> service.verifyUniqueApr(businessUnit, aprText));

            verify(repository).existsByAprTextAndBusinessUnit_businessUnitId(aprText, BUSINESS_UNIT_ID);
            verifyNoInteractions(defendantAccountRepositoryService, alternatePaymentReferenceMapper);
        }

        @Test
        @DisplayName("Rejects duplicate reference text and identifies the reference and business unit")
        void throwsBusinessRuleViolationExceptionWhenReferenceTextAlreadyExistsInBusinessUnit() {
            BusinessUnitEntity businessUnit = BusinessUnitEntity.builder().businessUnitId(BUSINESS_UNIT_ID).build();
            String aprText = "REFERENCE-1";
            when(repository.existsByAprTextAndBusinessUnit_businessUnitId(aprText, BUSINESS_UNIT_ID)).thenReturn(true);

            BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.verifyUniqueApr(businessUnit, aprText));

            assertEquals("Alternate Payment Reference text already exists in business unit", exception.getTitle());
            assertEquals("Alternate Payment Reference text: REFERENCE-1 already exists in business unit: 50",
                exception.getDetail());
            verify(repository).existsByAprTextAndBusinessUnit_businessUnitId(aprText, BUSINESS_UNIT_ID);
            verifyNoInteractions(defendantAccountRepositoryService, alternatePaymentReferenceMapper);
        }
    }

    @Nested
    @DisplayName("Create an alternate payment reference for a defendant account")
    class CreateAlternatePaymentReferences {

        private static final String APR_TEXT = "REFERENCE-1";
        private static final String EXPECTED_VERSION = "\"3\"";

        @Test
        @DisplayName("Saves the timestamped reference, adds an activity note, and returns the mapped result")
        void savesReferenceAddsActivityNoteAndReturnsMappedSavedReference() {
            DefendantAccountEntity account = createDefendantAccount();
            account.setVersionNumber(3L);
            doReturn(account).when(service)
                .verifyDefendantAccountEntityExistAndUserHasCorrectPermission(DEFENDANT_ACCOUNT_ID);
            doNothing().when(service).verifyUniqueApr(account.getBusinessUnit(), APR_TEXT);
            when(clock.instant()).thenReturn(Instant.parse("2026-10-22T10:15:30Z"));
            when(clock.getZone()).thenReturn(ZoneOffset.UTC);
            AlternatePaymentReferenceEntity savedEntity = AlternatePaymentReferenceEntity.builder()
                .alternatePaymentReferenceId(456L).aprText(APR_TEXT).versionNumber(0L).build();
            when(repository.save(any(AlternatePaymentReferenceEntity.class))).thenReturn(savedEntity);
            AlternatePaymentReferencesItem expected = new AlternatePaymentReferencesItem()
                .alternatePaymentReferenceId(456L).aprText(APR_TEXT).version(1L);
            when(alternatePaymentReferenceMapper.toAlternatePaymentReferencesItem(savedEntity)).thenReturn(expected);

            try (MockedStatic<VersionUtils> versionUtils = mockStatic(VersionUtils.class)) {
                AlternatePaymentReferencesItem actual = service.createAlternatePaymentReferences(
                    DEFENDANT_ACCOUNT_ID, createRequest(), EXPECTED_VERSION);

                assertSame(expected, actual);
                versionUtils.verify(() -> VersionUtils.verifyIfMatch(
                    account, EXPECTED_VERSION, DEFENDANT_ACCOUNT_ID, "createAlternatePaymentReferences"));
                verify(service).verifyDefendantAccountEntityExistAndUserHasCorrectPermission(DEFENDANT_ACCOUNT_ID);
                verify(service).verifyUniqueApr(account.getBusinessUnit(), APR_TEXT);
                ArgumentCaptor<AlternatePaymentReferenceEntity> entityCaptor =
                    ArgumentCaptor.forClass(AlternatePaymentReferenceEntity.class);
                InOrder order = inOrder(repository, notesService, alternatePaymentReferenceMapper);
                order.verify(repository).save(entityCaptor.capture());
                order.verify(notesService).addNote(
                    NoteType.AA, AssociatedRecordType.DEFENDANT_ACCOUNTS, DEFENDANT_ACCOUNT_ID,
                    account.getVersion(), "Alternate payment reference added | " + APR_TEXT, BUSINESS_UNIT_ID);
                order.verify(alternatePaymentReferenceMapper).toAlternatePaymentReferencesItem(savedEntity);

                AlternatePaymentReferenceEntity createdEntity = entityCaptor.getValue();
                assertSame(account, createdEntity.getDefendantAccount());
                assertSame(account.getBusinessUnit(), createdEntity.getBusinessUnit());
                assertEquals(Relationship.APR, createdEntity.getRelationship());
                assertEquals(Category.APR, createdEntity.getCategory());
                assertEquals(APR_TEXT, createdEntity.getAprText());
                assertEquals(LocalDateTime.of(2026, 10, 22, 10, 15, 30), createdEntity.getCreatedDatetime());
                assertEquals(createdEntity.getCreatedDatetime(), createdEntity.getUpdatedDatetime());
            }
        }

        @Test
        @DisplayName("Rejects an unauthorised user without saving a reference or adding a note")
        void throwsAccessDeniedExceptionWithoutSavingOrAddingNoteWhenUserLacksPermission() {
            AccessDeniedException expected = new AccessDeniedException("User does not have the required permission");
            doThrow(expected).when(service)
                .verifyDefendantAccountEntityExistAndUserHasCorrectPermission(DEFENDANT_ACCOUNT_ID);

            try (MockedStatic<VersionUtils> versionUtils = mockStatic(VersionUtils.class)) {
                AccessDeniedException actual = assertThrows(AccessDeniedException.class,
                    () -> service.createAlternatePaymentReferences(
                        DEFENDANT_ACCOUNT_ID, createRequest(), EXPECTED_VERSION));

                assertSame(expected, actual);
                versionUtils.verifyNoInteractions();
                verify(repository, never()).save(any(AlternatePaymentReferenceEntity.class));
                verifyNoInteractions(notesService, alternatePaymentReferenceMapper, clock);
            }
        }

        @Test
        @DisplayName("Rejects a version mismatch without checking uniqueness, saving a reference, or adding a note")
        void stopsBeforeUniquenessCheckAndSavingWhenVersionDoesNotMatch() {
            DefendantAccountEntity account = createDefendantAccount();
            doReturn(account).when(service)
                .verifyDefendantAccountEntityExistAndUserHasCorrectPermission(DEFENDANT_ACCOUNT_ID);
            ObjectOptimisticLockingFailureException expected = new ObjectOptimisticLockingFailureException(
                DefendantAccountEntity.class, DEFENDANT_ACCOUNT_ID);

            try (MockedStatic<VersionUtils> versionUtils = mockStatic(VersionUtils.class)) {
                versionUtils.when(() -> VersionUtils.verifyIfMatch(
                        account, EXPECTED_VERSION, DEFENDANT_ACCOUNT_ID, "createAlternatePaymentReferences"))
                    .thenThrow(expected);

                ObjectOptimisticLockingFailureException actual = assertThrows(
                    ObjectOptimisticLockingFailureException.class,
                    () -> service.createAlternatePaymentReferences(
                        DEFENDANT_ACCOUNT_ID, createRequest(), EXPECTED_VERSION));

                assertSame(expected, actual);
                verify(service, never()).verifyUniqueApr(any(BusinessUnitEntity.class), any(String.class));
                verify(repository, never()).save(any(AlternatePaymentReferenceEntity.class));
                verifyNoInteractions(notesService, alternatePaymentReferenceMapper, clock);
            }
        }

        @Test
        @DisplayName("Rejects duplicate reference text without saving a reference or adding a note")
        void throwsBusinessRuleViolationExceptionWithoutSavingOrAddingNoteWhenReferenceIsNotUnique() {
            DefendantAccountEntity account = createDefendantAccount();
            doReturn(account).when(service)
                .verifyDefendantAccountEntityExistAndUserHasCorrectPermission(DEFENDANT_ACCOUNT_ID);
            BusinessRuleViolationException expected = new BusinessRuleViolationException(
                "Duplicate reference", "The reference already exists in this business unit");
            doThrow(expected).when(service).verifyUniqueApr(account.getBusinessUnit(), APR_TEXT);

            try (MockedStatic<VersionUtils> versionUtils = mockStatic(VersionUtils.class)) {
                BusinessRuleViolationException actual = assertThrows(BusinessRuleViolationException.class,
                    () -> service.createAlternatePaymentReferences(
                        DEFENDANT_ACCOUNT_ID, createRequest(), EXPECTED_VERSION));

                assertSame(expected, actual);
                versionUtils.verify(() -> VersionUtils.verifyIfMatch(
                    account, EXPECTED_VERSION, DEFENDANT_ACCOUNT_ID, "createAlternatePaymentReferences"));
                verify(repository, never()).save(any(AlternatePaymentReferenceEntity.class));
                verifyNoInteractions(notesService, alternatePaymentReferenceMapper, clock);
            }
        }

        private AlternatePaymentReferencesWriteRequest createRequest() {
            return new AlternatePaymentReferencesWriteRequest().aprText(APR_TEXT);
        }
    }

    private static DefendantAccountEntity createDefendantAccount() {
        return DefendantAccountEntity.builder()
            .defendantAccountId(DEFENDANT_ACCOUNT_ID)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId(BUSINESS_UNIT_ID).build())
            .build();
    }
}
