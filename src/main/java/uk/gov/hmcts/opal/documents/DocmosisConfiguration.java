package uk.gov.hmcts.opal.documents;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import uk.gov.hmcts.opal.documents.docmosis.DocmosisClient;

@Configuration
public class DocmosisConfiguration {

    @Bean
    public DocmosisClient docmosisClient(@Value("${opal.docmosis.endpoint}") String baseUrl) {
        RestClient restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(restClient))
            .build();

        return factory.createClient(DocmosisClient.class);
    }
}
