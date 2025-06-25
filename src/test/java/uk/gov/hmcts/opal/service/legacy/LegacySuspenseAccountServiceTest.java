package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacySuspenseAccountSearchResults;
import uk.gov.hmcts.opal.dto.search.SuspenseAccountSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseAccountEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacySuspenseAccountServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacySuspenseAccountService legacySuspenseAccountService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacySuspenseAccountService = spy(new LegacySuspenseAccountService(legacyGatewayProperties, restClient));
    }

    @Test
    void testGetSuspenseAccount() {
        long id = 1L;
        SuspenseAccountEntity expectedEntity = new SuspenseAccountEntity();
        doReturn(expectedEntity).when(legacySuspenseAccountService).postToGateway(anyString(), any(), anyLong());

        SuspenseAccountEntity result = legacySuspenseAccountService.getSuspenseAccount(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    void testSearchSuspenseAccounts() {
        SuspenseAccountSearchDto criteria = SuspenseAccountSearchDto.builder().build();
        List<SuspenseAccountEntity> expectedEntities = Collections.singletonList(new SuspenseAccountEntity());
        LegacySuspenseAccountSearchResults searchResults = LegacySuspenseAccountSearchResults.builder().build();
        searchResults.setSuspenseAccountEntities(expectedEntities);
        doReturn(searchResults).when(legacySuspenseAccountService).postToGateway(anyString(), any(), any());

        List<SuspenseAccountEntity> result = legacySuspenseAccountService.searchSuspenseAccounts(criteria);

        assertEquals(expectedEntities, result);
    }
}
