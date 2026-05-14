package uk.gov.hmcts.opal.mapper.legacy;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse.CreditorHeader;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse.Financials;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyResponse.CreditorHeaderLegacy;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyResponse.FinancialsLegacy;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.mapper.AbstractMapperTest;

public class GetMinorCreditorAccountHeaderSummaryLegacyResponseMapperTest extends AbstractMapperTest {

    @Autowired
    GetMinorCreditorAccountHeaderSummaryResponseLegacyMapper mapper;

    @MockitoBean
    LegacyPartyDetailsMapper partyDetailsMapper;

    @MockitoBean
    FinancialsLegacyMapper financialsMapper;

    @MockitoBean
    BusinessUnitSummaryLegacyMapper businessUnitSummaryMapper;

    @MockitoBean
    CreditorHeaderLegacyMapper creditorHeaderMapper;

    @Test
    void givenFullLegacyResponse_MapsExpectedFieldsAndCallsSubmappers() {
        // Arrange
        PartyDetailsLegacy partyDetails = PartyDetailsLegacy.builder().build();
        BusinessUnitSummary businessUnitSummary = BusinessUnitSummary.builder().build();
        CreditorHeaderLegacy creditorHeader = CreditorHeaderLegacy.builder().build();
        FinancialsLegacy financials = FinancialsLegacy.builder().build();



        when(partyDetailsMapper.toOpal(partyDetails)).thenReturn(PartyDetails.builder().build());
        when(businessUnitSummaryMapper.toOpal(businessUnitSummary)).thenReturn(
            uk.gov.hmcts.opal.dto.common.BusinessUnitSummary.builder().build());
        when(creditorHeaderMapper.toOpal(creditorHeader)).thenReturn(CreditorHeader.builder().build());
        when(financialsMapper.toOpal(financials)).thenReturn(Financials.builder().build());

        GetMinorCreditorAccountHeaderSummaryLegacyResponse legacy = GetMinorCreditorAccountHeaderSummaryLegacyResponse
            .builder()
            .partyDetails(partyDetails)
            .businessUnit(businessUnitSummary)
            .creditor(creditorHeader)
            .financials(financials)
            .build();

        // Act
        mapper.toOpal(legacy);

        // Assert
        verify(partyDetailsMapper).toOpal(partyDetails);
        verify(businessUnitSummaryMapper).toOpal(businessUnitSummary);
        verify(creditorHeaderMapper).toOpal(creditorHeader);
        verify(financialsMapper).toOpal(financials);
    }
}
