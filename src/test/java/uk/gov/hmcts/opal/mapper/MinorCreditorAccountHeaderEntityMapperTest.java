package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountHeaderEntity;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.mapper.common.BusinessUnitSummaryMapper;
import uk.gov.hmcts.opal.mapper.common.PartyMapper;
import uk.gov.hmcts.opal.service.persistence.DebtorDetailRepositoryService;
import uk.gov.hmcts.opal.service.persistence.EnforcementRepositoryService;

@Isolated
class MinorCreditorAccountHeaderEntityMapperTest extends AbstractMapperTest {

    @Autowired
    private MinorCreditorAccountHeaderEntityMapper mapper;

    @MockitoBean
    private PartyMapper partyMapper;

    @MockitoBean
    private BusinessUnitSummaryMapper businessUnitSummaryMapper;

    @MockitoBean
    private EnforcementRepositoryService enforcementService;

    @MockitoBean
    private DebtorDetailRepositoryService debtorService;


    @Test
    void givenFullEntity_whenToResponse_thenMapsExpectedFieldsAndCallsSubmappers() {
        // Arrange
        MinorCreditorAccountHeaderEntity entity = MinorCreditorAccountHeaderEntity.builder()
            .creditorAccountId(101L)
            .creditorAccountNumber("123")
            .creditorAccountType(CreditorAccountType.MN)
            .versionNumber(8L)
            .partyId(999L)
            .organisation(false)
            .businessUnitId((short) 12)
            .businessUnitName("BU Name")
            .welshLanguage(true)
            .awarded(new BigDecimal("10.00"))
            .paidOut(new BigDecimal("2.00"))
            .awaitingPayment(new BigDecimal("1.00"))
            .outstanding(BigDecimal.ZERO)
            .hasAssociatedDefendant(true)
            .build();

        PartyEntity party = PartyEntity.builder()
            .partyId(999L)
            .build();

        when(partyMapper.toPartyDetailsCommon(party)).thenReturn(new PartyDetailsCommon());
        when(businessUnitSummaryMapper.toBusinessUnitSummaryCommon(entity)).thenReturn(
            new BusinessUnitSummaryCommon());

        // Act
        MinorCreditorAccountHeaderSummaryResponse mapped = mapper.toResponse(entity, party);

        // Assert
        assertNotNull(mapped);
        assertEquals("101", mapped.getCreditor().getAccountId());
        assertEquals("123", mapped.getCreditor().getAccountNumber());
        assertEquals(BigInteger.valueOf(8L), mapped.getVersion());
        assertTrue(mapped.getCreditor().getHasAssociatedDefendant());
        assertEquals(new BigDecimal("10.00"), mapped.getFinancials().getAwarded());
        assertEquals(new BigDecimal("2.00"), mapped.getFinancials().getPaidOut());
        assertEquals(new BigDecimal("1.00"), mapped.getFinancials().getAwaitingPayout());
        assertEquals(BigDecimal.ZERO, mapped.getFinancials().getOutstanding());


        verify(partyMapper).toPartyDetailsCommon(party);
        verify(businessUnitSummaryMapper).toBusinessUnitSummaryCommon(entity);
    }
}
