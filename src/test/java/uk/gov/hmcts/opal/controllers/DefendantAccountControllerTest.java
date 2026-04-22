package uk.gov.hmcts.opal.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldRequest;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.service.DefendantAccountEnforcementService;
import uk.gov.hmcts.opal.service.DefendantAccountPartyService;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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

    @Mock
    private DefendantAccountPartyService defendantAccountPartyService;

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
    void testAddEnforcement_Success() throws JsonProcessingException {
        // Arrange
        Long defendantAccountId = 1L;
        Short businessUnitId = 10;
        Long ifMatch = 1L;
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

    @Test
    void testRemoveEnforcementHold_Success() {
        Long defendantAccountId = 1L;
        Short businessUnitId = 10;
        String ifMatch = "\"7\"";

        RemoveDefendantAccountEnforcementHoldRequest request =
            RemoveDefendantAccountEnforcementHoldRequest.builder()
                .reason("remove hold reason")
                .build();

        RemoveDefendantAccountEnforcementHoldResponse expectedResponse =
            RemoveDefendantAccountEnforcementHoldResponse.builder().build();

        when(defendantAccountEnforcementService.removeEnforcementHold(
            defendantAccountId,
            businessUnitId,
            ifMatch,
            BEARER_TOKEN,
            request
        )).thenReturn(expectedResponse);

        ResponseEntity<RemoveDefendantAccountEnforcementHoldResponse> response =
            defendantAccountController.removeEnforcementHold(
                defendantAccountId,
                businessUnitId,
                ifMatch,
                BEARER_TOKEN,
                request
            );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void testAddDefendantAccountParty_Success() {
        // Arrange
        Long defendantAccountId = 1L;
        String businessUnitId = "10";
        String ifMatch = "1";

        AddDefendantAccountPartyRequest request = new AddDefendantAccountPartyRequest();
        GetDefendantAccountPartyResponse mockResponse = new GetDefendantAccountPartyResponse();

        when(defendantAccountPartyService.addDefendantAccountParty(
            defendantAccountId,
            BEARER_TOKEN,
            ifMatch,
            businessUnitId,
            request
        )).thenReturn(mockResponse);

        // Act
        ResponseEntity<GetDefendantAccountPartyResponse> response =
            defendantAccountController.addDefendantAccountParty(
                defendantAccountId,
                businessUnitId,
                ifMatch,
                BEARER_TOKEN,
                request
            );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());

        verify(defendantAccountPartyService).addDefendantAccountParty(
            defendantAccountId,
            BEARER_TOKEN,
            ifMatch,
            businessUnitId,
            request
        );
    }

    @Test
    void testRemoveEnforcementHold_forbiddenWhenServiceThrowsPermissionNotAllowedException() {
        Long defendantAccountId = 1L;
        Short businessUnitId = 10;
        String ifMatch = "\"7\"";

        RemoveDefendantAccountEnforcementHoldRequest request =
            RemoveDefendantAccountEnforcementHoldRequest.builder()
                .reason("remove hold reason")
                .build();

        when(defendantAccountEnforcementService.removeEnforcementHold(
            defendantAccountId,
            businessUnitId,
            ifMatch,
            BEARER_TOKEN,
            request
        )).thenThrow(new PermissionNotAllowedException(FinesPermission.ENTER_ENFORCEMENT));

        assertThrows(PermissionNotAllowedException.class, () ->
            defendantAccountController.removeEnforcementHold(
                defendantAccountId,
                businessUnitId,
                ifMatch,
                BEARER_TOKEN,
                request
            )
        );
    }

    @Test
    void testRemoveEnforcementHold_conflictWhenServiceThrowsResourceConflictException() {
        Long defendantAccountId = 1L;
        Short businessUnitId = 10;
        String ifMatch = null;

        RemoveDefendantAccountEnforcementHoldRequest request =
            RemoveDefendantAccountEnforcementHoldRequest.builder()
                .reason("remove hold reason")
                .build();

        when(defendantAccountEnforcementService.removeEnforcementHold(
            eq(defendantAccountId),
            eq(businessUnitId),
            isNull(),
            eq(BEARER_TOKEN),
            eq(request)
        )).thenThrow(new ResourceConflictException(
            "Defendant Account",
            defendantAccountId,
            "If-Match header is required",
            null
        ));

        assertThrows(ResourceConflictException.class, () ->
            defendantAccountController.removeEnforcementHold(
                defendantAccountId,
                businessUnitId,
                ifMatch,
                BEARER_TOKEN,
                request
            )
        );
    }
}
