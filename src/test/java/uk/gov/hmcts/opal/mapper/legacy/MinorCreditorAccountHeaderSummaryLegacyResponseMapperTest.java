package uk.gov.hmcts.opal.mapper.legacy;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyResponse.CreditorHeaderLegacy;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyResponse.FinancialsLegacy;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountHeaderSummaryResponseCreditor;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountHeaderSummaryResponseFinancials;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;

@ExtendWith(MockitoExtension.class)
class MinorCreditorAccountHeaderSummaryLegacyResponseMapperTest {

    @InjectMocks
    private MinorCreditorAccountHeaderSummaryResponseLegacyMapperImpl mapper;

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
        when(legacyPartyDetailsMapper.toPartyDetailsCommon(partyDetails)).thenReturn(new PartyDetailsCommon());
        when(businessUnitSummaryLegacyMapper.toBusinessUnitSummaryCommon(businessUnitSummary)).thenReturn(
            new BusinessUnitSummaryCommon());
        when(creditorHeaderLegacyMapper.toOpal(creditorHeader)).thenReturn(
            new MinorCreditorAccountHeaderSummaryResponseCreditor());
        when(financialsLegacyMapper.toOpal(financials)).thenReturn(
            new MinorCreditorAccountHeaderSummaryResponseFinancials());

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
        verify(legacyPartyDetailsMapper).toPartyDetailsCommon(partyDetails);
        verify(businessUnitSummaryLegacyMapper).toBusinessUnitSummaryCommon(businessUnitSummary);
        verify(creditorHeaderLegacyMapper).toOpal(creditorHeader);
        verify(financialsLegacyMapper).toOpal(financials);
    }
}
