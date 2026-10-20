package uk.gov.hmcts.opal.service.filehandler.clients;

import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import feign.Contract;
import feign.Feign;
import feign.codec.Encoder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.AddInterfaceFileRequestMetadata;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.AddInterfaceFileRequestMetadata.DomainEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.AddInterfaceFileRequestMetadata.PaymentTypeEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileTypeEnum;
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
