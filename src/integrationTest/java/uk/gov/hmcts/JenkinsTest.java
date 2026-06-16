package uk.gov.hmcts;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

// This is a placeholder test to ensure the Jenkins pipeline runs successfully.
//This class can not be moved into the darts package else it will be excluded
@Slf4j
class JenkinsTest {

    @Test
    void test() {
        log.info("Jenkins test executed successfully.");
    }
}
