package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyDefendantTransactionSearchResults;
import uk.gov.hmcts.opal.dto.search.DefendantTransactionSearchDto;
import uk.gov.hmcts.opal.entity.DefendantTransactionEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyDefendantTransactionServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyDefendantTransactionService legacyDefendantTransactionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyDefendantTransactionService = spy(new LegacyDefendantTransactionService(legacyGatewayProperties,
                                                                                      restClient));
    }

    @Test
    public void testGetDefendantTransaction() {
        long id = 1L;
        DefendantTransactionEntity expectedEntity = new DefendantTransactionEntity();
        doReturn(expectedEntity).when(legacyDefendantTransactionService).postToGateway(anyString(), any(), anyLong());

        DefendantTransactionEntity result = legacyDefendantTransactionService.getDefendantTransaction(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    public void testSearchDefendantTransactions() {
        DefendantTransactionSearchDto criteria = DefendantTransactionSearchDto.builder().build();
        List<DefendantTransactionEntity> expectedEntities = Collections.singletonList(new DefendantTransactionEntity());
        LegacyDefendantTransactionSearchResults searchResults = LegacyDefendantTransactionSearchResults.builder()
            .build();
        searchResults.setDefendantTransactionEntities(expectedEntities);
        doReturn(searchResults).when(legacyDefendantTransactionService).postToGateway(anyString(), any(), any());

        List<DefendantTransactionEntity> result = legacyDefendantTransactionService
            .searchDefendantTransactions(criteria);

        assertEquals(expectedEntities, result);
    }
}
