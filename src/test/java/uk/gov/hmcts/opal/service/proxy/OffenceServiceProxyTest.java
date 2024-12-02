package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.service.legacy.LegacyOffenceService;
import uk.gov.hmcts.opal.service.opal.OffenceService;

class OffenceServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private OffenceService opalService;

    @Mock
    private LegacyOffenceService legacyService;

    @InjectMocks
    private OffenceServiceProxy offenceServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

}
