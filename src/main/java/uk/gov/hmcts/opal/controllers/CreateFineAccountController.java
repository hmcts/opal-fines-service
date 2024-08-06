package uk.gov.hmcts.opal.controllers;

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
public class CreateFineAccountController {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OpalS2SResponseWrapper> searchDebtorProfile(OpalS2SRequestWrapper createFineAccountsRequest) {

        log.info(":POST:searchDebtorProfile: request: \n{}", createFineAccountsRequest);
        OpalS2SResponseWrapper response = OpalS2SResponseWrapper.builder()
            .opalResponsePayload(responseXML)
            .errorDetail("")
            .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String responseXML = """
        <?xml version="1.0" encoding="UTF-8"?>
                                <CreateFineAccountsResponse
                                xmlns="http://www.justice.gov.uk/magistrates/atcm/CreateFineAccountsResponse"
                                xmlns:lg="http://www.justice.gov.uk/magistrates/atcm/CreatedFineAccount">
                                    <lg:CreatedFineAccount>
                                        <AccountID>12345</AccountID>
                                        <FineAmount>100.00</FineAmount>
                                        <DueDate>2024-09-01</DueDate>
                                    </lg:CreatedFineAccount>
                                    <NumberOfFineAccounts>1</NumberOfFineAccounts>
                                    <ErrorCode></ErrorCode>
                                    <ErrorMessage></ErrorMessage>
                                </CreateFineAccountsResponse>
        """;

}

