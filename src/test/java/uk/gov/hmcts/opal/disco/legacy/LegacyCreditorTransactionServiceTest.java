package uk.gov.hmcts.opal.disco.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyCreditorTransactionSearchResults;
import uk.gov.hmcts.opal.dto.search.CreditorTransactionSearchDto;
import uk.gov.hmcts.opal.entity.CreditorTransactionEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyCreditorTransactionServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyCreditorTransactionService legacyCreditorTransactionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyCreditorTransactionService = spy(new LegacyCreditorTransactionService(legacyGatewayProperties,
                                                                                    restClient));
    }

    @Test
    void testGetCreditorTransaction() {
        long id = 1L;
        CreditorTransactionEntity expectedEntity = new CreditorTransactionEntity();
        doReturn(expectedEntity).when(legacyCreditorTransactionService).postToGateway(anyString(), any(), anyLong());

        CreditorTransactionEntity result = legacyCreditorTransactionService.getCreditorTransaction(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    void testSearchCreditorTransactions() {
        CreditorTransactionSearchDto criteria = CreditorTransactionSearchDto.builder().build();
        List<CreditorTransactionEntity> expectedEntities = Collections.singletonList(new CreditorTransactionEntity());
        LegacyCreditorTransactionSearchResults searchResults = LegacyCreditorTransactionSearchResults.builder().build();
        searchResults.setCreditorTransactionEntities(expectedEntities);
        doReturn(searchResults).when(legacyCreditorTransactionService).postToGateway(anyString(), any(), any());

        List<CreditorTransactionEntity> result = legacyCreditorTransactionService.searchCreditorTransactions(criteria);

        assertEquals(expectedEntities, result);
    }
}
