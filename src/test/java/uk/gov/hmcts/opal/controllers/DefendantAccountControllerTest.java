package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.service.DefendantAccountPartyService;
import uk.gov.hmcts.opal.service.DefendantAccountPaymentTermsService;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountService;

@ExtendWith(MockitoExtension.class)
class DefendantAccountControllerTest {

    @Mock
    private DefendantAccountService defendantAccountService;

    @Mock
    private DefendantAccountPartyService defendantAccountPartyService;

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

}
