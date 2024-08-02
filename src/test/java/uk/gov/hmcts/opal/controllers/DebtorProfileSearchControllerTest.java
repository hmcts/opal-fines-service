package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.OpalS2SRequestWrapper;
import uk.gov.hmcts.opal.dto.OpalS2SResponseWrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;


class DebtorProfileSearchControllerTest {

    private DebtorProfileSearchController debtorProfileSearchController = new DebtorProfileSearchController();

    @Test
    void testSearchDebtorProfile_Success() {
        // Arrange
        OpalS2SRequestWrapper request = new OpalS2SRequestWrapper();
        OpalS2SResponseWrapper response = OpalS2SResponseWrapper.builder()
            .opalResponsePayload("""
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
                    """)
            .errorDetail("")
            .build();

        ResponseEntity<OpalS2SResponseWrapper> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

        // Act
        ResponseEntity<OpalS2SResponseWrapper> actualResponse = debtorProfileSearchController
            .searchDebtorProfile(request);

        // Assert
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(response, actualResponse.getBody());
    }
}
