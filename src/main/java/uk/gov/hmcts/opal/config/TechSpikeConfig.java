package uk.gov.hmcts.opal.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.techspike.ActionMain;
import uk.gov.hmcts.opal.techspike.TechSpikeAction;
import uk.gov.hmcts.opal.config.properties.BeDeveloperConfiguration;
import uk.gov.hmcts.opal.controllers.DraftAccountController;
import uk.gov.hmcts.opal.service.opal.DraftAccountService;


@Configuration
@Slf4j(topic = "TechSpikeConfig")
@ConditionalOnProperty(prefix = "opal.testing-support-endpoints", name = "enabled", havingValue = "true")
public class TechSpikeConfig {

    private final BeDeveloperConfiguration developerConfiguration;
    private final boolean enabled;

    public TechSpikeConfig(BeDeveloperConfiguration developerConfiguration) {
        this.developerConfiguration = developerConfiguration;
        log.info(":: tech spike: {}", developerConfiguration.getTechSpike());
        enabled = developerConfiguration.getTechSpike().equals("Transactions-Locking");
    }

    /**
     * We can either use 'applicationTaskExecutor' or 'taskScheduler' for a built-in SimpleAsyncTaskExecutor.
     */
    @Bean
    public CommandLineRunner action1(TaskExecutor applicationTaskExecutor,
                                     @Autowired DraftAccountService draftAccountService,
                                     @Autowired DraftAccountController controller,
                                     @Autowired RestClient restClient) {
        return constructRunnableAction(applicationTaskExecutor,
                                       new ActionMain(draftAccountService, controller, restClient));
    }

    private CommandLineRunner constructRunnableAction(TaskExecutor executor, TechSpikeAction action) {
        if (enabled) {
            return new CommandLineRunner() {
                public void run(String... args) throws Exception {
                    log.info("Tech Spike ({}) Enabled", action.getLogName());
                    executor.execute(action);
                }
            };
        } else {
            return new CommandLineRunner() {
                @Override
                public void run(String... args) throws Exception {
                    log.info("Tech Spike ({}) Disabled", action.getLogName());
                }
            };
        }
    }

}
