package uk.gov.hmcts.opal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Alternate light weight MVC container test for where integration tests don't need the full spring context of the
 * application to be loaded. Specifically, DO NOT extend this class if the integration tests are testing:
 * - any kind of database interaction (queries, transactions, flyway-managed db behaviour, etc.)
 * - any redis integration
 * - LaunchDarkly client integration
 * - controller-to-service-to-repository wiring
 * - behaviour that depends on the complete application configuration
 */
public abstract class AbstractWebMvcIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;
}
