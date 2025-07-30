package uk.gov.hmcts.opal.disco.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyCommittalWarrantProgressSearchResults;
import uk.gov.hmcts.opal.dto.search.CommittalWarrantProgressSearchDto;
import uk.gov.hmcts.opal.entity.CommittalWarrantProgressEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyCommittalWarrantProgressServiceTest extends LegacyTestsBase {


    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyCommittalWarrantProgressService legacyCommittalWarrantProgressService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyCommittalWarrantProgressService = spy(new LegacyCommittalWarrantProgressService(legacyGatewayProperties,
                                                                                              restClient));
    }

    @Test
    void testGetCommittalWarrantProgress() {
        long id = 1L;
        CommittalWarrantProgressEntity expectedEntity = new CommittalWarrantProgressEntity();
        doReturn(expectedEntity).when(legacyCommittalWarrantProgressService).postToGateway(anyString(),
                                                                                           any(), anyLong());

        CommittalWarrantProgressEntity result = legacyCommittalWarrantProgressService.getCommittalWarrantProgress(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    void testSearchCommittalWarrantProgresss() {
        CommittalWarrantProgressSearchDto criteria = CommittalWarrantProgressSearchDto.builder().build();
        List<CommittalWarrantProgressEntity> expectedEntities = Collections.singletonList(
            new CommittalWarrantProgressEntity());
        LegacyCommittalWarrantProgressSearchResults searchResults = LegacyCommittalWarrantProgressSearchResults
            .builder().build();
        searchResults.setCommittalWarrantProgressEntities(expectedEntities);
        doReturn(searchResults).when(legacyCommittalWarrantProgressService).postToGateway(anyString(), any(), any());

        List<CommittalWarrantProgressEntity> result = legacyCommittalWarrantProgressService
            .searchCommittalWarrantProgresss(criteria);

        assertEquals(expectedEntities, result);
    }

}
