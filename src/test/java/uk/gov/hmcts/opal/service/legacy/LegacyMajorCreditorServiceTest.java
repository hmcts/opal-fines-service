package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMajorCreditorSearchResults;
import uk.gov.hmcts.opal.dto.search.MajorCreditorSearchDto;
import uk.gov.hmcts.opal.entity.MajorCreditorEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyMajorCreditorServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyMajorCreditorService legacyMajorCreditorService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyMajorCreditorService = spy(new LegacyMajorCreditorService(legacyGatewayProperties, restClient));
    }

    @Test
    public void testGetMajorCreditor() {
        long id = 1L;
        MajorCreditorEntity expectedEntity = new MajorCreditorEntity();
        doReturn(expectedEntity).when(legacyMajorCreditorService).postToGateway(anyString(), any(), anyLong());

        MajorCreditorEntity result = legacyMajorCreditorService.getMajorCreditor(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    public void testSearchMajorCreditors() {
        MajorCreditorSearchDto criteria = MajorCreditorSearchDto.builder().build();
        List<MajorCreditorEntity> expectedEntities = Collections.singletonList(new MajorCreditorEntity());
        LegacyMajorCreditorSearchResults searchResults = LegacyMajorCreditorSearchResults.builder().build();
        searchResults.setMajorCreditorEntities(expectedEntities);
        doReturn(searchResults).when(legacyMajorCreditorService).postToGateway(anyString(), any(), any());

        List<MajorCreditorEntity> result = legacyMajorCreditorService.searchMajorCreditors(criteria);

        assertEquals(expectedEntities, result);
    }
}
