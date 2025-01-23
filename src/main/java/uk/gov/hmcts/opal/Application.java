package uk.gov.hmcts.opal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("uk.gov.hmcts.opal.*")
@EntityScan("uk.gov.hmcts.opal.*")
@EnableCaching
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    public static void main(final String[] args) {
        System.setProperty("javax.net.debug", "all");
        SpringApplication.run(Application.class, args);
    }
}
