package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.DraftAccountResponseDto;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.DraftAccountStatus;
import uk.gov.hmcts.opal.service.opal.DraftAccountService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.util.DateTimeUtils.toOffsetDateTime;

@ExtendWith(MockitoExtension.class)
class DraftAccountControllerTest {

    static final String BEARER_TOKEN = "Bearer a_token_here";

    @Mock
    private DraftAccountService draftAccountService;

    @Mock
    private UserStateService userStateService;

    @Spy
    private JsonSchemaValidationService jsonSchemaValidationService;

    @InjectMocks
    private DraftAccountController draftAccountController;

    @Test
    void testGetDraftAccount_Success() {
        // Arrange
        DraftAccountEntity entity = DraftAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().build())
            .build();

        when(draftAccountService.getDraftAccount(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<DraftAccountResponseDto> response = draftAccountController
            .getDraftAccountById(1L, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(toGetDto(entity), response.getBody());
        verify(draftAccountService, times(1)).getDraftAccount(any(Long.class));
    }

    @Test
    void testSearchDraftAccounts_Success() {
        // Arrange
        DraftAccountEntity entity = DraftAccountEntity.builder().build();
        List<DraftAccountEntity> draftAccountList = List.of(entity);

        when(draftAccountService.searchDraftAccounts(any())).thenReturn(draftAccountList);

        // Act
        DraftAccountSearchDto searchDto = DraftAccountSearchDto.builder().build();
        ResponseEntity<List<DraftAccountResponseDto>> response = draftAccountController.postDraftAccountsSearch(
            searchDto, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(draftAccountService, times(1)).searchDraftAccounts(any());
    }

    @Test
    void testSaveDraftAccounts_Success() {
        // Arrange
        DraftAccountEntity entity = DraftAccountEntity.builder()
            .accountType("Large")
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .account("{\"acc\": \"1\"}")
            .businessUnit(BusinessUnitEntity.builder().build())
            .submittedBy("USER_ID")
            .timelineData("{\"dat\": \"2\"}")
            .build();
        AddDraftAccountRequestDto addDraftAccountDto = AddDraftAccountRequestDto.builder()
            .accountType("Large")
            .account("{\"acc\": \"1\"}")
            .businessUnitId((short)1)
            .submittedBy("USER_ID")
            .timelineData("{\"dat\": \"2\"}")
            .build();

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(new UserState.DeveloperUserState());
        when(draftAccountService.submitDraftAccount(any())).thenReturn(entity);

        // Act
        ResponseEntity<DraftAccountResponseDto> response = draftAccountController.postDraftAccount(
            addDraftAccountDto, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        DraftAccountResponseDto responseEntity = response.getBody();
        assertEquals("Large", responseEntity.getAccountType());
        assertEquals("Submitted", responseEntity.getAccountStatus().getLabel());
        assertEquals("{\"acc\": \"1\"}", responseEntity.getAccount());
        assertEquals("USER_ID", responseEntity.getSubmittedBy());
        assertEquals("{\"dat\": \"2\"}", responseEntity.getTimelineData());
        verify(draftAccountService, times(1)).submitDraftAccount(any());
    }

    @Test
    void testDeleteDraftAccount_Success() {
        // Act
        ResponseEntity<String> response = draftAccountController
            .deleteDraftAccountById(7L, BEARER_TOKEN, Optional.empty());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("""
                         { "message": "Draft Account '7' deleted"}""", response.getBody());
        verify(draftAccountService, times(1)).deleteDraftAccount(any(Long.class), any());
    }

    DraftAccountResponseDto toGetDto(DraftAccountEntity entity) {
        return DraftAccountResponseDto.builder()
            .draftAccountId(entity.getDraftAccountId())
            .businessUnitId(entity.getBusinessUnit().getBusinessUnitId())
            .createdDate(toOffsetDateTime(entity.getCreatedDate()))
            .submittedBy(entity.getSubmittedBy())
            .validatedDate(toOffsetDateTime(entity.getValidatedDate()))
            .validatedBy(entity.getValidatedBy())
            .account(entity.getAccount())
            .accountSnapshot(entity.getAccountSnapshot())
            .accountType(entity.getAccountType())
            .accountStatus(entity.getAccountStatus())
            .timelineData(entity.getTimelineData())
            .accountNumber(entity.getAccountNumber())
            .accountId(entity.getAccountId())
            .build();
    }
}
