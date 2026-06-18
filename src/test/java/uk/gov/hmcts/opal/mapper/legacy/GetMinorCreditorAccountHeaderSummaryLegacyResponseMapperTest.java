package uk.gov.hmcts.opal.mapper.legacy;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse.CreditorHeader;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse.Financials;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyResponse.CreditorHeaderLegacy;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyResponse.FinancialsLegacy;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary;

@ExtendWith(MockitoExtension.class)
class GetMinorCreditorAccountHeaderSummaryLegacyResponseMapperTest {

    @InjectMocks
    private GetMinorCreditorAccountHeaderSummaryResponseLegacyMapperImpl mapper;

    @Mock
    private LegacyPartyDetailsMapper legacyPartyDetailsMapper;

    @Mock
    private FinancialsLegacyMapper financialsLegacyMapper;

    @Mock
    private BusinessUnitSummaryLegacyMapper businessUnitSummaryLegacyMapper;

    @Mock
    private CreditorHeaderLegacyMapper creditorHeaderLegacyMapper;

    @Test
    void givenFullLegacyResponse_MapsExpectedFieldsAndCallsSubmappers() {
        // Arrange
        PartyDetailsLegacy partyDetails = PartyDetailsLegacy.builder().build();
        BusinessUnitSummary businessUnitSummary = BusinessUnitSummary.builder().build();
        CreditorHeaderLegacy creditorHeader = CreditorHeaderLegacy.builder().build();
        FinancialsLegacy financials = FinancialsLegacy.builder().build();
        when(legacyPartyDetailsMapper.toOpal(partyDetails)).thenReturn(PartyDetails.builder().build());
        when(businessUnitSummaryLegacyMapper.toOpal(businessUnitSummary)).thenReturn(
            uk.gov.hmcts.opal.dto.common.BusinessUnitSummary.builder().build());
        when(creditorHeaderLegacyMapper.toOpal(creditorHeader)).thenReturn(CreditorHeader.builder().build());
        when(financialsLegacyMapper.toOpal(financials)).thenReturn(Financials.builder().build());

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
        verify(legacyPartyDetailsMapper).toOpal(partyDetails);
        verify(businessUnitSummaryLegacyMapper).toOpal(businessUnitSummary);
        verify(creditorHeaderLegacyMapper).toOpal(creditorHeader);
        verify(financialsLegacyMapper).toOpal(financials);
    }
}
