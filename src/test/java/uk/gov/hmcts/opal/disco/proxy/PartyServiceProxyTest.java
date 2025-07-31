package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.PartyDto;
import uk.gov.hmcts.opal.dto.search.PartySearchDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PartySummary;
import uk.gov.hmcts.opal.disco.PartyServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyPartyService;
import uk.gov.hmcts.opal.disco.opal.PartyService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PartyServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private PartyService opalService;

    @Mock
    private LegacyPartyService legacyService;

    @InjectMocks
    private PartyServiceProxy partyServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(PartyServiceInterface targetService, PartyServiceInterface otherService) {
        testGetParty(targetService, otherService);
        testSearchParties(targetService, otherService);
        testSaveParty(targetService, otherService);
        testSearchForParty(targetService, otherService);
    }

    void testGetParty(PartyServiceInterface targetService, PartyServiceInterface otherService) {
        // Given: a PartyEntity is returned from the target service
        // PartyEntity entity = PartyEntity.builder().build();
        PartyDto partyDto = PartyDto.builder().build();
        when(targetService.getParty(anyLong())).thenReturn(partyDto);

        // When: getParty is called on the proxy
        PartyDto partyResult = partyServiceProxy.getParty(1);

        // Then: target service should be used, and the returned party should be as expected
        verify(targetService).getParty(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(partyDto, partyResult);
    }

    void testSearchParties(PartyServiceInterface targetService, PartyServiceInterface otherService) {
        // Given: a party list result is returned from the target service
        PartyEntity entity = PartyEntity.builder().build();
        List<PartyEntity> partysList = List.of(entity);
        when(targetService.searchParties(any())).thenReturn(partysList);

        // When: searchParties is called on the proxy
        PartySearchDto criteria = PartySearchDto.builder().build();
        List<PartyEntity> listResult = partyServiceProxy.searchParties(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchParties(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(partysList, listResult);
    }

    void testSaveParty(PartyServiceInterface targetService, PartyServiceInterface otherService) {
        // Given: a PartyDto is returned from the target service
        PartyDto partyDto = PartyDto.builder().build();
        when(targetService.saveParty(any(PartyDto.class))).thenReturn(partyDto);

        // When: saveParty is called on the proxy
        PartyDto partyResult = partyServiceProxy.saveParty(partyDto);

        // Then: target service should be used, and the returned party should be as expected
        verify(targetService).saveParty(partyDto);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(partyDto, partyResult);
    }

    void testSearchForParty(PartyServiceInterface targetService, PartyServiceInterface otherService) {
        // Given: a party list summary result is returned from the target service
        when(targetService.searchForParty(any())).thenReturn(Collections.emptyList());

        // When: searchForParty is called on the proxy
        PartySearchDto criteria = PartySearchDto.builder().build();
        List<PartySummary> listResult = partyServiceProxy.searchForParty(AccountSearchDto.builder().build());

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchForParty(any());
        verifyNoInteractions(otherService);
    }

    @Test
    void shouldUseOpalPartyServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyPartyServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
