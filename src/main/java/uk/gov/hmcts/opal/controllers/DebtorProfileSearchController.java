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
@RequestMapping("/api/debtor-profile")
@Slf4j(topic = "DebtorProfileSearchController")
@Tag(name = "Debtor Profile Search Controller")
public class DebtorProfileSearchController {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "cpp interface: Searches for debtor profile by criteia in request body xml.")
    public ResponseEntity<OpalS2SResponseWrapper> searchDebtorProfile(OpalS2SRequestWrapper debtorProfileRequest) {

        log.info(":POST:searchDebtorProfile: request: \n{}", debtorProfileRequest);
        OpalS2SResponseWrapper response = OpalS2SResponseWrapper.builder()
            .opalResponsePayload(responseXML)
            .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String responseXML = """
        <?xml version="1.0" encoding="UTF-8"?>
        <DebtorProfileResponse xmlns="http://www.justice.gov.uk/magistrates/atcm/DebtorProfileResponse"
                               xmlns:lgsr="http://www.justice.gov.uk/magistrates/atcm/SearchResponseType"
                               xmlns:lgsm="http://www.justice.gov.uk/magistrates/atcm/SearchMetadataType">
            <lgsr:SearchResponseType>
                <lgsm:SearchMetadataType>
                    <SequenceNumber>123</SequenceNumber>
                    <CPPDefendantID>DEF123</CPPDefendantID>
                    <CPPCaseID>CASE123</CPPCaseID>
                    <CourtOUCode>COURT01</CourtOUCode>
                    <DateOfHearing>2023-10-01</DateOfHearing>
                    <TimeOfHearing>10:00</TimeOfHearing>
                </lgsm:SearchMetadataType>
                <AccountMatch>
                    <Matches>
                        <Account>
                            <OrganisationName>Sample Org</OrganisationName>
                            <Forenames>John</Forenames>
                            <Surname>Doe</Surname>
                            <DOB>1980-01-01</DOB>
                            <NationalInsuranceNumber>AB123456C</NationalInsuranceNumber>
                            <AccountNumber>12345678</AccountNumber>
                            <AddressLine1>123 Sample Street</AddressLine1>
                            <AddressLine2>Sample Area</AddressLine2>
                            <AddressLine3>Sample City</AddressLine3>
                            <Postcode>AB12 3CD</Postcode>
                            <LastEnforcementAction>None</LastEnforcementAction>
                            <BalanceOutstanding>1000.00</BalanceOutstanding>
                            <CollectionOrderMade>Y</CollectionOrderMade>
                            <PaymentTerms>Monthly</PaymentTerms>
                            <AmountImposed>1500.00</AmountImposed>
                            <AmountPaid>500.00</AmountPaid>
                            <DaysInDefault>30</DaysInDefault>
                            <MasterAccount>N</MasterAccount>
                            <OriginatingCT>CT123</OriginatingCT>
                            <ParentGuardianFlag>N</ParentGuardianFlag>
                            <ProsecutorCaseReference>REF123</ProsecutorCaseReference>
                        </Account>
                    </Matches>
                </AccountMatch>
            </lgsr:SearchResponseType>
            <UnprocessedRequests>
                <lgsm:SearchMetadataType>
                    <SequenceNumber>456</SequenceNumber>
                    <CPPDefendantID>DEF456</CPPDefendantID>
                    <CPPCaseID>CASE456</CPPCaseID>
                    <CourtOUCode>COURT02</CourtOUCode>
                    <DateOfHearing>2023-11-01</DateOfHearing>
                    <TimeOfHearing>11:00</TimeOfHearing>
                </lgsm:SearchMetadataType>
            </UnprocessedRequests>
            <ErrorCode>12345</ErrorCode>
            <ErrorMessage>Sample error message</ErrorMessage>
        </DebtorProfileResponse>
        """;

}

