package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyAccountTransferSearchResults;
import uk.gov.hmcts.opal.dto.search.AccountTransferSearchDto;
import uk.gov.hmcts.opal.entity.AccountTransferEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class LegacyAccountTransferServiceTest {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyAccountTransferService legacyAccountTransferService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyAccountTransferService = spy(new LegacyAccountTransferService(legacyGatewayProperties, restClient));
    }

    @Test
    public void testGetAccountTransfer() {
        long id = 1L;
        AccountTransferEntity expectedEntity = new AccountTransferEntity();
        doReturn(expectedEntity).when(legacyAccountTransferService).postToGateway(anyString(), any(), anyLong());

        AccountTransferEntity result = legacyAccountTransferService.getAccountTransfer(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    public void testSearchAccountTransfers() {
        AccountTransferSearchDto criteria = AccountTransferSearchDto.builder().build();
        List<AccountTransferEntity> expectedEntities = Collections.singletonList(new AccountTransferEntity());
        LegacyAccountTransferSearchResults searchResults = LegacyAccountTransferSearchResults.builder().build();
        searchResults.setAccountTransferEntities(expectedEntities);
        doReturn(searchResults).when(legacyAccountTransferService).postToGateway(anyString(), any(), any());

        List<AccountTransferEntity> result = legacyAccountTransferService.searchAccountTransfers(criteria);

        assertEquals(expectedEntities, result);
    }
}
