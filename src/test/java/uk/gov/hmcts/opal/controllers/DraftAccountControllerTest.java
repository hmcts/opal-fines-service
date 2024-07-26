package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.service.opal.DraftAccountService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftAccountControllerTest {

    static final String BEARER_TOKEN = "Bearer a_token_here";

    @Mock
    private DraftAccountService draftAccountService;

    @Mock
    private UserStateService userStateService;

    @InjectMocks
    private DraftAccountController draftAccountController;

    @Test
    void testGetDraftAccount_Success() {
        // Arrange
        DraftAccountEntity entity = DraftAccountEntity.builder().build();

        when(draftAccountService.getDraftAccount(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<DraftAccountEntity> response = draftAccountController.getDraftAccountById(1L, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
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
        ResponseEntity<List<DraftAccountEntity>> response = draftAccountController.postDraftAccountsSearch(
            searchDto, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(draftAccountList, response.getBody());
        verify(draftAccountService, times(1)).searchDraftAccounts(any());
    }

    @Test
    void testSaveDraftAccounts_Success() {
        // Arrange
        DraftAccountEntity entity = DraftAccountEntity.builder().build();

        when(draftAccountService.saveDraftAccount(any())).thenReturn(entity);

        // Act
        ResponseEntity<DraftAccountEntity> response = draftAccountController.postDraftAccount(
            entity, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(draftAccountService, times(1)).saveDraftAccount(any());
    }
}
