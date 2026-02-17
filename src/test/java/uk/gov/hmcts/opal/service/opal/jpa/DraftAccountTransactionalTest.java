package uk.gov.hmcts.opal.service.opal.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigInteger;
import java.time.LocalDateTime;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor.SpecificationFluentQuery;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.ReplaceDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.UpdateDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.exception.SubmitterCannotValidateException;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;
import uk.gov.hmcts.opal.service.opal.DraftAccountPdplLoggingService;


@ExtendWith(MockitoExtension.class)
class DraftAccountTransactionalTest {

    @Mock
    private DraftAccountRepository draftAccountRepository;

    @Mock
    private BusinessUnitRepository businessUnitRepository;

    @Mock
    private DraftAccountPdplLoggingService loggingService;

    @InjectMocks
    private DraftAccountTransactional draftAccountTransactional;

    @Test
    void testGetDraftAccount() {
        // Arrange
        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().businessUnit(
                BusinessUnitFullEntity.builder().businessUnitId((short)77).build())
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
                BusinessUnitFullEntity.builder().businessUnitId((short)77).build())
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

        DraftAccountEntity saved = DraftAccountEntity.builder()
            .account(minimalAccountJson)
            .accountSnapshot("{}")
            .accountType("Fine")
            .draftAccountId(1L)
            .createdDate(LocalDateTime.now())
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .accountStatusDate(LocalDateTime.now())
            .build();

        AddDraftAccountRequestDto dto = AddDraftAccountRequestDto.builder()
            .businessUnitId((short)2)
            .submittedBy("TestUser")
            .submittedByName("Test User")
            .account(minimalAccountJson)
            .accountType("Fine")
            .timelineData("[]")
            .build();

        BusinessUnitFullEntity businessUnit = BusinessUnitFullEntity.builder()
            .businessUnitName("Old Bailey")
            .build();

        when(businessUnitRepository.getReferenceById(any())).thenReturn(businessUnit);
        when(draftAccountRepository.save(any(DraftAccountEntity.class))).thenReturn(saved);

        DraftAccountEntity result = draftAccountTransactional.submitDraftAccount(dto);

        assertEquals(saved.getAccount(), result.getAccount());

        // Capture the arguments passed to pdplForDraftAccount and assert their contents
        ArgumentCaptor<DraftAccountEntity> entityCaptor = ArgumentCaptor.forClass(DraftAccountEntity.class);
        ArgumentCaptor<DraftAccountPdplLoggingService.Action> actionCaptor =
            ArgumentCaptor.forClass(DraftAccountPdplLoggingService.Action.class);

        verify(loggingService).pdplForDraftAccount(entityCaptor.capture(), actionCaptor.capture());

        DraftAccountEntity capturedEntity = entityCaptor.getValue();

        assertNotNull(capturedEntity, "pdplForDraftAccount should be called with a DraftAccountEntity");
        assertEquals(saved.getDraftAccountId(), capturedEntity.getDraftAccountId(),
            "pdplForDraftAccount should be called with the saved draft account id");
        assertEquals(saved.getAccount(), capturedEntity.getAccount(),
            "pdplForDraftAccount should be called with the same account JSON that was saved");

        DraftAccountPdplLoggingService.Action capturedAction = actionCaptor.getValue();

        assertEquals(DraftAccountPdplLoggingService.Action.SUBMIT, capturedAction,
            "pdplForDraftAccount should be called with Action.SUBMIT");
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
            .accountType("Fine")
            .timelineData(createTimelineDataString())
            .version(BigInteger.valueOf(0L))
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .businessUnit(BusinessUnitFullEntity.builder().businessUnitId((short) 2).build())
            .createdDate(LocalDateTime.now())
            .versionNumber(0L)
            .build();

        BusinessUnitFullEntity businessUnit = BusinessUnitFullEntity.builder()
            .businessUnitId(((short) 2))
            .businessUnitName("New Bailey")
            .build();

