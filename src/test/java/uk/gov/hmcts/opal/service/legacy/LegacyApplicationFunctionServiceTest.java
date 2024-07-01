package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyApplicationFunctionSearchResults;
import uk.gov.hmcts.opal.dto.search.ApplicationFunctionSearchDto;
import uk.gov.hmcts.opal.entity.ApplicationFunctionEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyApplicationFunctionServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyApplicationFunctionService legacyApplicationFunctionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyApplicationFunctionService = spy(new LegacyApplicationFunctionService(legacyGatewayProperties,
                                                                                    restClient));
    }

    @Test
    public void testGetApplicationFunction() {
        long id = 1L;
        ApplicationFunctionEntity expectedEntity = new ApplicationFunctionEntity();
        doReturn(expectedEntity).when(legacyApplicationFunctionService).postToGateway(anyString(), any(), anyLong());

        ApplicationFunctionEntity result = legacyApplicationFunctionService.getApplicationFunction(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    public void testSearchApplicationFunctions() {
        ApplicationFunctionSearchDto criteria = ApplicationFunctionSearchDto.builder().build();
        List<ApplicationFunctionEntity> expectedEntities = Collections.singletonList(new ApplicationFunctionEntity());
        LegacyApplicationFunctionSearchResults searchResults = LegacyApplicationFunctionSearchResults.builder().build();
        searchResults.setApplicationFunctionEntities(expectedEntities);
        doReturn(searchResults).when(legacyApplicationFunctionService).postToGateway(anyString(), any(), any());

        List<ApplicationFunctionEntity> result = legacyApplicationFunctionService.searchApplicationFunctions(criteria);

        assertEquals(expectedEntities, result);
    }

}
