package uk.gov.hmcts.opal.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.dto.PartyDto;
import uk.gov.hmcts.opal.entity.PartySummary;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PartyServiceProxyTest {

    private AutoCloseable closeable;

    @Mock
    private PartyService opalPartyService;

    @Mock
    private LegacyPartyService legacyPartyService;

    @Mock
    private DynamicConfigService dynamicConfigService;

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

    @Test
    void shouldUseOpalPartyServiceWhenModeIsNotLegacy() {
        // Given: a PartyDto and the app mode is set to "opal"
        PartyDto partyDto = new PartyDto();
        AppMode appMode = AppMode.builder().mode("opal").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(opalPartyService.saveParty(partyDto)).thenReturn(partyDto);
        when(opalPartyService.getParty(1L)).thenReturn(partyDto);
        when(opalPartyService.searchForParty(any())).thenReturn(Collections.emptyList());

        // When: saveParty is called on the proxy
        PartyDto result1 = partyServiceProxy.saveParty(partyDto);

        // Then: opalPartyService should be used, and the returned party should be as expected
        verify(opalPartyService).saveParty(partyDto);
        verifyNoInteractions(legacyPartyService);
        Assertions.assertEquals(partyDto, result1);

        // When: getParty is called on the proxy
        PartyDto result2 = partyServiceProxy.getParty(1L);

        // Then: opalPartyService should be used, and the returned party should be as expected
        verify(opalPartyService).getParty(1L);
        verifyNoInteractions(legacyPartyService);
        Assertions.assertEquals(partyDto, result2);

        // When
        List<PartySummary> result3 = partyServiceProxy.searchForParty(AccountSearchDto.builder().build());

        // Then
        verify(opalPartyService).searchForParty(any());
        verifyNoInteractions(legacyPartyService);
    }

    @Test
    void shouldUseLegacyPartyServiceWhenModeIsLegacy() {
        // Given: a PartyDto and the app mode is set to "legacy"
        PartyDto partyDto = new PartyDto();
        AppMode appMode = AppMode.builder().mode("legacy").build();

        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(legacyPartyService.saveParty(partyDto)).thenReturn(partyDto);
        when(legacyPartyService.getParty(1L)).thenReturn(partyDto);
        when(legacyPartyService.searchForParty(any())).thenReturn(Collections.emptyList());

        // When: saveParty is called on the proxy
        PartyDto result1 = partyServiceProxy.saveParty(partyDto);

        // Then: legacyPartyService should be used, and the returned party should be as expected
        verify(legacyPartyService).saveParty(partyDto);
        verifyNoInteractions(opalPartyService);
        Assertions.assertEquals(partyDto, result1);

        // When: getParty is called on the proxy
        PartyDto result2 = partyServiceProxy.getParty(1L);

        // Then: opalPartyService should be used, and the returned party should be as expected
        verify(legacyPartyService).getParty(1L);
        verifyNoInteractions(opalPartyService);
        Assertions.assertEquals(partyDto, result2);


        // When
        List<PartySummary> result3 = partyServiceProxy.searchForParty(AccountSearchDto.builder().build());

        // Then
        verify(legacyPartyService).searchForParty(any());
        verifyNoInteractions(opalPartyService);
    }
}
