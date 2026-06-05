package uk.gov.hmcts.opal.service.proxy;

import tools.jackson.core.JacksonException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldRequest;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldResponse;
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
        EnforcementStatus entity = EnforcementStatus.builder()
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
        // Given: legacy mode flag is set
        setLegacyMode(false);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyServiceWhenModeIsLegacy() {
        // Given: legacy mode flag is set
        setLegacyMode(true);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }

    @Test
    void shouldDelegateAddEnforcementToLegacyServiceWhenInLegacyMode() throws JacksonException {
        // arrange
        setLegacyMode(true);

        long defendantAccountId = 77L;
        Short businessUnitId = 10;
        String businessUnitUserId = "BU-USER";
        String ifMatch = "3";
        String auth = "Bearer abc";
        AddDefendantAccountEnforcementRequest req =
            mock(AddDefendantAccountEnforcementRequest.class);

        AddEnforcementResponse expected = AddEnforcementResponse.builder()
            .enforcementId("ENF-L")
            .defendantAccountId("77")
            .version(3)
            .build();

        when(legacyService.addEnforcement(defendantAccountId, businessUnitId,
            businessUnitUserId, ifMatch, req))
            .thenReturn(expected);

        // act
        AddEnforcementResponse result =
            serviceProxy.addEnforcement(defendantAccountId, businessUnitId,
                businessUnitUserId, ifMatch, req);

        // assert
        verify(legacyService).addEnforcement(defendantAccountId, businessUnitId,
            businessUnitUserId, ifMatch, req);
        verifyNoInteractions(opalService);
        assertEquals(expected, result);
    }

    @Test
    void shouldDelegateAddEnforcementToOpalServiceWhenInOpalMode() throws JacksonException {
        // arrange
        setLegacyMode(false);

        long defendantAccountId = 77L;
        Short businessUnitId = 10;
        String businessUnitUserId = "BU-USER";
        String ifMatch = "3";
        String auth = "Bearer abc";
        AddDefendantAccountEnforcementRequest req =
            mock(AddDefendantAccountEnforcementRequest.class);

        AddEnforcementResponse expected = AddEnforcementResponse.builder()
            .enforcementId("ENF-O")
            .defendantAccountId("77")
            .version(3)
            .build();

        when(opalService.addEnforcement(defendantAccountId, businessUnitId,
            businessUnitUserId, ifMatch, req))
            .thenReturn(expected);

        // act
        AddEnforcementResponse result =
            serviceProxy.addEnforcement(defendantAccountId, businessUnitId,
                businessUnitUserId, ifMatch, req);

        // assert
        verify(opalService).addEnforcement(defendantAccountId, businessUnitId,
            businessUnitUserId, ifMatch, req);
        verifyNoInteractions(legacyService);
        assertEquals(expected, result);
    }

    @Test
    void shouldDelegateRemoveEnforcementHoldToLegacyServiceWhenInLegacyMode() {
        setLegacyMode(true);

        long defendantAccountId = 77L;
        Short businessUnitId = (short) 10;
        String businessUnitUserId = "BU-USER";
        String ifMatch = "\"7\"";
        String auth = "Bearer abc";

        RemoveDefendantAccountEnforcementHoldRequest req =
            RemoveDefendantAccountEnforcementHoldRequest.builder().build();

        RemoveDefendantAccountEnforcementHoldResponse expected =
            RemoveDefendantAccountEnforcementHoldResponse.builder().build();

        when(legacyService.removeEnforcementHold(
            defendantAccountId,
            businessUnitId,
            businessUnitUserId,
            ifMatch,
            req
        )).thenReturn(expected);

        RemoveDefendantAccountEnforcementHoldResponse result =
            serviceProxy.removeEnforcementHold(
                defendantAccountId,
                businessUnitId,
                businessUnitUserId,
                ifMatch,
                req
            );

        assertEquals(expected, result);
        verify(legacyService).removeEnforcementHold(
            defendantAccountId,
            businessUnitId,
            businessUnitUserId,
            ifMatch,
            req
        );
        verifyNoInteractions(opalService);
    }
}
