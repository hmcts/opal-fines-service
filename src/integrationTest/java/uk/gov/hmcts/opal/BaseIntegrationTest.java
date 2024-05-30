package uk.gov.hmcts.opal;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.opal.scheduler.config.CronJobConfiguration;
import uk.gov.hmcts.opal.scheduler.config.QuartzConfiguration;
import uk.gov.hmcts.opal.scheduler.service.JobService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"integration"})
public class BaseIntegrationTest {

    @MockBean
    CronJobConfiguration cronJobConfiguration;
    @MockBean
    QuartzConfiguration quartzConfiguration;
    @MockBean
    JobService jobService;

}
