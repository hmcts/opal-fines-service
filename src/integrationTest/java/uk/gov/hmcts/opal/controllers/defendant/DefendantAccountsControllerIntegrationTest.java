package uk.gov.hmcts.opal.controllers.defendant;


import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Compatibility bridge class to preserve existing super.*(log) calls
 * in OpalDefendantAccountsIntegrationTest and LegacyDefendantAccountsIntegrationTest.
 * Delegates to the new split integration test classes grouped by API area.
 */

@SpringBootTest
public abstract class DefendantAccountsControllerIntegrationTest extends BaseDefendantAccountsIntegrationTest {


    // --- Test class delegates (sharing Spring context via base injection) ---

    protected final DefendantAccountSearchIntegrationTest search = new DefendantAccountSearchIntegrationTest();

    @BeforeEach
    void propagateBaseContextToDelegates() {
//        injectBaseDependencies(headerSummary);
//        injectBaseDependencies(atAGlance);
//        injectBaseDependencies(party);
//        injectBaseDependencies(paymentTerms);
//        injectBaseDependencies(search);
//        injectBaseDependencies(update);
    }

    /*
      @Autowired protected DefendantAccountHeaderSummaryIntegrationTest headerSummary;
      @Autowired protected DefendantAccountAtAGlanceIntegrationTest atAGlance;
      @Autowired protected DefendantAccountPartyIntegrationTest party;
      @Autowired protected DefendantAccountPaymentTermsIntegrationTest paymentTerms;
      @Autowired protected DefendantAccountSearchIntegrationTest search;
      @Autowired protected DefendantAccountUpdateIntegrationTest update;
     */



    // Payment Terms



    // Defendant Account Party


    // Search





















































    // --- AC1–AC9 Multi-Parameter Search Scenarios (added for full parity) ---
























    // Update

}
