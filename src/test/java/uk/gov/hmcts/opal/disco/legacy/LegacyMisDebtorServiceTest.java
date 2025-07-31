package uk.gov.hmcts.opal.disco.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMisDebtorSearchResults;
import uk.gov.hmcts.opal.dto.search.MisDebtorSearchDto;
import uk.gov.hmcts.opal.entity.MisDebtorEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyMisDebtorServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyMisDebtorService legacyMisDebtorService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyMisDebtorService = spy(new LegacyMisDebtorService(legacyGatewayProperties, restClient));
    }

    @Test
    void testGetMisDebtor() {
        long id = 1L;
        MisDebtorEntity expectedEntity = new MisDebtorEntity();
        doReturn(expectedEntity).when(legacyMisDebtorService).postToGateway(anyString(), any(), anyLong());

        MisDebtorEntity result = legacyMisDebtorService.getMisDebtor(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    void testSearchMisDebtors() {
        MisDebtorSearchDto criteria = MisDebtorSearchDto.builder().build();
        List<MisDebtorEntity> expectedEntities = Collections.singletonList(new MisDebtorEntity());
        LegacyMisDebtorSearchResults searchResults = LegacyMisDebtorSearchResults.builder().build();
        searchResults.setMisDebtorEntities(expectedEntities);
        doReturn(searchResults).when(legacyMisDebtorService).postToGateway(anyString(), any(), any());

        List<MisDebtorEntity> result = legacyMisDebtorService.searchMisDebtors(criteria);

        assertEquals(expectedEntities, result);
    }
}
