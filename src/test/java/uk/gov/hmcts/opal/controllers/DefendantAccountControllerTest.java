package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.service.DefendantAccountEnforcementService;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class DefendantAccountControllerTest {

    static final String BEARER_TOKEN = "Bearer a_token_here";

    @Mock
    private DefendantAccountService defendantAccountService;

    @Mock
    private DefendantAccountEnforcementService defendantAccountEnforcementService;

    @InjectMocks
    private DefendantAccountController defendantAccountController;

    @Test
    void testPostDefendantAccountSearch_Success() {
        // Arrange
        AccountSearchDto requestEntity = AccountSearchDto.builder().build();
        DefendantAccountSearchResultsDto mockResponse = DefendantAccountSearchResultsDto.builder().build();

        when(defendantAccountService.searchDefendantAccounts(any(AccountSearchDto.class), eq(BEARER_TOKEN)))
            .thenReturn(mockResponse);

        // Act
        ResponseEntity<DefendantAccountSearchResultsDto> responseEntity =
            defendantAccountController.postDefendantAccountSearch(requestEntity, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(mockResponse, responseEntity.getBody());

        verify(defendantAccountService, times(1))
            .searchDefendantAccounts(any(AccountSearchDto.class), eq(BEARER_TOKEN));
    }

    @Test
    void testGetHeaderSummary_Success() {
        // Arrange
        DefendantAccountHeaderSummary mockBody = new DefendantAccountHeaderSummary();

        when(defendantAccountService.getHeaderSummary(eq(1L), any()))
            .thenReturn(mockBody);

        // Act
        ResponseEntity<DefendantAccountHeaderSummary> response =
            defendantAccountController.getHeaderSummary(1L, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBody, response.getBody());

        verify(defendantAccountService).getHeaderSummary(eq(1L), any());
    }

    @Test
    void testLegacyDefendantAccountService_createGetDefendantAccountRequest() {
        String id = "77";
        var req = LegacyDefendantAccountService.createGetDefendantAccountRequest(id);
        assertNotNull(req);
        assertEquals(id, req.getDefendantAccountId());
    }

    @Test
    void testAddEnforcement_Success() {
        // Arrange
        Long defendantAccountId = 1L;
        String businessUnitId = "10";
        String ifMatch = "1";
        AddDefendantAccountEnforcementRequest request = AddDefendantAccountEnforcementRequest.builder().build();
        AddEnforcementResponse mockResponse = AddEnforcementResponse.builder().build();

        when(defendantAccountEnforcementService.addEnforcement(defendantAccountId, businessUnitId, ifMatch,
            BEARER_TOKEN, request)).thenReturn(mockResponse);

        // Act
        ResponseEntity<AddEnforcementResponse> response =
            defendantAccountController.addEnforcement(defendantAccountId, BEARER_TOKEN, businessUnitId, ifMatch,
                request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());

        verify(defendantAccountEnforcementService).addEnforcement(defendantAccountId, businessUnitId, ifMatch,
            BEARER_TOKEN, request);
    }

}
