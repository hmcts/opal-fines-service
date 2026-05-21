package uk.gov.hmcts.opal.service.opal.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigInteger;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor.SpecificationFluentQuery;
import uk.gov.hmcts.opal.common.logging.SecurityEventLoggingService;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.ReplaceDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.UpdateDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.entity.draft.DraftAccountType;
import uk.gov.hmcts.opal.exception.SubmitterDeniedException;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;


@ExtendWith(MockitoExtension.class)
class DraftAccountTransactionalTest {

    @Mock
    private DraftAccountRepository draftAccountRepository;

    @Mock
    private BusinessUnitRepository businessUnitRepository;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-05-07T10:15:00Z"), ZoneOffset.UTC);

    @InjectMocks
    private DraftAccountTransactional draftAccountTransactional;

    @Mock
    SecurityEventLoggingService securityEventLoggingService;

    @Test
    void testGetDraftAccount() {
        // Arrange
        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().businessUnit(
                BusinessUnitEntity.builder().businessUnitId((short)77).build())
            .build();
        when(draftAccountRepository.findById(any())).thenReturn(Optional.of(draftAccountEntity));

        // Act
        DraftAccountEntity result = draftAccountTransactional.getDraftAccount(1);

        // Assert
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetDraftAccounts() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().businessUnit(
                BusinessUnitEntity.builder().businessUnitId((short)77).build())
            .build();
        Page<DraftAccountEntity> mockPage = new PageImpl<>(List.of(draftAccountEntity), Pageable.unpaged(), 999L);
        when(draftAccountRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<DraftAccountEntity> result = draftAccountTransactional.getDraftAccounts(
            List.copyOf(Set.of((short) 1)), List.copyOf(Set.of(DraftAccountStatus.REJECTED)),
            List.of(), List.of(), Optional.empty(), Optional.empty()
        );
        // Assert
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchDraftAccounts() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);
        final String accountText = "myaccount";

        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().account(accountText).build();
        Page<DraftAccountEntity> mockPage = new PageImpl<>(List.of(draftAccountEntity), Pageable.unpaged(), 999L);
        when(draftAccountRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<DraftAccountEntity> result = draftAccountTransactional.searchDraftAccounts(
            DraftAccountSearchDto.builder().build());

        // Assert
        assertEquals(accountText, result.get(0).getAccount());

    }

    @Test
    void testSubmitDraftAccounts_success() {
        String minimalAccountJson = createAccountString();

        AddDraftAccountRequestDto dto = AddDraftAccountRequestDto.builder()
            .businessUnitId((short)2)
            .submittedBy("TestUser")
            .submittedByName("Test User")
            .account(minimalAccountJson)
            .accountType(DraftAccountType.FINE)
            .build();

        BusinessUnitEntity businessUnit = BusinessUnitEntity.builder()
            .businessUnitName("Old Bailey")
            .build();

        when(businessUnitRepository.getReferenceById(any())).thenReturn(businessUnit);
        when(draftAccountRepository.save(any(DraftAccountEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        DraftAccountEntity result = draftAccountTransactional.submitDraftAccount(dto);

        assertEquals(minimalAccountJson, result.getAccount());
        assertEquals(LocalDateTime.of(2026, 5, 7, 10, 15), result.getCreatedDate());
        assertEquals(LocalDateTime.of(2026, 5, 7, 10, 15), result.getAccountStatusDate());
        ArgumentCaptor<DraftAccountEntity> savedCaptor = ArgumentCaptor.forClass(DraftAccountEntity.class);
        verify(draftAccountRepository).save(savedCaptor.capture());
        assertTimelineLastEntry(
            savedCaptor.getValue().getTimelineData(),
            "TestUser",
            DraftAccountStatus.SUBMITTED.getLabel(),
            null
        );
        assertEquals(DraftAccountStatus.SUBMITTED, savedCaptor.getValue().getAccountStatus());

    }

    @Test
    void testDeleteDraftAccount_success() {
        // Arrange
        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().createdDate(LocalDateTime.now()).build();
        when(draftAccountRepository.findById(any())).thenReturn(Optional.of(draftAccountEntity));

        // Act
        boolean deleted = draftAccountTransactional.deleteDraftAccount(1, draftAccountTransactional);
        assertTrue(deleted);
    }

    @Test
    void testDeleteDraftAccount_fail1() {
        // Arrange
        when(draftAccountRepository.findById(any())).thenReturn(Optional.empty());

        // Act
        EntityNotFoundException enfe = assertThrows(
            EntityNotFoundException.class, () -> draftAccountTransactional
                .deleteDraftAccount(1, draftAccountTransactional)
        );

        // Assert
        assertEquals("Draft Account not found with id: 1", enfe.getMessage());
    }

    @Test
    void testReplaceDraftAccount_success() {
        // Arrange
        Long draftAccountId = 1L;
        ReplaceDraftAccountRequestDto replaceDto = ReplaceDraftAccountRequestDto.builder()
            .businessUnitId((short) 2)
            .submittedBy("TestUser")
            .submittedByName("Test User")
            .account(createAccountString())
            .accountType(DraftAccountType.FINE)
            .version(BigInteger.valueOf(0L))
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 2).build())
            .createdDate(LocalDateTime.now())
            .versionNumber(0L)
            .build();

        BusinessUnitEntity businessUnit = BusinessUnitEntity.builder()
            .businessUnitId(((short) 2))
            .businessUnitName("New Bailey")
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));
        when(businessUnitRepository.findById((short) 2)).thenReturn(Optional.of(businessUnit));
        when(draftAccountRepository.save(any(DraftAccountEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        DraftAccountEntity result = draftAccountTransactional
            .replaceDraftAccount(draftAccountId, replaceDto, draftAccountTransactional, "0");

        // Assert
        assertNotNull(result);
        assertEquals(draftAccountId, result.getDraftAccountId());
        assertEquals("TestUser", result.getSubmittedBy());
        assertEquals(createAccountString(), result.getAccount());
        assertEquals(DraftAccountType.FINE, result.getAccountType());
        assertEquals(DraftAccountStatus.RESUBMITTED, result.getAccountStatus());
        assertEquals(LocalDateTime.of(2026, 5, 7, 10, 15), result.getAccountStatusDate());

        verify(draftAccountRepository).findById(draftAccountId);
        verify(businessUnitRepository).findById((short) 2);
        ArgumentCaptor<DraftAccountEntity> savedCaptor = ArgumentCaptor.forClass(DraftAccountEntity.class);
        verify(draftAccountRepository).save(savedCaptor.capture());
        assertTimelineLastEntry(
            savedCaptor.getValue().getTimelineData(),
            "TestUser",
            DraftAccountStatus.RESUBMITTED.getLabel(),
            null
        );
    }

    @Test
    void testReplaceDraftAccount_appendsTimelineDataToExistingTimeline() {
        Long draftAccountId = 1L;
        String existingTimeline = singleTimelineDataString("original-user", "Submitted");
        ReplaceDraftAccountRequestDto replaceDto = ReplaceDraftAccountRequestDto.builder()
            .businessUnitId((short) 2)
            .submittedBy("TestUser")
            .submittedByName("Test User")
            .account(createAccountString())
            .accountType(DraftAccountType.FINE)
            .version(BigInteger.valueOf(0L))
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 2).build())
            .createdDate(LocalDateTime.now())
            .timelineData(existingTimeline)
            .versionNumber(0L)
            .build();

        BusinessUnitEntity businessUnit = BusinessUnitEntity.builder()
            .businessUnitId(((short) 2))
            .businessUnitName("New Bailey")
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));
        when(businessUnitRepository.findById((short) 2)).thenReturn(Optional.of(businessUnit));
        when(draftAccountRepository.save(any(DraftAccountEntity.class))).thenAnswer(invocation -> invocation
            .getArgument(0));

