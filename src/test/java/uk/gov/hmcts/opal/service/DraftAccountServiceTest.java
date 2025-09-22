package uk.gov.hmcts.opal.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.aspect.PermissionNotAllowedException;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.DraftAccountResponseDto;
import uk.gov.hmcts.opal.dto.DraftAccountsResponseDto;
import uk.gov.hmcts.opal.dto.ReplaceDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.UpdateDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.mapper.DraftAccountMapper;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.service.opal.jpa.DraftAccountTransactions;
import uk.gov.hmcts.opal.service.proxy.DraftAccountPublishProxy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftAccountServiceTest {

    @Mock
    private BusinessUnitRepository businessUnitRepository;

    @Mock
    private UserStateService userStateService;

    @Spy
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Mock
    private DraftAccountTransactions draftAccountTransactions;

    @Mock
    private DraftAccountMapper draftAccountMapper;
    @Mock
    private DraftAccountPublishProxy draftAccountPublishProxy;

    @InjectMocks
    private DraftAccountService draftAccountService;

    @Test
    void testGetDraftAccount() {
        // Arrange
        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().businessUnit(
            BusinessUnitEntity.builder().businessUnitId((short)77).build())
            .build();
        when(draftAccountTransactions.getDraftAccount(anyLong())).thenReturn(draftAccountEntity);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allPermissionsUser());

        // Act
        DraftAccountResponseDto result = draftAccountService.getDraftAccount(1, "authHeaderValue");

        // Assert
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetDraftAccounts() {
        // Arrange

        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().businessUnit(
            BusinessUnitEntity.builder().businessUnitId((short)77).build())
            .build();
        when(draftAccountTransactions.getDraftAccounts(any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(draftAccountEntity));
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allPermissionsUser());

        // Act
        DraftAccountsResponseDto result = draftAccountService.getDraftAccounts(
            Optional.of(List.copyOf(Set.of((short) 1))), Optional.of(List.copyOf(Set.of(DraftAccountStatus.REJECTED))),
            Optional.of(List.of()), Optional.of(List.of()), Optional.empty(), Optional.empty(),
            "authHeaderValue"
        );
        // Assert
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchDraftAccounts() {
        // Arrange
        final String accountText = "myaccount";

        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().account(accountText).build();
        when(draftAccountTransactions.searchDraftAccounts(any())).thenReturn(List.of(draftAccountEntity));
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allPermissionsUser());

        // Act
        List<DraftAccountResponseDto> result = draftAccountService.searchDraftAccounts(
            DraftAccountSearchDto.builder().build(), "authHeaderValue");

        // Assert
        assertEquals(accountText, result.get(0).getAccount());

    }

    @Test
    void testSubmitDraftAccounts_success() {
        // Arrange
        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().build();
        AddDraftAccountRequestDto addDraftAccountDto = AddDraftAccountRequestDto.builder()
            .businessUnitId((short) 2)
            .submittedBy("TestUser")
            .submittedByName("Test User")
            .account(createAccountString())
            .accountType("Fine")
            .timelineData(createTimelineDataString())
            .build();

        when(draftAccountTransactions.submitDraftAccount(any())).thenReturn(draftAccountEntity);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allPermissionsUser());

        // Act
        DraftAccountResponseDto result = draftAccountService.submitDraftAccount(addDraftAccountDto, "authHeaderValue");

        // Assert
        assertEquals(draftAccountEntity.getAccount(), result.getAccount());
    }

    @Test
    void testSubmitDraftAccounts_fail() {
        // Arrange
        AddDraftAccountRequestDto addDraftAccountDto = AddDraftAccountRequestDto.builder()
            .businessUnitId((short)1)
            .accountType("Fine")
            .account(createAccountString())
            .submittedBy("TestUser")
            .submittedByName("Test User")
            .timelineData(createTimelineDataString())
            .build();

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.noPermissionsUser());

        // Act
        assertThrows(PermissionNotAllowedException.class, () ->
            draftAccountService.submitDraftAccount(addDraftAccountDto, "authHeaderValue"));
    }

    @Test
    void testDeleteDraftAccount_success() {
        // Arrange
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allPermissionsUser());

        // Act
        draftAccountService.deleteDraftAccount(1, true, "authHeaderValue");
    }

    @Test
    void testDeleteDraftAccount_fail1() {
        // Arrange
        when(draftAccountTransactions.deleteDraftAccount(anyLong(), anyBoolean(), any())).thenThrow(
            new EntityNotFoundException("Draft Account not found with id: 1"));
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allPermissionsUser());

        // Act
        EntityNotFoundException enfe = assertThrows(
            EntityNotFoundException.class, () -> draftAccountService.deleteDraftAccount(1, true, "authHeaderValue")
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
            .version(0L)
            .build();

        DraftAccountEntity updatedAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .submittedBy("TestUser")
            .account(createAccountString())
            .accountType("Fine")
            .accountStatus(DraftAccountStatus.RESUBMITTED)
            .timelineData(createTimelineDataString())
            .version(1L)
            .build();

        when(draftAccountTransactions.replaceDraftAccount(any(), any(), any())).thenReturn(updatedAccount);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allPermissionsUser());

        // Act
        DraftAccountResponseDto result = draftAccountService.replaceDraftAccount(draftAccountId, replaceDto,
                                                                                 "authHeaderValue");

        // Assert
        assertNotNull(result);
        assertEquals(draftAccountId, result.getDraftAccountId());
        assertEquals("TestUser", result.getSubmittedBy());
        assertEquals(createAccountString(), result.getAccount());
        assertEquals("Fine", result.getAccountType());
        assertEquals(DraftAccountStatus.RESUBMITTED, result.getAccountStatus());
        assertEquals(createTimelineDataString(), result.getTimelineData());

        verify(jsonSchemaValidationService).validateOrError(any(), any());

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
            .version(0L)
            .build();

        when(draftAccountTransactions.replaceDraftAccount(any(), any(), any())).thenThrow(
            new EntityNotFoundException("Draft Account not found with id: " + draftAccountId));
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allPermissionsUser());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            draftAccountService.replaceDraftAccount(draftAccountId, replaceDto, "authHeaderValue")
        );
        assertEquals("Draft Account not found with id: 1", exception.getMessage());
    }

    @Test
    void testReplaceDraftAccount_businessUnitMismatch() {
        // Arrange
        Long draftAccountId = 1L;

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 3).build())
            .version(0L)
            .build();

        ReplaceDraftAccountRequestDto dto = ReplaceDraftAccountRequestDto.builder()
            .businessUnitId((short) 2)
            .accountType("Fine")
            .account(createAccountString())
            .submittedBy("TestUser")
            .submittedByName("Test User")
            .timelineData(createTimelineDataString())
            .version(0L)
            .build();

        when(draftAccountTransactions.replaceDraftAccount(any(), any(), any())).thenReturn(existingAccount);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allPermissionsUser());

        // Act & Assert
        assertThrows(ResourceConflictException.class, () ->
            draftAccountService.replaceDraftAccount(draftAccountId, dto, "authHeaderValue")
        );

        verify(jsonSchemaValidationService).validateOrError(any(), any());
    }

    @Test
    void testUpdateDraftAccount_businessUnitMismatch() {
        // Arrange
        Long draftAccountId = 1L;
        UpdateDraftAccountRequestDto updateDto = UpdateDraftAccountRequestDto.builder()
            .businessUnitId((short) 2)
            .accountStatus("SUBMITTED")
            .timelineData(createTimelineDataString())
            .version(0L)
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 3).build())
            .version(0L)
            .build();

        when(draftAccountTransactions.updateDraftAccount(any(), any(), any())).thenReturn(existingAccount);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allPermissionsUser());

        // Act & Assert
        assertThrows(ResourceConflictException.class, () ->
            draftAccountService.updateDraftAccount(draftAccountId, updateDto, "authHeaderValue")
        );
    }

    @Test
    void testUpdateDraftAccount_success() {
        // Arrange
        Long draftAccountId = 1L;
        UpdateDraftAccountRequestDto updateDto = UpdateDraftAccountRequestDto.builder()
            .accountStatus("PENDING")
            .validatedBy("TestValidator")
            .timelineData(createTimelineDataString())
            .businessUnitId((short) 2)
            .version(0L)
            .build();

        DraftAccountEntity updatedAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .accountStatus(DraftAccountStatus.PUBLISHING_PENDING)
            .validatedBy("TestValidator")
            .validatedByName("Tester McValidator")
            .validatedDate(LocalDateTime.now())
            .accountSnapshot("{\"created_date\":\"2024-10-01T10:00:00Z\",\"approved_date\":\"2024-10-03T14:30:00Z\"}")
            .timelineData(createTimelineDataString())
            .version(1L)
            .build();

        when(draftAccountTransactions.updateDraftAccount(any(), any(), any())).thenReturn(updatedAccount);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allPermissionsUser());
        when(draftAccountPublishProxy.publishDefendantAccount(any(), any())).thenReturn(updatedAccount);

        // Act
        DraftAccountResponseDto result = draftAccountService.updateDraftAccount(draftAccountId,
                                                                           updateDto, "authHeaderValue");

        // Assert
        assertNotNull(result);
        assertEquals(draftAccountId, result.getDraftAccountId());
        assertEquals(DraftAccountStatus.PUBLISHING_PENDING, result.getAccountStatus());
        assertEquals("TestValidator", result.getValidatedBy());
        assertEquals("Tester McValidator", result.getValidatedByName());
        assertNotNull(result.getValidatedDate());
        assertTrue(result.getAccountSnapshot().contains("approved_date"));
        assertEquals(createTimelineDataString(), result.getTimelineData());

        verify(jsonSchemaValidationService).validateOrError(any(), any());
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
