package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.StandardLetterSearchDto;
import uk.gov.hmcts.opal.entity.StandardLetterEntity;
import uk.gov.hmcts.opal.disco.StandardLetterServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyStandardLetterService;
import uk.gov.hmcts.opal.disco.opal.StandardLetterService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class StandardLetterServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private StandardLetterService opalService;

    @Mock
    private LegacyStandardLetterService legacyService;

    @InjectMocks
    private StandardLetterServiceProxy standardLetterServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(StandardLetterServiceInterface targetService, StandardLetterServiceInterface otherService) {
        testGetStandardLetter(targetService, otherService);
        testSearchStandardLetters(targetService, otherService);
    }

    void testGetStandardLetter(StandardLetterServiceInterface targetService,
                               StandardLetterServiceInterface otherService) {
        // Given: a StandardLetterEntity is returned from the target service
        StandardLetterEntity entity = StandardLetterEntity.builder().build();
        when(targetService.getStandardLetter(anyLong())).thenReturn(entity);

        // When: getStandardLetter is called on the proxy
        StandardLetterEntity standardLetterResult = standardLetterServiceProxy.getStandardLetter(1);

        // Then: target service should be used, and the returned standardLetter should be as expected
        verify(targetService).getStandardLetter(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, standardLetterResult);
    }

    void testSearchStandardLetters(StandardLetterServiceInterface targetService,
                                   StandardLetterServiceInterface otherService) {
        // Given: a standardLetters list result is returned from the target service
        StandardLetterEntity entity = StandardLetterEntity.builder().build();
        List<StandardLetterEntity> standardLettersList = List.of(entity);
        when(targetService.searchStandardLetters(any())).thenReturn(standardLettersList);

        // When: searchStandardLetters is called on the proxy
        StandardLetterSearchDto criteria = StandardLetterSearchDto.builder().build();
        List<StandardLetterEntity> listResult = standardLetterServiceProxy.searchStandardLetters(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchStandardLetters(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(standardLettersList, listResult);
    }

    @Test
    void shouldUseOpalStandardLetterServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyStandardLetterServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
