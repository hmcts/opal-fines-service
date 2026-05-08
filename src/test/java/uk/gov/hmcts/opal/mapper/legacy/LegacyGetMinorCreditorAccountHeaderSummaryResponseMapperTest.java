package uk.gov.hmcts.opal.mapper.legacy;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse.CreditorHeader;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse.Financials;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHeaderSummaryResponse.CreditorHeaderLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHeaderSummaryResponse.FinancialsLegacy;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.mapper.AbstractMapperTest;

public class LegacyGetMinorCreditorAccountHeaderSummaryResponseMapperTest extends AbstractMapperTest {

    @Autowired
    LegacyGetMinorCreditorAccountHeaderSummaryResponseMapper mapper;

    @MockitoBean
    LegacyPartyDetailsMapper partyDetailsMapper;

    @MockitoBean
    FinancialsLegacyMapper financialsMapper;

    @MockitoBean
    LegacyBusinessUnitSummaryMapper businessUnitSummaryMapper;

    @MockitoBean
    CreditorHeaderLegacyMapper creditorHeaderMapper;

    @Test
    void givenFullLegacyResponse_MapsExpectedFieldsAndCallsSubmappers() {
        // Arrange
        PartyDetailsLegacy partyDetails = PartyDetailsLegacy.builder().build();
        BusinessUnitSummary businessUnitSummary = BusinessUnitSummary.builder().build();
        CreditorHeaderLegacy creditorHeader = CreditorHeaderLegacy.builder().build();
        FinancialsLegacy financials = FinancialsLegacy.builder().build();

        LegacyGetMinorCreditorAccountHeaderSummaryResponse legacy = LegacyGetMinorCreditorAccountHeaderSummaryResponse
            .builder()
            .partyDetails(partyDetails)
            .businessUnit(businessUnitSummary)
            .creditor(creditorHeader)
            .financials(financials)
            .build();

        when(partyDetailsMapper.toOpal(partyDetails)).thenReturn(PartyDetails.builder().build());
        when(businessUnitSummaryMapper.toOpal(businessUnitSummary)).thenReturn(
            uk.gov.hmcts.opal.dto.common.BusinessUnitSummary.builder().build());
        when(creditorHeaderMapper.toOpal(creditorHeader)).thenReturn(CreditorHeader.builder().build());
        when(financialsMapper.toOpal(financials)).thenReturn(Financials.builder().build());

        // Act
        mapper.toOpal(legacy);

        // Assert
        verify(partyDetailsMapper).toOpal(partyDetails);
        verify(businessUnitSummaryMapper).toOpal(businessUnitSummary);
        verify(creditorHeaderMapper).toOpal(creditorHeader);
        verify(financialsMapper).toOpal(financials);
    }
}
