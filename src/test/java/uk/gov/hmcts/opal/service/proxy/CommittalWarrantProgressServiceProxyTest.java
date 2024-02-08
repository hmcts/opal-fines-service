package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.dto.search.CommittalWarrantProgressSearchDto;
import uk.gov.hmcts.opal.entity.CommittalWarrantProgressEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyCommittalWarrantProgressService;
import uk.gov.hmcts.opal.service.opal.CommittalWarrantProgressService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CommittalWarrantProgressServiceProxyTest {

    private AutoCloseable closeable;

    @Mock
    private CommittalWarrantProgressService opalCommittalWarrantProgressService;

    @Mock
    private LegacyCommittalWarrantProgressService legacyCommittalWarrantProgressService;

    @Mock
    private DynamicConfigService dynamicConfigService;

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

    @Test
    void shouldUseOpalCommittalWarrantProgressServiceWhenModeIsNotLegacy() {
        // Given: a CommittalWarrantProgressEntity and the app mode is set to "opal"
        CommittalWarrantProgressEntity entity = CommittalWarrantProgressEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("opal").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(opalCommittalWarrantProgressService.getCommittalWarrantProgress(anyLong())).thenReturn(entity);

        // When: saveCommittalWarrantProgress is called on the proxy
        CommittalWarrantProgressEntity committalWarrantProgressResult = committalWarrantProgressServiceProxy
            .getCommittalWarrantProgress(1);

        // Then: opalCommittalWarrantProgressService should be used
        verify(opalCommittalWarrantProgressService).getCommittalWarrantProgress(1);
        verifyNoInteractions(legacyCommittalWarrantProgressService);
        Assertions.assertEquals(entity, committalWarrantProgressResult);

        // Given: a committalWarrantProgresss list result and the app mode is set to "opal"
        List<CommittalWarrantProgressEntity> committalWarrantProgresssList = List.of(entity);
        when(opalCommittalWarrantProgressService.searchCommittalWarrantProgresss(any()))
            .thenReturn(committalWarrantProgresssList);

        // When: searchCommittalWarrantProgresss is called on the proxy
        CommittalWarrantProgressSearchDto criteria = CommittalWarrantProgressSearchDto.builder().build();
        List<CommittalWarrantProgressEntity> listResult = committalWarrantProgressServiceProxy
            .searchCommittalWarrantProgresss(criteria);

        // Then: opalCommittalWarrantProgressService should be used, and the returned list should be as expected
        verify(opalCommittalWarrantProgressService).searchCommittalWarrantProgresss(criteria);
        verifyNoInteractions(legacyCommittalWarrantProgressService);
        Assertions.assertEquals(committalWarrantProgresssList, listResult);
    }

    @Test
    void shouldUseLegacyCommittalWarrantProgressServiceWhenModeIsLegacy() {
        // Given: a CommittalWarrantProgressEntity and the app mode is set to "legacy"
        CommittalWarrantProgressEntity entity = CommittalWarrantProgressEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("legacy").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(legacyCommittalWarrantProgressService.getCommittalWarrantProgress(anyLong())).thenReturn(entity);

        // When: saveCommittalWarrantProgress is called on the proxy
        CommittalWarrantProgressEntity result = committalWarrantProgressServiceProxy
            .getCommittalWarrantProgress(1);

        // Then: legacyCommittalWarrantProgressService should be used
        verify(legacyCommittalWarrantProgressService).getCommittalWarrantProgress(1);
        verifyNoInteractions(opalCommittalWarrantProgressService);
        Assertions.assertEquals(entity, result);

        // Given: a committalWarrantProgresss list result and the app mode is set to "legacy"
        List<CommittalWarrantProgressEntity> committalWarrantProgresssList = List.of(entity);
        when(legacyCommittalWarrantProgressService.searchCommittalWarrantProgresss(any()))
            .thenReturn(committalWarrantProgresssList);

        // When: searchCommittalWarrantProgresss is called on the proxy
        CommittalWarrantProgressSearchDto criteria = CommittalWarrantProgressSearchDto.builder().build();
        List<CommittalWarrantProgressEntity> listResult = committalWarrantProgressServiceProxy
            .searchCommittalWarrantProgresss(criteria);

        // Then: opalCommittalWarrantProgressService should be used, and the returned list should be as expected
        verify(legacyCommittalWarrantProgressService).searchCommittalWarrantProgresss(criteria);
        verifyNoInteractions(opalCommittalWarrantProgressService);
        Assertions.assertEquals(committalWarrantProgresssList, listResult); // Not yet implemented in Legacy mode
    }
}
