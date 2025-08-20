package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

@ActiveProfiles({"integration", "opal"})
@Sql(scripts = "classpath:db/insertData/insert_into_minor_creditors.sql", executionPhase = BEFORE_TEST_CLASS)
@Slf4j(topic = "opal.OpalMinorCreditorIntegrationTest")
public class OpalMinorCreditorIntegrationTest extends MinorCreditorControllerIntegrationTest {

    @Test
    void testPostSearchMinorCreditor_Success() throws Exception {
        super.postSearchMinorCreditorImpl_Success(log);
    }

    @Test
    void testPostSearchMinorCreditor_500Error() throws Exception {
        super.postSearchMinorCreditorImpl_500Error(log);
    }
}

