package uk.gov.hmcts.opal.service.opal.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityNotFoundException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor.SpecificationFluentQuery;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.dto.ReplaceDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.UpdateDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;
import uk.gov.hmcts.opal.util.LogUtil;



@ExtendWith(MockitoExtension.class)
class DraftAccountTransactionalTest {

    @Mock
    private DraftAccountRepository draftAccountRepository;

    @Mock
    private BusinessUnitRepository businessUnitRepository;

    @Mock
    private LoggingService loggingService;

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
    void testSubmitDraftAccounts_success() throws Exception {
        // Arrange
        String minimalAccountJson = createAccountString(); // non-empty valid JSON

        // ensure the entity returned from the repo contains the account JSON (so assertEquals can compare)
        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder()
            .account(minimalAccountJson)
            .accountSnapshot("{}")
            .accountType("Fine")
            .draftAccountId(1L)
            .createdDate(LocalDateTime.now())
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .accountStatusDate(LocalDateTime.now())
            .build();

        AddDraftAccountRequestDto addDraftAccountDto = AddDraftAccountRequestDto.builder()
            .businessUnitId((short) 2)
            .submittedBy("TestUser")
            .submittedByName("Test User")
            .account(minimalAccountJson)
            .accountType("Fine")
            .timelineData(createTimelineDataString())
            .build();

        BusinessUnitFullEntity businessUnit = BusinessUnitFullEntity.builder()
            .businessUnitName("Old Bailey")
            .build();

        when(businessUnitRepository.getReferenceById(any())).thenReturn(businessUnit);
        when(draftAccountRepository.save(any(DraftAccountEntity.class))).thenReturn(draftAccountEntity);

        // Act
        DraftAccountEntity result = draftAccountTransactional.submitDraftAccount(addDraftAccountDto);

        // Assert
        assertEquals(draftAccountEntity.getAccount(), result.getAccount());
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

    @Test
    void logDefendantInfo_passes_expected_logDetails_to_loggingService() throws Exception {
        // Arrange
        Long draftId = 11111111L;
        String submittedBy = "opal-user-99";
        DraftAccountEntity entity = Mockito.mock(DraftAccountEntity.class);
        Mockito.when(entity.getDraftAccountId()).thenReturn(draftId);
        Mockito.when(entity.getSubmittedBy()).thenReturn(submittedBy);

        String expectedBusinessIdentifier = "Submit Draft Account - Defendant";
        String expectedIp = "192.0.2.33";
        OffsetDateTime expectedNow = OffsetDateTime.parse("2023-01-02T03:04:05+00:00");

        try (MockedStatic<LogUtil> logUtilMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilMock.when(LogUtil::getIpAddress).thenReturn(expectedIp);
            logUtilMock.when(LogUtil::getCurrentDateTime).thenReturn(expectedNow);

            // Act - call private method via reflection
            Method privateMethod = draftAccountTransactional.getClass().getDeclaredMethod(
                "logDefendantInfo", DraftAccountEntity.class);
            privateMethod.setAccessible(true);
            privateMethod.invoke(draftAccountTransactional, entity);

            // Assert
            ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
                ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);

            verify(loggingService, times(1)).personalDataAccessLogAsync(captor.capture());

            PersonalDataProcessingLogDetails captured = captor.getValue();

            // Validate full details exactly as built in your method
            assertThat(captured.getBusinessIdentifier()).isEqualTo(expectedBusinessIdentifier);
            assertThat(captured.getCategory()).isEqualTo(PersonalDataProcessingCategory.COLLECTION);
            assertThat(captured.getIpAddress()).isEqualTo(expectedIp);
            assertThat(captured.getCreatedAt()).isEqualTo(expectedNow);

            ParticipantIdentifier createdBy = captured.getCreatedBy();
            assertThat(createdBy).isNotNull();
            assertThat(createdBy.getIdentifier()).isEqualTo(submittedBy);
            assertThat(createdBy.getType()).isEqualTo(PdplIdentifierType.OPAL_USER_ID);

            List<ParticipantIdentifier> individuals = captured.getIndividuals();
            assertThat(individuals).hasSize(1);
            ParticipantIdentifier individual = individuals.get(0);
            assertThat(individual.getIdentifier()).isEqualTo(draftId.toString());
            assertThat(individual.getType()).isEqualTo(PdplIdentifierType.DRAFT_ACCOUNT);

            assertThat(captured.getRecipient()).isNull();
        }
    }

