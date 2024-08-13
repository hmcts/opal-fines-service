package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.OpalS2SRequestWrapper;
import uk.gov.hmcts.opal.dto.OpalS2SResponseWrapper;

@RestController
@RequestMapping("/api/create-fine-accounts")
@Slf4j(topic = "CreateFineAccountController")
@Tag(name = "Create Fine Account Controller")
public class CreateFineAccountsController {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "cpp interface: creates fine accounts by criteria in request body xml.")
    public ResponseEntity<OpalS2SResponseWrapper> createFineAccounts(OpalS2SRequestWrapper createFineAccountsRequest) {

        log.info(":POST:createFineAccounts: request: \n{}", createFineAccountsRequest);
        OpalS2SResponseWrapper response = OpalS2SResponseWrapper.builder()
            .opalResponsePayload(responseXML)
            .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String responseXML = """
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
        """;

}

