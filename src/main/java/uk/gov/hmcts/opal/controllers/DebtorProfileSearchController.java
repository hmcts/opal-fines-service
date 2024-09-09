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
@RequestMapping("/s2s/debtor-profiles")
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
                    <SequenceNumber xmlns="">123</SequenceNumber>
                    <CPPDefendantID xmlns="">DEF123</CPPDefendantID>
                    <CPPCaseID xmlns="">CASE123</CPPCaseID>
                    <CourtOUCode xmlns="">COURT01</CourtOUCode>
                    <DateOfHearing xmlns="">2023-10-01</DateOfHearing>
                    <TimeOfHearing xmlns="">10:00</TimeOfHearing>
                </lgsm:SearchMetadataType>
                <AccountMatch xmlns="">
                    <Matches xmlns="">
                        <Account xmlns="">
                            <OrganisationName xmlns="">Sample Org</OrganisationName>
                            <Forenames xmlns="">John</Forenames>
                            <Surname xmlns="">Doe</Surname>
                            <DOB xmlns="">1980-01-01</DOB>
                            <NationalInsuranceNumber xmlns="">AB123456C</NationalInsuranceNumber>
                            <AccountNumber xmlns="">12345678</AccountNumber>
                            <AddressLine1 xmlns="">123 Sample Street</AddressLine1>
                            <AddressLine2 xmlns="">Sample Area</AddressLine2>
                            <AddressLine3 xmlns="">Sample City</AddressLine3>
                            <Postcode xmlns="">AB12 3CD</Postcode>
                            <LastEnforcementAction xmlns="">None</LastEnforcementAction>
                            <BalanceOutstanding xmlns="">1000.00</BalanceOutstanding>
                            <CollectionOrderMade xmlns="">Y</CollectionOrderMade>
                            <PaymentTerms xmlns="">Monthly</PaymentTerms>
                            <AmountImposed xmlns="">1500.00</AmountImposed>
                            <AmountPaid xmlns="">500.00</AmountPaid>
                            <DaysInDefault xmlns="">30</DaysInDefault>
                            <MasterAccount xmlns="">N</MasterAccount>
                            <OriginatingCT xmlns="">CT123</OriginatingCT>
                            <ParentGuardianFlag xmlns="">N</ParentGuardianFlag>
                            <ProsecutorCaseReference xmlns="">REF123</ProsecutorCaseReference>
                        </Account>
                    </Matches>
                </AccountMatch>
            </lgsr:SearchResponseType>
            <UnprocessedRequests xmlns="">
                <lgsm:SearchMetadataType>
                    <SequenceNumber xmlns="">456</SequenceNumber>
                    <CPPDefendantID xmlns="">DEF456</CPPDefendantID>
                    <CPPCaseID xmlns="">CASE456</CPPCaseID>
                    <CourtOUCode xmlns="">COURT02</CourtOUCode>
                    <DateOfHearing xmlns="">2023-11-01</DateOfHearing>
                    <TimeOfHearing xmlns="">11:00</TimeOfHearing>
                </lgsm:SearchMetadataType>
            </UnprocessedRequests>
            <ErrorCode xmlns="">12345</ErrorCode>
            <ErrorMessage xmlns="">Sample error message</ErrorMessage>
        </DebtorProfileResponse>
        """;

}
