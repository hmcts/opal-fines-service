package uk.gov.hmcts.opal.service.filehandler;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.GetInterfaceFiles200Response;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileObject;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileObject.DomainEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileTypeEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.StatusEnum;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Slf4j(topic = "opal.FileHandlerAPIServiceIntegrationTest")
@DisplayName("FileHandler API Service Integration Test")
@WireMockTest(httpPort = 4075)
public class FileHandlerAPIServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private FileHandlerAPIService fileHandlerAPIService;

    private final Map<String, String> userAuthToken = Map.of(
        "token_type", "Bearer",
        "access_token", "token-value"
    );

    private final InterfaceFileObject interfaceFileEntity = InterfaceFileObject.builder()
        .interfaceFileId(0L)
        .fileName("filename")
        .checksum("00000000000000000000000000000000")
        .filestoreUuid(UUID.fromString("00000000-0000-0000-0000-000000000000"))
        .source(InterfaceFileEnum.NATWEST)
        .target(InterfaceFileEnum.OPAL)
        .status(StatusEnum.SUCCESS)
        .type(InterfaceFileTypeEnum.SOURCE)
        .domain(DomainEnum.FILE_HANDLER)
        .createdDatetime(LocalDateTime.of(2026, 9, 24, 11, 0, 0))
        .build();

    private final GetInterfaceFiles200Response getInterfaceFiles200Response = GetInterfaceFiles200Response.builder()
        .numberOfResults(1)
        .interfaceFiles(List.of(interfaceFileEntity))
        .build();

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("opal.common.system-users.token-url", wireMockServer::baseUrl);
    }

    @BeforeEach
    public void beforeEach() {
        wireMockServer.stubFor(post(urlEqualTo("/"))
            .willReturn(okJson(objectMapper.writeValueAsString(userAuthToken))));

        stubFor(get(urlEqualTo("/interface-files/0/content"))
            .withHeader(HttpHeaders.AUTHORIZATION, matching("^Bearer token-value$"))
            .willReturn(okJson(objectMapper.writeValueAsString(interfaceFileEntity))));

        stubFor(get(urlEqualTo("/interface-files"))
            .withHeader(HttpHeaders.AUTHORIZATION, matching("^Bearer token-value$"))
            .willReturn(okJson(objectMapper.writeValueAsString(getInterfaceFiles200Response))));
    }

    @Test
    @DisplayName("Correctly calls FileHandler endpoint with auth header")
    @JiraStory("PO-6497")
    @JiraEpic("PO-3497")
    void getInterfaceFileContent_success() throws IOException {
        var response = fileHandlerAPIService.getInterfaceFileContent(SystemUserEnum.OPAL_SYSTEM_USER, 0L);

        var responseObject = objectMapper.readValue(
            response.getContentAsString(StandardCharsets.UTF_8), InterfaceFileObject.class);

        assertEquals(responseObject, interfaceFileEntity);

        WireMock.verify(1, getRequestedFor(urlPathEqualTo("/interface-files/0/content")));
    }

    @Test
    @DisplayName("Correctly calls FileHandler endpoint with auth header")
    @JiraStory("PO-6497")
    @JiraEpic("PO-3497")
    void getInterfaceFiles_success() {
        var response = fileHandlerAPIService.getInterfaceFiles(
            SystemUserEnum.OPAL_SYSTEM_USER, null, null, null, null, null, null, null);

        assertEquals(response, getInterfaceFiles200Response);

        WireMock.verify(1, getRequestedFor(urlPathEqualTo("/interface-files")));
    }

}