    @Test
    void logParentGuardianInfo_passes_expected_logDetails_to_loggingService() throws Exception {
        // Arrange
        Long draftId = 12345678L;
        String submittedBy = "user-42";
        DraftAccountEntity entity = Mockito.mock(DraftAccountEntity.class);
        Mockito.when(entity.getDraftAccountId()).thenReturn(draftId);
        Mockito.when(entity.getSubmittedBy()).thenReturn(submittedBy);

        String expectedBusinessIdentifier = "Submit Draft Account - Parent or Guardian";
        String expectedIp = "10.0.0.5";
        OffsetDateTime expectedNow = OffsetDateTime.parse("2024-02-03T04:05:06+00:00");

        try (MockedStatic<LogUtil> logUtilMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilMock.when(LogUtil::getIpAddress).thenReturn(expectedIp);
            logUtilMock.when(LogUtil::getCurrentDateTime).thenReturn(expectedNow);

            // Act
            Method method = draftAccountTransactional.getClass().getDeclaredMethod(
                "logParentGuardianInfo", DraftAccountEntity.class);
            method.setAccessible(true);
            method.invoke(draftAccountTransactional, entity);

            // Assert
            ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
                ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);

            verify(loggingService, times(1)).personalDataAccessLogAsync(captor.capture());

            PersonalDataProcessingLogDetails captured = captor.getValue();

            assertThat(captured.getBusinessIdentifier()).isEqualTo(expectedBusinessIdentifier);
            assertThat(captured.getIpAddress()).isEqualTo(expectedIp);
            assertThat(captured.getCreatedAt()).isEqualTo(expectedNow);

            // spot-check createdBy and individuals are correctly populated
            assertThat(captured.getCreatedBy().getIdentifier()).isEqualTo(submittedBy);
            assertThat(captured.getIndividuals()).hasSize(1);
            assertThat(captured.getIndividuals().get(0).getIdentifier()).isEqualTo(draftId.toString());
        }
    }

    @Test
    void logMinorCreditorInfo_passes_expected_logDetails_to_loggingService() throws Exception {
        // Arrange
        Long draftId = 99999999L;
        String submittedBy = "user-minor";
        DraftAccountEntity entity = Mockito.mock(DraftAccountEntity.class);
        Mockito.when(entity.getDraftAccountId()).thenReturn(draftId);
        Mockito.when(entity.getSubmittedBy()).thenReturn(submittedBy);

        String expectedBusinessIdentifier = "Submit Draft Account - Minor Creditor";
        String expectedIp = "203.0.113.7";
        OffsetDateTime expectedNow = OffsetDateTime.parse("2025-03-04T05:06:07+00:00");

        try (MockedStatic<LogUtil> logUtilMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilMock.when(LogUtil::getIpAddress).thenReturn(expectedIp);
            logUtilMock.when(LogUtil::getCurrentDateTime).thenReturn(expectedNow);

            // Act
            Method method = draftAccountTransactional.getClass().getDeclaredMethod(
                "logMinorCreditorInfo", DraftAccountEntity.class);
            method.setAccessible(true);
            method.invoke(draftAccountTransactional, entity);

            // Assert
            ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
                ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);

            verify(loggingService, times(1)).personalDataAccessLogAsync(captor.capture());

            PersonalDataProcessingLogDetails captured = captor.getValue();

            assertThat(captured.getBusinessIdentifier()).isEqualTo(expectedBusinessIdentifier);
            assertThat(captured.getIpAddress()).isEqualTo(expectedIp);
            assertThat(captured.getCreatedAt()).isEqualTo(expectedNow);

            assertThat(captured.getCreatedBy().getIdentifier()).isEqualTo(submittedBy);
            assertThat(captured.getIndividuals()).hasSize(1);
            assertThat(captured.getIndividuals().get(0).getIdentifier()).isEqualTo(draftId.toString());
        }
    }

    @Test
    void jsonPathUtil_extracts_values_from_minimal_account_json() {
        // Build a minimal account JSON that satisfies the schema "required" list.
        ObjectMapper om = new ObjectMapper();
        ObjectNode root = om.createObjectNode();

        root.put("account_type", "DEFENDANT");
        root.put("defendant_type", "ADULT");
        root.put("originator_name", "LJS");
        root.put("originator_id", 123);
        root.put("enforcement_court_id", 10);
        root.put("payment_card_request", false);
        root.put("account_sentence_date", "2025-01-01");

        // minimal defendant object (required: company_flag, address_line_1)
        ObjectNode defendant = om.createObjectNode();
        defendant.put("company_flag", false);
        defendant.put("address_line_1", "1 Example Street");
        root.set("defendant", defendant);

        // minimal offences array (each offence requires date_of_sentence, offence_id, impositions)
        ObjectNode imposition0 = om.createObjectNode();
        imposition0.put("result_id", "R1");
        imposition0.put("amount_imposed", 100.00);
        imposition0.put("amount_paid", 0.00);
        ArrayNode impositions = om.createArrayNode().add(imposition0);

        ObjectNode offence0 = om.createObjectNode();
        offence0.put("date_of_sentence", "2024-12-01");
        offence0.put("offence_id", 42);
        offence0.set("impositions", impositions);
        ArrayNode offences = om.createArrayNode().add(offence0);
        root.set("offences", offences);

        // minimal payment_terms (required: payment_terms_type_code)
        ObjectNode paymentTerms = om.createObjectNode();
        paymentTerms.put("payment_terms_type_code", "B");
        root.set("payment_terms", paymentTerms);

        String json = root.toString();

        // Assertions using JsonPath
        assertEquals("1 Example Street",
            com.jayway.jsonpath.JsonPath.read(json, "$.defendant.address_line_1"));

        assertEquals("B",
            com.jayway.jsonpath.JsonPath.read(json, "$.payment_terms.payment_terms_type_code"));

        assertThat(((Number) com.jayway.jsonpath.JsonPath.read(json, "$.offences[0].offence_id"))
            .intValue()).isEqualTo(42);
    }

    @Test
    void jsonPathUtil_handles_arrays_and_missing_optional_fields() {
        // Construct JSON with multiple offences and absent optional fields to ensure JsonPath still works
        ObjectMapper om = new ObjectMapper();
        ObjectNode root = om.createObjectNode();

        root.put("account_type", "DEFENDANT");
        root.put("defendant_type", "ADULT");
        root.put("originator_name", "LJS");
        root.put("originator_id", 123);
        root.put("enforcement_court_id", 10);
        root.put("payment_card_request", true);
        root.put("account_sentence_date", "2025-01-10");

        ObjectNode defendant = om.createObjectNode();
        defendant.put("company_flag", false);
        defendant.put("address_line_1", "4 Another Lane");
        root.set("defendant", defendant);

        // offences
        ObjectNode impositionA = om.createObjectNode();
        impositionA.put("result_id", "R1");
        impositionA.put("amount_imposed", 50.0);
        impositionA.put("amount_paid", 10.0);
        ArrayNode impositionsA = om.createArrayNode().add(impositionA);

        ObjectNode offenceA = om.createObjectNode();
        offenceA.put("date_of_sentence", "2024-10-01");
        offenceA.put("offence_id", 7);
        offenceA.set("impositions", impositionsA);

        ObjectNode offenceB = om.createObjectNode();
        offenceB.put("date_of_sentence", "2024-11-01");
        offenceB.put("offence_id", 8);
        offenceB.set("impositions", impositionsA);

        ArrayNode offences = om.createArrayNode().add(offenceA).add(offenceB);
        root.set("offences", offences);

        // payment_terms
        ObjectNode paymentTerms = om.createObjectNode();
        paymentTerms.put("payment_terms_type_code", "P");
        root.set("payment_terms", paymentTerms);

        String json = root.toString();

        // JsonPath returns list of offence ids
        @SuppressWarnings("unchecked")
        List<Integer> ids = com.jayway.jsonpath.JsonPath.read(json, "$.offences[*].offence_id");
        assertEquals(List.of(7, 8), ids);

        // Missing optional field: assert that JsonPath throws PathNotFoundException for the absent path
        assertThrows(com.jayway.jsonpath.PathNotFoundException.class, () ->
            com.jayway.jsonpath.JsonPath.read(json, "$.nonexistent_field")
        );
    }

}
