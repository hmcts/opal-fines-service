package uk.gov.hmcts.opal.service.filehandler.clients;

import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import feign.Contract;
import feign.Feign;
import feign.FeignException;
import feign.Retryer;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.support.GenericApplicationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.AddInterfaceFileRequestMetadata;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.AddInterfaceFileRequestMetadata.DomainEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.AddInterfaceFileRequestMetadata.PaymentTypeEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileTypeEnum;
import uk.gov.hmcts.opal.logging.integration.client.config.PdpoSyncFeignConfig;
import uk.gov.hmcts.opal.service.filehandler.FileHandlerSystemUserContext;
import uk.gov.hmcts.opal.service.filehandler.TransformedJsonMultipartFile;

class FileHandlerFeignClientConfigurationTest {

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(ObjectMapper.class, () -> JsonMapper.builder().build())
        .withBean(FileHandlerSystemUserContext.class, FileHandlerSystemUserContext::new)
        .withUserConfiguration(FileHandlerFeignClientConfiguration.class, FeignClientsConfiguration.class);

    @Test
    void getInterfaceFile_preservesErrorDetailsWhenParentHasPdpoDecoder() {
        wireMockServer.stubFor(get(urlEqualTo("/interface-files/987"))
            .willReturn(aResponse().withStatus(503)
                .withHeader("Content-Type", "application/problem+json")
                .withBody("""
                    {"detail":"File Handler storage is temporarily unavailable. Please retry later."}
                    """)));

        try (GenericApplicationContext parent = new GenericApplicationContext()) {
            parent.registerBean(ErrorDecoder.class, () -> new PdpoSyncFeignConfig().pdpoSyncErrorDecoder());
            parent.refresh();

            contextRunner.withParent(parent).run(context -> {
                FileHandlerClient client = Feign.builder()
                    .contract(context.getBean(Contract.class))
                    .encoder(context.getBean(Encoder.class))
                    .errorDecoder(context.getBean(ErrorDecoder.class))
                    .retryer(Retryer.NEVER_RETRY)
                    .target(FileHandlerClient.class, wireMockServer.baseUrl());

                assertThatThrownBy(() -> client.getInterfaceFile(987L))
                    .isInstanceOfSatisfying(FeignException.class, failure -> {
                        assertThat(failure.status()).isEqualTo(503);
                        assertThat(failure.contentUTF8())
                            .contains("File Handler storage is temporarily unavailable. Please retry later.");
                    })
                    .hasMessageContaining("File Handler storage is temporarily unavailable. Please retry later.");
            });
        }
    }

    @Test
    void addInterfaceFile_sendsMetadataAsJsonMultipartPart() {
        wireMockServer.stubFor(post(urlEqualTo("/interface-files"))
            .willReturn(aResponse().withStatus(204)));

        contextRunner.run(context -> {
            FileHandlerClient client = Feign.builder()
                .contract(context.getBean(Contract.class))
                .encoder(context.getBean(Encoder.class))
                .target(FileHandlerClient.class, wireMockServer.baseUrl());

            AddInterfaceFileRequestMetadata metadata = AddInterfaceFileRequestMetadata.builder()
                .relatedInterfaceFileId(20L)
                .shouldPreProcessFile(false)
                .source(InterfaceFileEnum.BTECKOH)
                .target(InterfaceFileEnum.OPAL)
                .type(InterfaceFileTypeEnum.TRANSFORMED_JSON)
                .domain(DomainEnum.FINES)
                .paymentType(PaymentTypeEnum.CASH)
                .fileName("transformed.json")
                .businessUnitCode("BC12")
                .build();

            client.addInterfaceFile(new TransformedJsonMultipartFile(
                "transformed.json", "{\"transactions\":[]}".getBytes(StandardCharsets.UTF_8)), metadata);

            wireMockServer.verify(1, postRequestedFor(urlEqualTo("/interface-files"))
                .withAnyRequestBodyPart(aMultipart()
                    .withName("metadata")
                    .withHeader("Content-Type", containing("application/json"))
                    .withBody(equalToJson("""
                        {
                          "related_interface_file_id": 20,
                          "should_pre_process_file": false,
                          "source": "BTECKOH",
                          "target": "OPAL",
                          "type": "TRANSFORMED_JSON",
                          "domain": "FINES",
                          "payment_type": "CASH",
                          "file_name": "transformed.json",
                          "business_unit_code": "BC12"
                        }
                        """)))
                .withAnyRequestBodyPart(aMultipart()
                    .withName("file")
                    .withHeader("Content-Type", containing("application/json"))
                    .withHeader("Content-Disposition", containing("filename=\"transformed.json\""))
                    .withBody(equalTo("{\"transactions\":[]}"))));
        });
    }
}
