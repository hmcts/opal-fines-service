package uk.gov.hmcts.opal.service.legacy;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.common.legacy.config.LegacyGatewayProperties;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.common.legacy.service.LegacyGatewayService;
import uk.gov.hmcts.opal.disco.legacy.LegacyTestsBase;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.mapper.legacy.LegacyDefendantAccountHistoryResponseMapper;
import uk.gov.hmcts.opal.mapper.legacy.LegacyUpdateDefendantAccountResponseMapper;
import uk.gov.hmcts.opal.mapper.request.UpdateDefendantAccountRequestMapper;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.CourtService;
import uk.gov.hmcts.opal.service.opal.LocalJusticeAreaService;
import uk.gov.hmcts.opal.service.opal.history.HistoryItemOrderingService;

@ExtendWith(MockitoExtension.class)
abstract class AbstractLegacyDefAccServiceTest extends LegacyTestsBase {

    @Spy
    protected MockRestClient restClient = spy(MockRestClient.class);

    @Mock
    protected LegacyGatewayProperties gatewayProperties;

    @Mock
    protected CourtService courtService;

    @Mock
    protected LocalJusticeAreaService ljaService;

    protected GatewayService gatewayService;
    protected HistoryItemOrderingService historyItemOrderingService = new HistoryItemOrderingService();
    protected LegacyDefendantAccountHistoryResponseMapper legacyDefendantAccountHistoryResponseMapper =
        Mappers.getMapper(LegacyDefendantAccountHistoryResponseMapper.class);

    @Mock protected UpdateDefendantAccountRequestMapper updateDefendantAccountRequestMapper;
    @Mock protected LegacyUpdateDefendantAccountResponseMapper legacyUpdateDefendantAccountResponseMapper;
    @Mock protected UserStateService userStateService;

    protected LegacyDefendantAccountService legacyDefendantAccountService;

    protected UpdateDefendantAccountRequest updateDefendantAccountRequest;

    @BeforeEach
    void openMocks() {
        gatewayService = Mockito.spy(new LegacyGatewayService(gatewayProperties, restClient));
        legacyDefendantAccountService = new LegacyDefendantAccountService(
            gatewayService,
            gatewayProperties,
            courtService,
            ljaService,
            historyItemOrderingService,
            legacyDefendantAccountHistoryResponseMapper,
            updateDefendantAccountRequestMapper,
            legacyUpdateDefendantAccountResponseMapper
        );

        updateDefendantAccountRequest = mock(UpdateDefendantAccountRequest.class, RETURNS_DEEP_STUBS);
    }
}