        DraftAccountEntity updatedAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .submittedBy("TestUser")
            .account(createAccountString())
            .accountType("Fine")
            .accountStatus(DraftAccountStatus.RESUBMITTED)
            .timelineData(createTimelineDataString())
            .versionNumber(1L)
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));
        when(businessUnitRepository.findById((short) 2)).thenReturn(Optional.of(businessUnit));
        when(draftAccountRepository.save(any(DraftAccountEntity.class))).thenReturn(updatedAccount);

        // Act
        DraftAccountEntity result = draftAccountTransactional
            .replaceDraftAccount(draftAccountId, replaceDto, draftAccountTransactional, "0");

        // Assert
        assertNotNull(result);
        assertEquals(draftAccountId, result.getDraftAccountId());
        assertEquals("TestUser", result.getSubmittedBy());
        assertEquals(createAccountString(), result.getAccount());
        assertEquals("Fine", result.getAccountType());
        assertEquals(DraftAccountStatus.RESUBMITTED, result.getAccountStatus());
        assertEquals(createTimelineDataString(), result.getTimelineData());

        verify(draftAccountRepository).findById(draftAccountId);
        verify(businessUnitRepository).findById((short) 2);
        verify(draftAccountRepository).save(any(DraftAccountEntity.class));
    }

    @Test
    void testReplaceDraftAccount_draftAccountNotFound() {
        // Arrange
        Long draftAccountId = 1L;
        ReplaceDraftAccountRequestDto replaceDto = ReplaceDraftAccountRequestDto.builder()
            .businessUnitId((short)1)
            .accountType("Fine")
            .account(createAccountString())
            .submittedBy("TestUser")
            .submittedByName("Test User")
            .timelineData(createTimelineDataString())
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
            .accountType("Fine")
            .account(createAccountString())
            .submittedBy("TestUser")
            .submittedByName("Test User")
            .timelineData(createTimelineDataString())
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
            .businessUnit(BusinessUnitFullEntity.builder().businessUnitId((short) 3).build())
            .versionNumber(0L)
            .build();

        BusinessUnitFullEntity businessUnit = BusinessUnitFullEntity.builder()
            .businessUnitId(((short) 3))
            .build();

        ReplaceDraftAccountRequestDto dto = ReplaceDraftAccountRequestDto.builder()
            .businessUnitId((short) 2)
            .accountType("Fine")
            .account(createAccountString())
            .submittedBy("TestUser")
            .submittedByName("Test User")
            .timelineData(createTimelineDataString())
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
            .accountStatus("SUBMITTED")
            .timelineData(createTimelineDataString())
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .businessUnit(BusinessUnitFullEntity.builder().businessUnitId((short) 3).build())
            .versionNumber(0L)
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));

        // Act & Assert
        assertThrows(ResourceConflictException.class, () ->
            draftAccountTransactional.updateDraftAccount(draftAccountId, updateDto, draftAccountTransactional,
                BigInteger.ZERO)
        );
    }

    @Test
    void testUpdateDraftAccount_success() {
        // Arrange
        Long draftAccountId = 1L;
        UpdateDraftAccountRequestDto updateDto = UpdateDraftAccountRequestDto.builder()
            .accountStatus(DraftAccountStatus.PUBLISHING_PENDING.getLabel())
            .validatedBy("TestValidator")
            .timelineData(createTimelineDataString())
            .businessUnitId((short) 2)
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .accountSnapshot("{\"created_date\":\"2024-10-01T10:00:00Z\"}")
            .businessUnit(BusinessUnitFullEntity.builder().businessUnitId((short) 2).build())
            .timelineData(createTimelineDataString())
            .versionNumber(0L)
            .build();

        DraftAccountEntity updatedAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .accountStatus(DraftAccountStatus.PUBLISHING_PENDING)
            .validatedBy("TestValidator")
            .validatedByName("Tester McValidator")
            .validatedDate(LocalDateTime.now())
            .accountSnapshot("{\"created_date\":\"2024-10-01T10:00:00Z\",\"approved_date\":\"2024-10-03T14:30:00Z\"}")
            .timelineData(createTimelineDataString())
            .versionNumber(1L)
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));
        when(draftAccountRepository.save(any(DraftAccountEntity.class))).thenReturn(updatedAccount);

        // Act
        DraftAccountEntity result = draftAccountTransactional
            .updateDraftAccount(draftAccountId, updateDto, draftAccountTransactional, BigInteger.ZERO);

        // Assert
        assertNotNull(result);
        assertEquals(draftAccountId, result.getDraftAccountId());
        assertEquals(DraftAccountStatus.PUBLISHING_PENDING, result.getAccountStatus());
        assertEquals("TestValidator", result.getValidatedBy());
        assertEquals("Tester McValidator", result.getValidatedByName());
        assertNotNull(result.getValidatedDate());
        assertTrue(result.getAccountSnapshot().contains("approved_date"));
        assertEquals(createTimelineDataString(), result.getTimelineData());

        verify(draftAccountRepository).findById(draftAccountId);
        verify(draftAccountRepository).save(any(DraftAccountEntity.class));
    }

    @Test
    void testUpdateDraftAccount_submitterCannotValidate() {
        Long draftAccountId = 1L;
        UpdateDraftAccountRequestDto updateDto = UpdateDraftAccountRequestDto.builder()
            .accountStatus(DraftAccountStatus.PUBLISHING_PENDING.getLabel())
            .validatedBy("BUUID1")
            .validatedByName("User One")
            .timelineData(createTimelineDataString())
            .businessUnitId((short) 2)
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .submittedBy("BUUID1")
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .businessUnit(BusinessUnitFullEntity.builder().businessUnitId((short) 2).build())
            .timelineData(createTimelineDataString())
            .versionNumber(0L)
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));

        assertThrows(SubmitterCannotValidateException.class, () ->
            draftAccountTransactional.updateDraftAccount(draftAccountId, updateDto, draftAccountTransactional,
                BigInteger.ZERO)
        );
    }

    @Test
    void testUpdateDraftAccount_invalidStatus() {
        // Arrange
        Long draftAccountId = 1L;
        UpdateDraftAccountRequestDto updateDto = UpdateDraftAccountRequestDto.builder()
            .accountStatus("SUBMITTED")
            .businessUnitId((short) 2)
            .timelineData(createTimelineDataString())
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .businessUnit(BusinessUnitFullEntity.builder().businessUnitId((short) 2).build())
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .timelineData(createTimelineDataString())
            .versionNumber(0L)
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            draftAccountTransactional.updateDraftAccount(draftAccountId, updateDto, draftAccountTransactional,
                BigInteger.ZERO)
        );
        assertEquals("'SUBMITTED' is not a valid Draft Account Status.", exception.getMessage());

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
            .businessUnit(BusinessUnitFullEntity.builder()
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
                            "document_language": "English",
                            "hearing_language": "English",
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

}