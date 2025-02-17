package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.FluentQuery;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.ReplaceDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.UpdateDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.DraftAccountStatus;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnit;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.repository.BusinessUnitLiteRepository;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftAccountServiceTest {

    @Mock
    private DraftAccountRepository draftAccountRepository;

    @Mock
    private BusinessUnitLiteRepository businessUnitLiteRepository;

    @InjectMocks
    private DraftAccountService draftAccountService;

    @Test
    void testGetDraftAccount() {
        // Arrange
        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().build();
        when(draftAccountRepository.findById(any())).thenReturn(Optional.of(draftAccountEntity));

        // Act
        DraftAccountEntity result = draftAccountService.getDraftAccount(1);

        // Assert
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetDraftAccounts() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().build();
        Page<DraftAccountEntity> mockPage = new PageImpl<>(List.of(draftAccountEntity), Pageable.unpaged(), 999L);
        when(draftAccountRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<DraftAccountEntity> result = draftAccountService.getDraftAccounts(Set.of((short)1), Set.of(
            DraftAccountStatus.REJECTED), Set.of(), Set.of());

        // Assert
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchDraftAccounts() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().build();
        Page<DraftAccountEntity> mockPage = new PageImpl<>(List.of(draftAccountEntity), Pageable.unpaged(), 999L);
        when(draftAccountRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<DraftAccountEntity> result = draftAccountService.searchDraftAccounts(
            DraftAccountSearchDto.builder().build());

        // Assert
        assertEquals(List.of(draftAccountEntity), result);

    }

    @Test
    void testSubmitDraftAccounts_success() {
        // Arrange
        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().build();
        AddDraftAccountRequestDto addDraftAccountDto = AddDraftAccountRequestDto.builder()
            .businessUnitId((short)1)
            .account(createAccountString())
            .build();
        BusinessUnit.Lite businessUnit = BusinessUnit.Lite.builder()
            .businessUnitName("Old Bailey")
            .build();

        when(businessUnitLiteRepository.getReferenceById(any())).thenReturn(businessUnit);
        when(draftAccountRepository.save(any(DraftAccountEntity.class))).thenReturn(draftAccountEntity);

        // Act
        DraftAccountEntity result = draftAccountService.submitDraftAccount(addDraftAccountDto);

        // Assert
        assertEquals(draftAccountEntity, result);
    }

    @Test
    void testSubmitDraftAccounts_fail() {
        // Arrange
        AddDraftAccountRequestDto addDraftAccountDto = AddDraftAccountRequestDto.builder()
            .businessUnitId((short)1)
            .account("{}")
            .build();
        BusinessUnit.Lite businessUnit = BusinessUnit.Lite.builder()
            .businessUnitName("Old Bailey")
            .build();

        when(businessUnitLiteRepository.getReferenceById(any())).thenReturn(businessUnit);

        // Act
        RuntimeException re = assertThrows(RuntimeException.class, () ->
            draftAccountService.submitDraftAccount(addDraftAccountDto));

        // Assert
        assertEquals("Missing property in path $['defendant']", re.getMessage());
    }

    @Test
    void testDeleteDraftAccount_success() {
        // Arrange
        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().createdDate(LocalDateTime.now()).build();
        when(draftAccountRepository.findById(any())).thenReturn(Optional.of(draftAccountEntity));

        // Act
        draftAccountService.deleteDraftAccount(1, true, draftAccountService);
    }

    @Test
    void testDeleteDraftAccount_fail1() {
        // Arrange
        when(draftAccountRepository.findById(any())).thenReturn(Optional.empty());

        // Act
        EntityNotFoundException enfe = assertThrows(
            EntityNotFoundException.class, () -> draftAccountService.deleteDraftAccount(1, true, draftAccountService)
        );

        // Assert
        assertEquals("Draft Account not found with id: 1", enfe.getMessage());
    }

    @Test
    void testDeleteDraftAccount_fail2() {
        // Arrange
        when(draftAccountRepository.findById(any())).thenReturn(Optional.empty());

        // Act
        boolean response = draftAccountService.deleteDraftAccount(8, false, draftAccountService);

        // Assert
        assertEquals(false, response);
    }

    @Test
    void testReplaceDraftAccount_success() {
        // Arrange
        Long draftAccountId = 1L;
        ReplaceDraftAccountRequestDto replaceDto = ReplaceDraftAccountRequestDto.builder()
            .businessUnitId((short) 2)
            .submittedBy("TestUser")
            .account(createAccountString())
            .accountType("Fine")
            .timelineData("Timeline data")
            .version(0L)
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .businessUnit(BusinessUnit.Lite.builder().businessUnitId((short) 2).build())
            .createdDate(LocalDateTime.now())
            .version(0L)
            .build();

        BusinessUnit.Lite businessUnit = BusinessUnit.Lite.builder()
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
            .version(1L)
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));
        when(businessUnitLiteRepository.findById((short) 2)).thenReturn(Optional.of(businessUnit));
        when(draftAccountRepository.save(any(DraftAccountEntity.class))).thenReturn(updatedAccount);

        // Act
        DraftAccountEntity result = draftAccountService.replaceDraftAccount(draftAccountId, replaceDto,
                                                                            draftAccountService);

        // Assert
        assertNotNull(result);
        assertEquals(draftAccountId, result.getDraftAccountId());
        assertEquals("TestUser", result.getSubmittedBy());
        assertEquals(createAccountString(), result.getAccount());
        assertEquals("Fine", result.getAccountType());
        assertEquals(DraftAccountStatus.RESUBMITTED, result.getAccountStatus());
        assertEquals(createTimelineDataString(), result.getTimelineData());

        verify(draftAccountRepository).findById(draftAccountId);
        verify(businessUnitLiteRepository).findById((short) 2);
        verify(draftAccountRepository).save(any(DraftAccountEntity.class));
    }

    @Test
    void testReplaceDraftAccount_draftAccountNotFound() {
        // Arrange
        Long draftAccountId = 1L;
        ReplaceDraftAccountRequestDto replaceDto = ReplaceDraftAccountRequestDto.builder()
            .businessUnitId((short)1)
            .version(0L)
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            draftAccountService.replaceDraftAccount(draftAccountId, replaceDto, draftAccountService)
        );
        assertEquals("Draft Account not found with id: 1", exception.getMessage());
    }

    @Test
    void testReplaceDraftAccount_businessUnitNotFound() {
        // Arrange
        Long draftAccountId = 1L;
        ReplaceDraftAccountRequestDto replaceDto = ReplaceDraftAccountRequestDto.builder()
            .businessUnitId((short) 2)
            .version(0L)
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder().version(0L).build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));
        when(businessUnitLiteRepository.findById((short) 2)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            draftAccountService.replaceDraftAccount(draftAccountId, replaceDto, draftAccountService)
        );
        assertEquals("Business Unit not found with id: 2", exception.getMessage());
    }

    @Test
    void testReplaceDraftAccount_businessUnitMismatch() {
        // Arrange
        Long draftAccountId = 1L;
        ReplaceDraftAccountRequestDto replaceDto = ReplaceDraftAccountRequestDto.builder()
            .businessUnitId((short) 2)
            .version(0L)
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .businessUnit(BusinessUnit.Lite.builder().businessUnitId((short) 3).build())
            .version(0L)
            .build();

        BusinessUnit.Lite businessUnit = BusinessUnit.Lite.builder()
            .businessUnitId(((short) 3))
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));
        when(businessUnitLiteRepository.findById((short) 2)).thenReturn(Optional.of(businessUnit));

        // Act & Assert
        assertThrows(ResourceConflictException.class, () ->
            draftAccountService.replaceDraftAccount(draftAccountId, replaceDto, draftAccountService)
        );
    }

    @Test
    void testUpdateDraftAccount_businessUnitMismatch() {
        // Arrange
        Long draftAccountId = 1L;
        UpdateDraftAccountRequestDto updateDto = UpdateDraftAccountRequestDto.builder()
            .businessUnitId((short) 2)
            .version(0L)
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .businessUnit(BusinessUnit.Lite.builder().businessUnitId((short) 3).build())
            .version(0L)
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));

        // Act & Assert
        assertThrows(ResourceConflictException.class, () ->
            draftAccountService.updateDraftAccount(draftAccountId, updateDto, draftAccountService)
        );
    }

    @Test
    void testUpdateDraftAccount_success() {
        // Arrange
        Long draftAccountId = 1L;
        UpdateDraftAccountRequestDto updateDto = UpdateDraftAccountRequestDto.builder()
            .accountStatus("PENDING")
            .validatedBy("TestValidator")
            .timelineData("Updated timeline data")
            .businessUnitId((short) 2)
            .version(0L)
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .accountSnapshot("{\"created_date\":\"2024-10-01T10:00:00Z\"}")
            .businessUnit(BusinessUnit.Lite.builder().businessUnitId((short) 2).build())
            .version(0L)
            .build();

        DraftAccountEntity updatedAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .accountStatus(DraftAccountStatus.PENDING)
            .validatedBy("TestValidator")
            .validatedByName("Tester McValidator")
            .validatedDate(LocalDateTime.now())
            .accountSnapshot("{\"created_date\":\"2024-10-01T10:00:00Z\",\"approved_date\":\"2024-10-03T14:30:00Z\"}")
            .timelineData("Updated timeline data")
            .version(1L)
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));
        when(draftAccountRepository.save(any(DraftAccountEntity.class))).thenReturn(updatedAccount);

        // Act
        DraftAccountEntity result = draftAccountService.updateDraftAccount(draftAccountId,
                                                                           updateDto, draftAccountService);

        // Assert
        assertNotNull(result);
        assertEquals(draftAccountId, result.getDraftAccountId());
        assertEquals(DraftAccountStatus.PENDING, result.getAccountStatus());
        assertEquals("TestValidator", result.getValidatedBy());
        assertEquals("Tester McValidator", result.getValidatedByName());
        assertNotNull(result.getValidatedDate());
        assertTrue(result.getAccountSnapshot().contains("approved_date"));
        assertEquals("Updated timeline data", result.getTimelineData());

        verify(draftAccountRepository).findById(draftAccountId);
        verify(draftAccountRepository).save(any(DraftAccountEntity.class));
    }

    @Test
    void testUpdateDraftAccount_invalidStatus() {
        // Arrange
        Long draftAccountId = 1L;
        UpdateDraftAccountRequestDto updateDto = UpdateDraftAccountRequestDto.builder()
            .accountStatus("SUBMITTED")
            .businessUnitId((short) 2)
            .version(0L)
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .businessUnit(BusinessUnit.Lite.builder().businessUnitId((short) 2).build())
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .version(0L)
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            draftAccountService.updateDraftAccount(draftAccountId, updateDto, draftAccountService)
        );
        assertEquals("Invalid account status for update: SUBMITTED", exception.getMessage());

        verify(draftAccountRepository).findById(draftAccountId);
        verify(draftAccountRepository, never()).save(any(DraftAccountEntity.class));
    }

    private String createTimelineDataString() {
        return """
            {
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
                     }
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
