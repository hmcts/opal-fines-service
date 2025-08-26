package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.CommittalWarrantProgressSearchDto;
import uk.gov.hmcts.opal.entity.CommittalWarrantProgressEntity;
import uk.gov.hmcts.opal.disco.CommittalWarrantProgressServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyCommittalWarrantProgressService;
import uk.gov.hmcts.opal.disco.opal.CommittalWarrantProgressService;
import uk.gov.hmcts.opal.service.proxy.ProxyTestsBase;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CommittalWarrantProgressServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private CommittalWarrantProgressService opalService;

    @Mock
    private LegacyCommittalWarrantProgressService legacyService;

    @InjectMocks
    private CommittalWarrantProgressServiceProxy committalWarrantProgressServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(CommittalWarrantProgressServiceInterface targetService,
                  CommittalWarrantProgressServiceInterface otherService) {
        testGetCommittalWarrantProgress(targetService, otherService);
        testSearchCommittalWarrantProgresss(targetService, otherService);
    }

    void testGetCommittalWarrantProgress(CommittalWarrantProgressServiceInterface targetService,
                                         CommittalWarrantProgressServiceInterface otherService) {
        // Given: a CommittalWarrantProgressEntity is returned from the target service
        CommittalWarrantProgressEntity entity = CommittalWarrantProgressEntity.builder().build();
        when(targetService.getCommittalWarrantProgress(anyLong())).thenReturn(entity);

        // When: getCommittalWarrantProgress is called on the proxy
        CommittalWarrantProgressEntity committalWarrantProgressResult = committalWarrantProgressServiceProxy
            .getCommittalWarrantProgress(1);

        // Then: target service should be used, and the returned committalWarrantProgress should be as expected
        verify(targetService).getCommittalWarrantProgress(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, committalWarrantProgressResult);
    }

    void testSearchCommittalWarrantProgresss(CommittalWarrantProgressServiceInterface targetService,
                                             CommittalWarrantProgressServiceInterface otherService) {
        // Given: a committalWarrantProgresss list result is returned from the target service
        CommittalWarrantProgressEntity entity = CommittalWarrantProgressEntity.builder().build();
        List<CommittalWarrantProgressEntity> committalWarrantProgresssList = List.of(entity);
        when(targetService.searchCommittalWarrantProgresss(any())).thenReturn(committalWarrantProgresssList);

        // When: searchCommittalWarrantProgresss is called on the proxy
        CommittalWarrantProgressSearchDto criteria = CommittalWarrantProgressSearchDto.builder().build();
        List<CommittalWarrantProgressEntity> listResult = committalWarrantProgressServiceProxy
            .searchCommittalWarrantProgresss(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchCommittalWarrantProgresss(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(committalWarrantProgresssList, listResult);
    }

    @Test
    void shouldUseOpalCommittalWarrantProgressServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyCommittalWarrantProgressServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
