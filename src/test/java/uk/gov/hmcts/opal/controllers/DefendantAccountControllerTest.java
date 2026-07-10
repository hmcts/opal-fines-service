package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import tools.jackson.core.JacksonException;
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
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldRequest;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldResponse;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.request.RemoveDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.response.RemoveDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.service.DefendantAccountEnforcementService;
import uk.gov.hmcts.opal.service.DefendantAccountFixedPenaltyService;
import uk.gov.hmcts.opal.service.DefendantAccountPartyService;
import uk.gov.hmcts.opal.service.DefendantAccountPaymentTermsService;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountService;

@ExtendWith(MockitoExtension.class)
class DefendantAccountControllerTest {

    @Mock
    private DefendantAccountService defendantAccountService;

    @Mock
    private DefendantAccountEnforcementService defendantAccountEnforcementService;

    @Mock
    private DefendantAccountPartyService defendantAccountPartyService;

    @Mock
    private DefendantAccountFixedPenaltyService defendantAccountFixedPenaltyService;

    @Mock
    private DefendantAccountPaymentTermsService defendantAccountPaymentTermsService;

    @InjectMocks
    private DefendantAccountController defendantAccountController;

    @Test
    void testLegacyDefendantAccountService_createGetDefendantAccountRequest() {
        String id = "77";
        var req = LegacyDefendantAccountService.createGetDefendantAccountRequest(id);
        assertNotNull(req);
        assertEquals(id, req.getDefendantAccountId());
    }

    @Test
    void testAddEnforcement_Success() throws JacksonException {
        // Arrange
        Long defendantAccountId = 1L;
        Short businessUnitId = 10;
        String ifMatch = "1";
        AddDefendantAccountEnforcementRequest request = AddDefendantAccountEnforcementRequest.builder().build();
        AddEnforcementResponse mockResponse = AddEnforcementResponse.builder().build();

        when(defendantAccountEnforcementService.addEnforcement(
            defendantAccountId, businessUnitId, ifMatch, request)).thenReturn(mockResponse);

        // Act
        ResponseEntity<AddEnforcementResponse> response =
            defendantAccountController.addEnforcement(defendantAccountId, businessUnitId, ifMatch,
                request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());

        verify(defendantAccountEnforcementService).addEnforcement(defendantAccountId, businessUnitId, ifMatch, request);
    }

    @Test
    void testRemoveDefendantAccountParty_Success() {
        // Arrange
        Long defendantAccountId = 1L;
        Long defendantAccountPartyId = 10L;
        Short businessUnitId = 10;
        String ifMatch = "1";

        RemoveDefendantAccountPartyRequest request = new RemoveDefendantAccountPartyRequest();
        RemoveDefendantAccountPartyResponse mockResponse = new RemoveDefendantAccountPartyResponse();

        when(defendantAccountPartyService.removeDefendantAccountParty(defendantAccountId,
            defendantAccountPartyId, businessUnitId,
                ifMatch, request
        )).thenReturn(mockResponse);

        // Act
        ResponseEntity<RemoveDefendantAccountPartyResponse> response =
            defendantAccountController.removeDefendantAccountParty(defendantAccountId,
                defendantAccountPartyId, businessUnitId,
                ifMatch, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());

        verify(defendantAccountPartyService).removeDefendantAccountParty(defendantAccountId,
            defendantAccountPartyId, businessUnitId,
                ifMatch, request);
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
            request
        )).thenReturn(expectedResponse);

        ResponseEntity<RemoveDefendantAccountEnforcementHoldResponse> response =
            defendantAccountController.removeEnforcementHold(
                defendantAccountId,
                businessUnitId,
                ifMatch,
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
                request
            );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());

        verify(defendantAccountPartyService).addDefendantAccountParty(
            defendantAccountId,
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
            request
        )).thenThrow(new PermissionNotAllowedException(FinesPermission.ENTER_ENFORCEMENT));

        assertThrows(PermissionNotAllowedException.class, () ->
            defendantAccountController.removeEnforcementHold(
                defendantAccountId,
                businessUnitId,
                ifMatch,
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
            defendantAccountId,
            businessUnitId,
            null,
            request
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
                request
            )
        );
    }
}
