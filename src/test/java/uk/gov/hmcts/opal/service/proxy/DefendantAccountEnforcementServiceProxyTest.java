package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.service.iface.DefendantAccountEnforcementServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountEnforcementService;
import uk.gov.hmcts.opal.service.opal.OpalDefendantAccountEnforcementService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DefendantAccountEnforcementServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private OpalDefendantAccountEnforcementService opalService;

    @Mock
    private LegacyDefendantAccountEnforcementService legacyService;

    @InjectMocks
    private DefendantAccountEnforcementServiceProxy serviceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(DefendantAccountEnforcementServiceInterface targetService,
                  DefendantAccountEnforcementServiceInterface otherService) {
        testGetEnforcementStatus(targetService, otherService);
    }

    void testGetEnforcementStatus(DefendantAccountEnforcementServiceInterface targetService,
                                  DefendantAccountEnforcementServiceInterface otherService) {
        // Given: a Entity is returned from the target service
        EnforcementStatus entity = EnforcementStatus.newBuilder()
            .build();
        when(targetService.getEnforcementStatus(anyLong())).thenReturn(entity);

        EnforcementStatus headerSummaryResult = serviceProxy.getEnforcementStatus(1L);

        // Then: target service should be used, and the returned entity should be as expected
        verify(targetService).getEnforcementStatus(1L);
        verifyNoInteractions(otherService);
        assertEquals(entity, headerSummaryResult);
    }

    @Test
    void shouldUseOpalServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }

    @Test
    void shouldDelegateAddEnforcementToLegacyServiceWhenInLegacyMode() {
        // arrange
        setMode(LEGACY);

        long defendantAccountId = 77L;
        String businessUnitId = "10";
        String businessUnitUserId = "BU-USER";
        String ifMatch = "\"3\"";
        String auth = "Bearer abc";
        AddDefendantAccountEnforcementRequest req =
            mock(AddDefendantAccountEnforcementRequest.class);

        AddEnforcementResponse expected = AddEnforcementResponse.builder()
            .enforcementId("ENF-L")
            .defendantAccountId("77")
            .version(3)
            .build();

        when(legacyService.addEnforcement(defendantAccountId, businessUnitId,
            businessUnitUserId, ifMatch, auth, req))
            .thenReturn(expected);

        // act
        AddEnforcementResponse result =
            serviceProxy.addEnforcement(defendantAccountId, businessUnitId,
                businessUnitUserId, ifMatch, auth, req);

        // assert
        verify(legacyService).addEnforcement(defendantAccountId, businessUnitId,
            businessUnitUserId, ifMatch, auth, req);
        verifyNoInteractions(opalService);
        assertEquals(expected, result);
    }

    @Test
    void shouldDelegateAddEnforcementToOpalServiceWhenInOpalMode() {
        // arrange
        setMode(OPAL);

        long defendantAccountId = 77L;
        String businessUnitId = "10";
        String businessUnitUserId = "BU-USER";
        String ifMatch = "\"3\"";
        String auth = "Bearer abc";
        AddDefendantAccountEnforcementRequest req =
            mock(AddDefendantAccountEnforcementRequest.class);

        AddEnforcementResponse expected = AddEnforcementResponse.builder()
            .enforcementId("ENF-O")
            .defendantAccountId("77")
            .version(3)
            .build();

        when(opalService.addEnforcement(defendantAccountId, businessUnitId,
            businessUnitUserId, ifMatch, auth, req))
            .thenReturn(expected);

        // act
        AddEnforcementResponse result =
            serviceProxy.addEnforcement(defendantAccountId, businessUnitId,
                businessUnitUserId, ifMatch, auth, req);

        // assert
        verify(opalService).addEnforcement(defendantAccountId, businessUnitId,
            businessUnitUserId, ifMatch, auth, req);
        verifyNoInteractions(legacyService);
        assertEquals(expected, result);
    }


}
