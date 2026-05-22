package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(profiles = {"integration-with-spring-security", "opal"}, inheritProfiles = false)
@Slf4j(topic = "opal.OpalDefendantEnforcementIntegrationTest")
public class OpalDefendantEnforcementIntegrationTest extends DefendantEnforcementIntegrationTest {

    @Test
    public void testAddEnforcement_whenGivenAllFields_addsEnforcement() throws Exception {
        super.postEnforcementImpl_fullRequest_Success(log);
    }

    @Test
    public void testAddEnforcement_whenGivenMinimumFields_addsEnforcement() throws Exception {
        super.postEnforcementImpl_minimumRequest_Success(log);
    }

    @Test
    public void testAddEnforcement_whenGivenInvalidDefendant_Fails() throws Exception {
        super.postEnforcementImpl_invalidDefendant_Failure(log);
    }
}
