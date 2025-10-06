package uk.gov.hmcts.opal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import uk.gov.hmcts.common.user.authorisation.TestUtils;

@SpringBootApplication
@EnableJpaRepositories("uk.gov.hmcts.opal.*")
@EnableFeignClients("uk.gov.hmcts.opal.*")
@EntityScan("uk.gov.hmcts.opal.*")
@EnableCaching
@Slf4j
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    public static void main(final String[] args) {

        System.out.println(TestUtils.COMMONLIB_TEST_STRING);
        log.info("********** ********** ********** ********** ********** **********");
        log.info("Application started with test string: {}", TestUtils.COMMONLIB_TEST_STRING);
        SpringApplication.run(Application.class, args);
    }
}
