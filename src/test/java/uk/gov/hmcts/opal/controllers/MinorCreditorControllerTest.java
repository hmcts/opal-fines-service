package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.Creditor;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.service.MinorCreditorService;
import uk.gov.hmcts.opal.service.opal.OpalCreditorAccountService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinorCreditorControllerTest {

    static final String BEARER_TOKEN = "Bearer a_token_here";

    @Mock
    MinorCreditorService minorCreditorService;

    @Mock
    OpalCreditorAccountService opalCreditorAccountService;

    @InjectMocks
    private MinorCreditorController minorCreditorController;

    @Test
    void testPostMinorCreditorSearch_Success() {
        // Arrange
        PostMinorCreditorAccountsSearchResponse mockResponse = new PostMinorCreditorAccountsSearchResponse();

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(101, 202, 303))
            .activeAccountsOnly(true)
            .accountNumber("ACC123456")
            .creditor(new Creditor(/* set Creditor fields as needed */))
            .build();

        when(minorCreditorService.searchMinorCreditors(any(), any())).thenReturn(mockResponse);

        // Act
        ResponseEntity<PostMinorCreditorAccountsSearchResponse> responseEntity =
            minorCreditorController.postMinorCreditorsSearch(search, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(minorCreditorService, times(1)).searchMinorCreditors(any(), any());
    }

    @Test
    void testGetMinorCreditorAccountAtAGlance_Success() {
        // Arrange
        GetMinorCreditorAccountAtAGlanceResponse mockResponse = new GetMinorCreditorAccountAtAGlanceResponse();

        when(minorCreditorService.getMinorCreditorAtAGlance("creditorId", BEARER_TOKEN)).thenReturn(mockResponse);

        // Act
        ResponseEntity<GetMinorCreditorAccountAtAGlanceResponse> responseEntity =
            minorCreditorController.getMinorCreditorsAtAGlance("creditorId",  BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
    }

    @Test
    void testDeleteMinorCreditor_Success() {
        // Arrange
        when(opalCreditorAccountService.deleteCreditorAccount(anyLong(), anyBoolean(), anyString())).thenReturn("OK");

        // Act
        ResponseEntity<String> responseEntity = minorCreditorController
            .deleteMinorCreditorById(444L, "auth", "if-match", Optional.of(false));

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("OK", responseEntity.getBody());
    }

}
