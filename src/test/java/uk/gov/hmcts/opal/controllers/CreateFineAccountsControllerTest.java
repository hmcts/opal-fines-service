package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.OpalS2SRequestWrapper;
import uk.gov.hmcts.opal.dto.OpalS2SResponseWrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;


class CreateFineAccountsControllerTest {

    private CreateFineAccountsController createFineAccountsController = new CreateFineAccountsController();

    @Test
    void testSearchDebtorProfile_Success() {
        // Arrange
        OpalS2SRequestWrapper request = new OpalS2SRequestWrapper();
        OpalS2SResponseWrapper response = OpalS2SResponseWrapper.builder()
            .opalResponsePayload("""
        <?xml version="1.0" encoding="UTF-8"?>
                                <CreateFineAccountsResponse
                                xmlns="http://www.justice.gov.uk/magistrates/atcm/CreateFineAccountsResponse"
                                xmlns:lg="http://www.justice.gov.uk/magistrates/atcm/CreatedFineAccount">
                                    <lg:CreatedFineAccount>
                                        <CPPID xmlns="">12345</CPPID>
                                        <CPPUUID xmlns="">12345</CPPUUID>
                                    </lg:CreatedFineAccount>
                                    <NumberOfFineAccounts xmlns="">1</NumberOfFineAccounts>
                                    <ErrorCode xmlns=""></ErrorCode>
                                    <ErrorMessage xmlns=""></ErrorMessage>
                                </CreateFineAccountsResponse>
            """)
            .build();

        ResponseEntity<OpalS2SResponseWrapper> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

        // Act
        ResponseEntity<OpalS2SResponseWrapper> actualResponse = createFineAccountsController
            .createFineAccounts(request);

        // Assert
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(response, actualResponse.getBody());
    }
}
