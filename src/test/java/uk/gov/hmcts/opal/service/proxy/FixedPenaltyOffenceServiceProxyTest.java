package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.FixedPenaltyOffenceSearchDto;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.service.FixedPenaltyOffenceServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyFixedPenaltyOffenceService;
import uk.gov.hmcts.opal.service.opal.FixedPenaltyOffenceService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FixedPenaltyOffenceServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private FixedPenaltyOffenceService opalService;

    @Mock
    private LegacyFixedPenaltyOffenceService legacyService;

    @InjectMocks
    private FixedPenaltyOffenceServiceProxy fixedPenaltyOffenceServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(FixedPenaltyOffenceServiceInterface targetService, FixedPenaltyOffenceServiceInterface otherService) {
        testGetFixedPenaltyOffence(targetService, otherService);
        testSearchFixedPenaltyOffences(targetService, otherService);
    }

    void testGetFixedPenaltyOffence(FixedPenaltyOffenceServiceInterface targetService,
                                    FixedPenaltyOffenceServiceInterface otherService) {
        // Given: a FixedPenaltyOffenceEntity is returned from the target service
        FixedPenaltyOffenceEntity entity = FixedPenaltyOffenceEntity.builder().build();
        when(targetService.getFixedPenaltyOffence(anyLong())).thenReturn(entity);

        // When: getFixedPenaltyOffence is called on the proxy
        FixedPenaltyOffenceEntity fixedPenaltyOffenceResult = fixedPenaltyOffenceServiceProxy
            .getFixedPenaltyOffence(1);

        // Then: target service should be used, and the returned fixedPenaltyOffence should be as expected
        verify(targetService).getFixedPenaltyOffence(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, fixedPenaltyOffenceResult);
    }

    void testSearchFixedPenaltyOffences(FixedPenaltyOffenceServiceInterface targetService,
                                        FixedPenaltyOffenceServiceInterface otherService) {
        // Given: a fixedPenaltyOffences list result is returned from the target service
        FixedPenaltyOffenceEntity entity = FixedPenaltyOffenceEntity.builder().build();
        List<FixedPenaltyOffenceEntity> fixedPenaltyOffencesList = List.of(entity);
        when(targetService.searchFixedPenaltyOffences(any())).thenReturn(fixedPenaltyOffencesList);

        // When: searchFixedPenaltyOffences is called on the proxy
        FixedPenaltyOffenceSearchDto criteria = FixedPenaltyOffenceSearchDto.builder().build();
        List<FixedPenaltyOffenceEntity> listResult = fixedPenaltyOffenceServiceProxy
            .searchFixedPenaltyOffences(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchFixedPenaltyOffences(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(fixedPenaltyOffencesList, listResult);
    }

    @Test
    void shouldUseOpalFixedPenaltyOffenceServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyFixedPenaltyOffenceServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
