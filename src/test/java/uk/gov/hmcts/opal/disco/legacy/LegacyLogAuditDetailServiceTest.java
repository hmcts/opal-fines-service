package uk.gov.hmcts.opal.disco.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyLogAuditDetailSearchResults;
import uk.gov.hmcts.opal.dto.search.LogAuditDetailSearchDto;
import uk.gov.hmcts.opal.entity.LogAuditDetailEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyLogAuditDetailServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyLogAuditDetailService legacyLogAuditDetailService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyLogAuditDetailService = spy(new LegacyLogAuditDetailService(legacyGatewayProperties, restClient));
    }

    @Test
    void testGetLogAuditDetail() {
        long id = 1L;
        LogAuditDetailEntity expectedEntity = new LogAuditDetailEntity();
        doReturn(expectedEntity).when(legacyLogAuditDetailService).postToGateway(anyString(), any(), anyLong());

        LogAuditDetailEntity result = legacyLogAuditDetailService.getLogAuditDetail(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    void testSearchLogAuditDetails() {
        LogAuditDetailSearchDto criteria = LogAuditDetailSearchDto.builder().build();
        List<LogAuditDetailEntity> expectedEntities = Collections.singletonList(new LogAuditDetailEntity());
        LegacyLogAuditDetailSearchResults searchResults = LegacyLogAuditDetailSearchResults.builder().build();
        searchResults.setLogAuditDetailEntities(expectedEntities);
        doReturn(searchResults).when(legacyLogAuditDetailService).postToGateway(anyString(), any(), any());

        List<LogAuditDetailEntity> result = legacyLogAuditDetailService.searchLogAuditDetails(criteria);

        assertEquals(expectedEntities, result);
    }
}
