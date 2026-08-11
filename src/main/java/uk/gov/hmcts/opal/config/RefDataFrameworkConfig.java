package uk.gov.hmcts.opal.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.service.refdata.framework.RefDataQueueConsumerService;
import uk.gov.hmcts.opal.service.refdata.framework.RefDataUpdateHandler;
import uk.gov.hmcts.opal.service.refdata.framework.SchemaValidationService;

@Configuration
public class RefDataFrameworkConfig {

    @Bean
    public SchemaValidationService schemaValidationService() {
        return new SchemaValidationService();
    }

    @Bean
    public RefDataQueueConsumerService refDataQueueConsumerService(
        ObjectMapper objectMapper,
        SchemaValidationService schemaValidationService,
        List<RefDataUpdateHandler<?, ?>> handlers
    ) {
        return new RefDataQueueConsumerService(objectMapper, schemaValidationService, handlers);
    }
}
