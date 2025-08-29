package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountService;
import uk.gov.hmcts.opal.service.opal.OpalDefendantAccountService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DefendantAccountServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private OpalDefendantAccountService opalService;

    @Mock
    private LegacyDefendantAccountService legacyService;

    @InjectMocks
    private DefendantAccountServiceProxy serviceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void shouldDelegateSearchToLegacyServiceWhenInLegacyMode() {

        setMode(LEGACY);
        AccountSearchDto dto = AccountSearchDto.builder().build();
        DefendantAccountSearchResultsDto expected = new DefendantAccountSearchResultsDto();

        when(legacyService.searchDefendantAccounts(dto)).thenReturn(expected);

        DefendantAccountSearchResultsDto result = serviceProxy.searchDefendantAccounts(dto);

        verify(legacyService).searchDefendantAccounts(dto);
        verifyNoInteractions(opalService);
        Assertions.assertEquals(expected, result);
    }

    @Test
    void shouldDelegateSearchToOpalServiceWhenInOpalMode() {

        setMode(OPAL);
        AccountSearchDto dto = AccountSearchDto.builder().build();
        DefendantAccountSearchResultsDto expected = new DefendantAccountSearchResultsDto();

        when(opalService.searchDefendantAccounts(dto)).thenReturn(expected);

        DefendantAccountSearchResultsDto result = serviceProxy.searchDefendantAccounts(dto);

        verify(opalService).searchDefendantAccounts(dto);
        verifyNoInteractions(legacyService);
        Assertions.assertEquals(expected, result);
    }

}
