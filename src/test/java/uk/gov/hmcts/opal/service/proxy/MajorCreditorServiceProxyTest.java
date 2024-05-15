package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.MajorCreditorSearchDto;
import uk.gov.hmcts.opal.entity.MajorCreditorEntity;
import uk.gov.hmcts.opal.service.MajorCreditorServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyMajorCreditorService;
import uk.gov.hmcts.opal.service.opal.MajorCreditorService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MajorCreditorServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private MajorCreditorService opalService;

    @Mock
    private LegacyMajorCreditorService legacyService;

    @InjectMocks
    private MajorCreditorServiceProxy majorCreditorServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(MajorCreditorServiceInterface targetService, MajorCreditorServiceInterface otherService) {
        testGetMajorCreditor(targetService, otherService);
        testSearchMajorCreditors(targetService, otherService);
    }

    void testGetMajorCreditor(MajorCreditorServiceInterface targetService, MajorCreditorServiceInterface otherService) {
        // Given: a MajorCreditorEntity is returned from the target service
        MajorCreditorEntity entity = MajorCreditorEntity.builder().build();
        when(targetService.getMajorCreditor(anyLong())).thenReturn(entity);

        // When: getMajorCreditor is called on the proxy
        MajorCreditorEntity majorCreditorResult = majorCreditorServiceProxy.getMajorCreditor(1);

        // Then: target service should be used, and the returned majorCreditor should be as expected
        verify(targetService).getMajorCreditor(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, majorCreditorResult);
    }

    void testSearchMajorCreditors(MajorCreditorServiceInterface targetService,
                                  MajorCreditorServiceInterface otherService) {
        // Given: a majorCreditors list result is returned from the target service
        MajorCreditorEntity entity = MajorCreditorEntity.builder().build();
        List<MajorCreditorEntity> majorCreditorsList = List.of(entity);
        when(targetService.searchMajorCreditors(any())).thenReturn(majorCreditorsList);

        // When: searchMajorCreditors is called on the proxy
        MajorCreditorSearchDto criteria = MajorCreditorSearchDto.builder().build();
        List<MajorCreditorEntity> listResult = majorCreditorServiceProxy.searchMajorCreditors(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchMajorCreditors(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(majorCreditorsList, listResult);
    }

    @Test
    void shouldUseOpalMajorCreditorServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyMajorCreditorServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
