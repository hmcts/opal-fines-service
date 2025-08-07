package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.service.iface.DraftAccountPublishInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyDraftAccountPublish;
import uk.gov.hmcts.opal.service.opal.DraftAccountPublish;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DraftAccountPublishProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private DraftAccountPublish draftAccountPublish;

    @Mock
    private LegacyDraftAccountPublish legacyDraftAccountPublish;

    @InjectMocks
    private DraftAccountPublishProxy draftAccountPublishProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(DraftAccountPublishInterface targetService, DraftAccountPublishInterface otherService) {
        testPublishDefendantAccount(targetService, otherService);
    }

    void testPublishDefendantAccount(DraftAccountPublishInterface targetService,
                                     DraftAccountPublishInterface otherService) {
        // Given: an Entity is returned from the target service
        DraftAccountEntity entity = DraftAccountEntity.builder().build();
        when(targetService.publishDefendantAccount(any(), any())).thenReturn(entity);

        DraftAccountEntity entityResult = draftAccountPublishProxy.publishDefendantAccount(null, null);

        // Then: target service should be used, and the returned entity should be as expected
        verify(targetService).publishDefendantAccount(any(), any());
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, entityResult);
    }

    @Test
    void shouldUseOpalServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(draftAccountPublish, legacyDraftAccountPublish);
    }

    @Test
    void shouldUseLegacyServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyDraftAccountPublish, draftAccountPublish);
    }
}
