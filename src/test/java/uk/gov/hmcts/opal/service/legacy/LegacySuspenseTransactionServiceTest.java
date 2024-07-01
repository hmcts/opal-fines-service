package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacySuspenseTransactionSearchResults;
import uk.gov.hmcts.opal.dto.search.SuspenseTransactionSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseTransactionEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacySuspenseTransactionServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacySuspenseTransactionService legacySuspenseTransactionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacySuspenseTransactionService = spy(new LegacySuspenseTransactionService(legacyGatewayProperties,
                                                                                    restClient));
    }

    @Test
    public void testGetSuspenseTransaction() {
        long id = 1L;
        SuspenseTransactionEntity expectedEntity = new SuspenseTransactionEntity();
        doReturn(expectedEntity).when(legacySuspenseTransactionService).postToGateway(anyString(), any(), anyLong());

        SuspenseTransactionEntity result = legacySuspenseTransactionService.getSuspenseTransaction(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    public void testSearchSuspenseTransactions() {
        SuspenseTransactionSearchDto criteria = SuspenseTransactionSearchDto.builder().build();
        List<SuspenseTransactionEntity> expectedEntities = Collections.singletonList(new SuspenseTransactionEntity());
        LegacySuspenseTransactionSearchResults searchResults = LegacySuspenseTransactionSearchResults.builder().build();
        searchResults.setSuspenseTransactionEntities(expectedEntities);
        doReturn(searchResults).when(legacySuspenseTransactionService).postToGateway(anyString(), any(), any());

        List<SuspenseTransactionEntity> result = legacySuspenseTransactionService.searchSuspenseTransactions(criteria);

        assertEquals(expectedEntities, result);
    }
}