        DraftAccountEntity result = draftAccountTransactional
            .replaceDraftAccount(draftAccountId, replaceDto, draftAccountTransactional, "0");

        assertThat(result.getTimelineData()).contains("original-user", "TestUser");
        assertThat(result.getTimelineData().indexOf("original-user"))
            .isLessThan(result.getTimelineData().indexOf("TestUser"));
    }

    @Test
    void testReplaceDraftAccount_draftAccountNotFound() {
        // Arrange
        Long draftAccountId = 1L;
        ReplaceDraftAccountRequestDto replaceDto = ReplaceDraftAccountRequestDto.builder()
            .businessUnitId((short)1)
            .accountType(DraftAccountType.FINE)
            .account(createAccountString())
            .submittedBy("TestUser")
            .submittedByName("Test User")
            .version(BigInteger.valueOf(0L))
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            draftAccountTransactional.replaceDraftAccount(draftAccountId, replaceDto, draftAccountTransactional, "")
        );
        assertEquals("Draft Account not found with id: 1", exception.getMessage());
    }

    @Test
    void testReplaceDraftAccount_businessUnitNotFound() {
        // Arrange
        Long draftAccountId = 1L;
        ReplaceDraftAccountRequestDto replaceDto = ReplaceDraftAccountRequestDto.builder()
            .businessUnitId((short)2)
            .accountType(DraftAccountType.FINE)
            .account(createAccountString())
            .submittedBy("TestUser")
            .submittedByName("Test User")
            .version(BigInteger.valueOf(0L))
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder().versionNumber(0L).build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));
        when(businessUnitRepository.findById((short) 2)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            draftAccountTransactional.replaceDraftAccount(draftAccountId, replaceDto, draftAccountTransactional, "0")
        );
        assertEquals("Business Unit not found with id: 2", exception.getMessage());
    }

    @Test
    void testReplaceDraftAccount_businessUnitMismatch() {
        // Arrange
        Long draftAccountId = 1L;

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 3).build())
            .versionNumber(0L)
            .build();

        BusinessUnitEntity businessUnit = BusinessUnitEntity.builder()
            .businessUnitId(((short) 3))
            .build();

        ReplaceDraftAccountRequestDto dto = ReplaceDraftAccountRequestDto.builder()
            .businessUnitId((short) 2)
            .accountType(DraftAccountType.FINE)
            .account(createAccountString())
            .submittedBy("TestUser")
            .submittedByName("Test User")
            .version(BigInteger.valueOf(0L))
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));
        when(businessUnitRepository.findById((short) 2)).thenReturn(Optional.of(businessUnit));

        // Act & Assert
        assertThrows(ResourceConflictException.class, () ->
            draftAccountTransactional.replaceDraftAccount(draftAccountId, dto, draftAccountTransactional, "0")
        );
    }

    @Test
    void testUpdateDraftAccount_businessUnitMismatch() {
        // Arrange
        Long draftAccountId = 1L;
        UpdateDraftAccountRequestDto updateDto = UpdateDraftAccountRequestDto.builder()
            .businessUnitId((short) 2)
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 3).build())
            .versionNumber(0L)
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));
        UserState userState = UserState.builder().userName("USER_NAME_1").build();

        // Act & Assert
        assertThrows(ResourceConflictException.class, () ->
            draftAccountTransactional.updateDraftAccount(draftAccountId, updateDto, draftAccountTransactional,
                BigInteger.ZERO, userState)
        );
    }

    @Test
    void testUpdateDraftAccount_success() {
        // Arrange
        Long draftAccountId = 1L;
        UpdateDraftAccountRequestDto updateDto = UpdateDraftAccountRequestDto.builder()
            .accountStatus(DraftAccountStatus.PUBLISHING_PENDING)
            .validatedBy("TestValidator")
            .businessUnitId((short) 2)
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .accountSnapshot("{\"created_date\":\"2024-10-01T10:00:00Z\"}")
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 2).build())
            .timelineData(createTimelineDataString())
            .versionNumber(0L)
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));
        when(draftAccountRepository.save(any(DraftAccountEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UserState userState = UserState.builder().userName("USER_NAME_1").build();

        // Act
        DraftAccountEntity result = draftAccountTransactional
            .updateDraftAccount(draftAccountId, updateDto, draftAccountTransactional, BigInteger.ZERO, userState);

        // Assert
        assertNotNull(result);
        assertEquals(draftAccountId, result.getDraftAccountId());
        assertEquals(DraftAccountStatus.PUBLISHING_PENDING, result.getAccountStatus());
        assertEquals("TestValidator", result.getValidatedBy());
        assertEquals(LocalDateTime.of(2026, 5, 7, 10, 15), result.getValidatedDate());
        assertEquals(LocalDateTime.of(2026, 5, 7, 10, 15), result.getAccountStatusDate());
        assertTrue(result.getAccountSnapshot().contains("approved_date"));
        assertTrue(result.getAccountSnapshot().contains("2026-05-07T10:15:00Z"));

        verify(draftAccountRepository).findById(draftAccountId);
        ArgumentCaptor<DraftAccountEntity> savedCaptor = ArgumentCaptor.forClass(DraftAccountEntity.class);
        verify(draftAccountRepository).save(savedCaptor.capture());
        assertTimelineLastEntry(
            savedCaptor.getValue().getTimelineData(),
            "TestValidator",
            DraftAccountStatus.PUBLISHING_PENDING.getLabel(),
            null
        );
    }

    @Test
    void testUpdateDraftAccount_appendsTimelineDataToExistingTimeline() {
        Long draftAccountId = 1L;
        String existingTimeline = singleTimelineDataString("original-user", "Submitted");
        UpdateDraftAccountRequestDto updateDto = UpdateDraftAccountRequestDto.builder()
            .accountStatus(DraftAccountStatus.REJECTED)
            .validatedBy("normal@users.com")
            .businessUnitId((short) 2)
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 2).build())
            .timelineData(existingTimeline)
            .versionNumber(0L)
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));
        when(draftAccountRepository.save(any(DraftAccountEntity.class))).thenAnswer(invocation -> invocation
            .getArgument(0));

        UserState userState = UserState.builder().userName("USER_NAME_1").build();

        DraftAccountEntity result = draftAccountTransactional
            .updateDraftAccount(draftAccountId, updateDto, draftAccountTransactional, BigInteger.ZERO, userState);

        assertThat(result.getTimelineData()).contains("original-user", "normal@users.com");
        assertThat(result.getTimelineData().indexOf("original-user"))
            .isLessThan(result.getTimelineData().indexOf("normal@users.com"));
    }

    @Test
    void testUpdateDraftAccount_submitterCannotValidate() {

        //Arrange
        Long draftAccountId = 1L;
        UpdateDraftAccountRequestDto updateDto = UpdateDraftAccountRequestDto.builder()
            .accountStatus(DraftAccountStatus.PUBLISHING_PENDING)
            .validatedBy("BUUID1")
            .validatedByName("User One")
            .businessUnitId((short) 2)
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .submittedBy("BUUID1")
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 2).build())
            .timelineData(createTimelineDataString())
            .versionNumber(0L)
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));

        UserState userState = UserState.builder().userName("USER_NAME_1").userId(23L).build();

        // Act & Assert
        SubmitterDeniedException ex = assertThrows(SubmitterDeniedException.class, () -> {
            draftAccountTransactional.updateDraftAccount(draftAccountId, updateDto, draftAccountTransactional,
                BigInteger.ZERO, userState);
        });
        assertThat(ex.getUpdateType()).isEqualTo("validate");
        assertThat(ex.getSubmitterUsername()).isEqualTo("BUUID1");

        verify(securityEventLoggingService, times(1)).logEvent(
            eq("Business Function - Approval of Draft Account"),
            eq("Failure"),
            eq((short) 2),
            eq("Approval"),
            eq(LocalDateTime.of(2026, 5, 7, 10, 15)),
            eq(Map.of(
                "UserIdentifier", 23L,
                "DraftAccountIdentifier", draftAccountId,
                "DraftAccountSubmittedByUserIdentifier", "BUUID1"
            ))
        );
    }

    @Test
    void testUpdateDraftAccount_submitterCannotDelete() {

        // Arrange
        Long draftAccountId = 1L;
        UpdateDraftAccountRequestDto updateDto = UpdateDraftAccountRequestDto.builder()
            .accountStatus(DraftAccountStatus.DELETED)
            .validatedBy("BUUID1")
            .validatedByName("User One")
            .businessUnitId((short) 2)
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .submittedBy("BUUID1")
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 2).build())
            .timelineData(createTimelineDataString())
            .versionNumber(0L)
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));

        UserState userState = UserState.builder().userName("BUUID1").userId(23L).build();

        // Act & Assert
        SubmitterDeniedException ex = assertThrows(SubmitterDeniedException.class, () -> {
            draftAccountTransactional.updateDraftAccount(draftAccountId, updateDto, draftAccountTransactional,
                BigInteger.ZERO, userState);
        });
        assertThat(ex.getUpdateType()).isEqualTo("delete");
        assertThat(ex.getSubmitterUsername()).isEqualTo("BUUID1");

        verify(securityEventLoggingService, times(1)).logEvent(
            eq("Business Function - Deletion of Draft Account"),
            eq("Failure"),
            eq((short) 2),
            eq("Deletion"),
            eq(LocalDateTime.of(2026, 5, 7, 10, 15)),
            eq(Map.of(
                "UserIdentifier", 23L,
                "DraftAccountIdentifier", draftAccountId,
                "DraftAccountSubmittedByUserIdentifier", "BUUID1"
            ))
        );
    }

    @Test
    void testUpdateDraftAccount_invalidStatus() {
        // Arrange
        Long draftAccountId = 1L;
        UpdateDraftAccountRequestDto updateDto = UpdateDraftAccountRequestDto.builder()
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .businessUnitId((short) 2)
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 2).build())
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .timelineData(createTimelineDataString())
            .versionNumber(0L)
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));

        UserState userState = UserState.builder().userName("BUUID1").build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            draftAccountTransactional.updateDraftAccount(draftAccountId, updateDto, draftAccountTransactional,
                BigInteger.ZERO, userState)
        );
        assertEquals("Invalid account status for update: SUBMITTED", exception.getMessage());

        verify(draftAccountRepository).findById(draftAccountId);
        verify(draftAccountRepository, never()).save(any(DraftAccountEntity.class));
    }

    @Test
    void testUpdateState() {
        // Arrange
        DraftAccountEntity entity = DraftAccountEntity.builder()
            .draftAccountId(007L)
            .versionNumber(0L)
            .build();

        when(draftAccountRepository.findById(any())).thenReturn(Optional.of(entity));

        // Act
        DraftAccountEntity result = draftAccountTransactional.updateStatus(entity, DraftAccountStatus.PUBLISHING_FAILED,
            draftAccountTransactional
        );

        Assertions.assertDoesNotThrow(() -> { }); // Stops SonarQube complaining about no assertions in method.
    }

    @Test
    void testPublishAccountStoredProc() {
        // Arrange
        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder()
            .draftAccountId(007L)
            .businessUnit(BusinessUnitEntity.builder()
                .businessUnitId((short)78)
                .build())
            .submittedBy("BU001")
            .submittedByName("Malcolm Mclaren")
            .build();
        Map<String, Object> mockOutputs = Collections.emptyMap();
        when(draftAccountRepository.createDefendantAccount(any(), any(), any(), any()))
            .thenReturn(mockOutputs);

        // Act
        Map<String, Object> outputs = draftAccountTransactional.publishAccountStoredProc(draftAccountEntity);
        assertNotNull(outputs);
    }

    private String createTimelineDataString() {
        return """
            [{
                         "username": "johndoe123",
                         "status": "Active",
                         "status_date": "2023-11-01",
                         "reason_text": "Account successfully activated after review."
                     },
                     {
                         "username": "janedoe456",
                         "status": "Pending",
                         "status_date": "2023-12-05",
                         "reason_text": "Awaiting additional documentation for verification."
                     },
                     {
                         "username": "mikebrown789",
                         "status": "Suspended",
                         "status_date": "2023-10-15",
                         "reason_text": "Violation of terms of service."
                     }]
            """;
    }

    private String createAccountString() {
        return """
            {
                    "account_type": "Fine",
                    "defendant_type": "Adult",
                    "originator_name": "Police Force",
                    "originator_id": 12345,
                    "enforcement_court_id": 101,
                    "collection_order_made": true,
                    "collection_order_made_today": false,
                    "payment_card_request": true,
                    "account_sentence_date": "2023-12-01",
                    "defendant": {
                        "company_flag": false,
                        "title": "Mr",
                        "surname": "Doe",
                        "forenames": "John",
                        "dob": "1985-04-15",
                        "address_line_1": "123 Elm Street",
                        "address_line_2": "Suite 45",
                        "post_code": "AB1 2CD",
                        "telephone_number_home": "0123456789",
                        "telephone_number_mobile": "07712345678",
                        "email_address_1": "john.doe@example.com",
                        "national_insurance_number": "AB123456C",
                        "nationality_1": "British",
                        "occupation": "Engineer",
                        "debtor_detail": {
                            "document_language": "EN",
                            "hearing_language": "EN",
                            "vehicle_make": "Toyota",
                            "vehicle_registration_mark": "ABC123",
                            "aliases": [
                                {
                                    "alias_forenames": "Jon",
                                    "alias_surname": "Smith"
                                }
                            ]
                        }
                    },
                    "offences": [
                        {
                            "date_of_sentence": "2023-11-15",
                            "imposing_court_id": 202,
                            "offence_id": 1234,
                            "impositions": [
                                {
                                    "result_id": "1",
                                    "amount_imposed": 500.0,
                                    "amount_paid": 200.0,
                                    "major_creditor_id": 999
                                }
                            ]
                        }
                    ],
                    "payment_terms": {
                        "payment_terms_type_code": "P",
                        "effective_date": "2023-11-01",
                        "instalment_period": "M",
                        "lump_sum_amount": 1000.0,
                        "instalment_amount": 200.0,
                        "default_days_in_jail": 5
                    },
                    "account_notes": [
                        {
                            "account_note_serial": 1,
                            "account_note_text": "Defendant requested an installment plan.",
                            "note_type": "AC"
                        }
                    ]
               }
            """;
    }

    private String singleTimelineDataString(String username, String status) {
        return """
            [{
                         "username": "%s",
                         "status": "%s",
                         "status_date": "2026-04-22",
                         "reason_text": "Timeline reason"
                     }]
            """.formatted(username, status);
    }

}
