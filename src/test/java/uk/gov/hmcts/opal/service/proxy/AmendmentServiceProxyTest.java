package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.AmendmentSearchDto;
import uk.gov.hmcts.opal.entity.AmendmentEntity;
import uk.gov.hmcts.opal.service.AmendmentServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyAmendmentService;
import uk.gov.hmcts.opal.service.opal.AmendmentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AmendmentServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private AmendmentService opalService;

    @Mock
    private LegacyAmendmentService legacyService;

    @InjectMocks
    private AmendmentServiceProxy amendmentServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(AmendmentServiceInterface targetService, AmendmentServiceInterface otherService) {
        testGetAmendment(targetService, otherService);
        testSearchAmendments(targetService, otherService);
    }

    void testGetAmendment(AmendmentServiceInterface targetService, AmendmentServiceInterface otherService) {
        // Given: a AmendmentEntity is returned from the target service
        AmendmentEntity entity = AmendmentEntity.builder().build();
        when(targetService.getAmendment(anyLong())).thenReturn(entity);

        // When: getAmendment is called on the proxy
        AmendmentEntity amendmentResult = amendmentServiceProxy.getAmendment(1);

        // Then: target service should be used, and the returned amendment should be as expected
        verify(targetService).getAmendment(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, amendmentResult);
    }

    void testSearchAmendments(AmendmentServiceInterface targetService, AmendmentServiceInterface otherService) {
        // Given: a amendments list result is returned from the target service
        AmendmentEntity entity = AmendmentEntity.builder().build();
        List<AmendmentEntity> amendmentsList = List.of(entity);
        when(targetService.searchAmendments(any())).thenReturn(amendmentsList);

        // When: searchAmendments is called on the proxy
        AmendmentSearchDto criteria = AmendmentSearchDto.builder().build();
        List<AmendmentEntity> listResult = amendmentServiceProxy.searchAmendments(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchAmendments(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(amendmentsList, listResult);
    }

    @Test
    void shouldUseOpalAmendmentServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyAmendmentServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
