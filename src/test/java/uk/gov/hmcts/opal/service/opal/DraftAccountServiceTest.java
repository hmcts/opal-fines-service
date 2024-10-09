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
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.DraftAccountStatus;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftAccountServiceTest {

    @Mock
    private DraftAccountRepository draftAccountRepository;

    @Mock
    private BusinessUnitRepository businessUnitRepository;

    @InjectMocks
    private DraftAccountService draftAccountService;

    @Test
    void testGetDraftAccount() {
        // Arrange
        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().build();
        when(draftAccountRepository.getReferenceById(any())).thenReturn(draftAccountEntity);

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
            DraftAccountStatus.REJECTED), Set.of());

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
            .account(createAccountString())
            .build();
        BusinessUnitEntity businessUnit = BusinessUnitEntity.builder()
            .businessUnitName("Old Bailey")
            .build();

        when(businessUnitRepository.findById(any())).thenReturn(Optional.of(businessUnit));
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
            .account("{}")
            .build();
        BusinessUnitEntity businessUnit = BusinessUnitEntity.builder()
            .businessUnitName("Old Bailey")
            .build();

        when(businessUnitRepository.findById(any())).thenReturn(Optional.of(businessUnit));

        // Act
        RuntimeException re = assertThrows(RuntimeException.class, () ->
            draftAccountService.submitDraftAccount(addDraftAccountDto));

        // Assert
        assertEquals("Missing property in path $['accountCreateRequest']", re.getMessage());
    }

    @Test
    void testDeleteDraftAccount_success() {
        // Arrange
        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().createdDate(LocalDateTime.now()).build();
        when(draftAccountRepository.getReferenceById(any())).thenReturn(draftAccountEntity);

        // Act
        draftAccountService.deleteDraftAccount(1, Optional.empty());
    }

    @Test
    void testDeleteDraftAccount_fail1() {
        // Arrange
        DraftAccountEntity draftAccountEntity = mock(DraftAccountEntity.class);
        when(draftAccountEntity.getCreatedDate()).thenThrow(new EntityNotFoundException("No Entity in DB"));
        when(draftAccountRepository.getReferenceById(any())).thenReturn(draftAccountEntity);

        // Act
        EntityNotFoundException enfe = assertThrows(
            EntityNotFoundException.class, () -> draftAccountService.deleteDraftAccount(1, Optional.empty())
        );

        // Assert
        assertEquals("No Entity in DB", enfe.getMessage());
    }

    @Test
    void testDeleteDraftAccount_fail2() {
        // Arrange
        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().build();
        when(draftAccountRepository.getReferenceById(any())).thenReturn(draftAccountEntity);

        // Act
        RuntimeException re = assertThrows(
            RuntimeException.class, () -> draftAccountService.deleteDraftAccount(8, Optional.empty())
        );

        // Assert
        assertEquals("Draft Account entity '8' does not exist in the DB.", re.getMessage());
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
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 2).build())
            .build();

        BusinessUnitEntity businessUnit = BusinessUnitEntity.builder()
            .businessUnitId(((short) 2))
            .businessUnitName("New Bailey")
            .build();

        DraftAccountEntity updatedAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .submittedBy("TestUser")
            .account(createAccountString())
            .accountType("Fine")
            .accountStatus(DraftAccountStatus.RESUBMITTED)
            .timelineData("Timeline data")
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));
        when(businessUnitRepository.findById((short) 2)).thenReturn(Optional.of(businessUnit));
        when(draftAccountRepository.save(any(DraftAccountEntity.class))).thenReturn(updatedAccount);

        // Act
        DraftAccountEntity result = draftAccountService.replaceDraftAccount(draftAccountId, replaceDto);

        // Assert
        assertNotNull(result);
        assertEquals(draftAccountId, result.getDraftAccountId());
        assertEquals("TestUser", result.getSubmittedBy());
        assertEquals(createAccountString(), result.getAccount());
        assertEquals("Fine", result.getAccountType());
        assertEquals(DraftAccountStatus.RESUBMITTED, result.getAccountStatus());
        assertEquals("Timeline data", result.getTimelineData());

        verify(draftAccountRepository).findById(draftAccountId);
        verify(businessUnitRepository).findById((short) 2);
        verify(draftAccountRepository).save(any(DraftAccountEntity.class));
    }

    @Test
    void testReplaceDraftAccount_draftAccountNotFound() {
        // Arrange
        Long draftAccountId = 1L;
        ReplaceDraftAccountRequestDto replaceDto = ReplaceDraftAccountRequestDto.builder().build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            draftAccountService.replaceDraftAccount(draftAccountId, replaceDto)
        );
        assertEquals("Draft Account not found with id: 1", exception.getMessage());
    }

    @Test
    void testReplaceDraftAccount_businessUnitNotFound() {
        // Arrange
        Long draftAccountId = 1L;
        ReplaceDraftAccountRequestDto replaceDto = ReplaceDraftAccountRequestDto.builder()
            .businessUnitId((short) 2)
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder().build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));
        when(businessUnitRepository.findById((short) 2)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            draftAccountService.replaceDraftAccount(draftAccountId, replaceDto)
        );
        assertEquals("Business Unit not found with id: 2", exception.getMessage());
    }

    @Test
    void testReplaceDraftAccount_businessUnitMismatch() {
        // Arrange
        Long draftAccountId = 1L;
        ReplaceDraftAccountRequestDto replaceDto = ReplaceDraftAccountRequestDto.builder()
            .businessUnitId((short) 2)
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 3).build())
            .build();

        BusinessUnitEntity businessUnit = BusinessUnitEntity.builder()
            .businessUnitId(((short) 3))
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));
        when(businessUnitRepository.findById((short) 2)).thenReturn(Optional.of(businessUnit));

        // Act & Assert
        RuntimeException exception = assertThrows(IllegalArgumentException.class, () ->
            draftAccountService.replaceDraftAccount(draftAccountId, replaceDto)
        );
        assertEquals("Business Unit ID does not match the existing draft account", exception.getMessage());
    }

    @Test
    void testUpdateDraftAccount_success() {
        // Arrange
        Long draftAccountId = 1L;
        UpdateDraftAccountRequestDto updateDto = UpdateDraftAccountRequestDto.builder()
            .accountStatus("PENDING")
            .validatedBy("TestValidator")
            .timelineData("Updated timeline data")
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .accountSnapshot("{\"created_date\":\"2024-10-01T10:00:00Z\"}")
            .build();

        DraftAccountEntity updatedAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .accountStatus(DraftAccountStatus.PENDING)
            .validatedBy("TestValidator")
            .validatedDate(LocalDateTime.now())
            .accountSnapshot("{\"created_date\":\"2024-10-01T10:00:00Z\",\"approved_date\":\"2024-10-03T14:30:00Z\"}")
            .timelineData("Updated timeline data")
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));
        when(draftAccountRepository.save(any(DraftAccountEntity.class))).thenReturn(updatedAccount);

        // Act
        DraftAccountEntity result = draftAccountService.updateDraftAccount(draftAccountId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(draftAccountId, result.getDraftAccountId());
        assertEquals(DraftAccountStatus.PENDING, result.getAccountStatus());
        assertEquals("TestValidator", result.getValidatedBy());
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
            .build();

        DraftAccountEntity existingAccount = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .build();

        when(draftAccountRepository.findById(draftAccountId)).thenReturn(Optional.of(existingAccount));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            draftAccountService.updateDraftAccount(draftAccountId, updateDto)
        );
        assertEquals("Invalid account status for update: SUBMITTED", exception.getMessage());

        verify(draftAccountRepository).findById(draftAccountId);
        verify(draftAccountRepository, never()).save(any(DraftAccountEntity.class));
    }

    private String createAccountString() {
        return """
            {
                "accountCreateRequest": {
                    "Defendant": {
                        "Surname": "Windsor",
                        "Forenames": "Charles",
                        "DOB": "August 1958"
                    },
                    "Account": {
                        "AccountType": "Fine"
                    }
                }
            }
            """;
    }
}
