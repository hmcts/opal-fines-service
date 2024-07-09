package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyAmendmentSearchResults;
import uk.gov.hmcts.opal.dto.search.AmendmentSearchDto;
import uk.gov.hmcts.opal.entity.AmendmentEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyAmendmentServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyAmendmentService legacyAmendmentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyAmendmentService = spy(new LegacyAmendmentService(legacyGatewayProperties, restClient));
    }

    @Test
    public void testGetAmendment() {
        long id = 1L;
        AmendmentEntity expectedEntity = new AmendmentEntity();
        doReturn(expectedEntity).when(legacyAmendmentService).postToGateway(anyString(), any(), anyLong());

        AmendmentEntity result = legacyAmendmentService.getAmendment(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    public void testSearchAmendments() {
        AmendmentSearchDto criteria = AmendmentSearchDto.builder().build();
        List<AmendmentEntity> expectedEntities = Collections.singletonList(new AmendmentEntity());
        LegacyAmendmentSearchResults searchResults = LegacyAmendmentSearchResults.builder().build();
        searchResults.setAmendmentEntities(expectedEntities);
        doReturn(searchResults).when(legacyAmendmentService).postToGateway(anyString(), any(), any());

        List<AmendmentEntity> result = legacyAmendmentService.searchAmendments(criteria);

        assertEquals(expectedEntities, result);
    }

}
