package uk.gov.hmcts.opal.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import feign.FeignException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.exception.DownstreamServiceUnavailableException;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserEnum;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileObject;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileTypeEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.StatusEnum;
import uk.gov.hmcts.opal.repository.InterfaceFileRepository;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.service.filehandler.FileHandlerAPIService;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.BankDetails;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.DestinationDetails;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.InterfaceFileCommonDataExtract;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.OriginatorDetails;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.PaymentType;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.Transaction;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Slf4j(topic = "opal.InterfaceFileProcessorServiceIntegrationTest")
@DisplayName("Interface File Processor Service Integration Test")
@TestPropertySource(properties = {
    "opal.interface-jobs.service-bus.consumer-enabled=false",
    "opal.report.service-bus.consumer-enabled=false"
})
@Sql(scripts = "classpath:db/insertData/insert_interface_file_processor.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_interface_file_processor.sql", executionPhase = AFTER_TEST_METHOD)
class InterfaceFileProcessorServiceIntegrationTest extends AbstractIntegrationTest {

    private static final Long SOURCE_ID = 987L;

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("opal.common.system-users.token-url", wireMockServer::baseUrl);
        registry.add("file-handler.service.url", wireMockServer::baseUrl);
    }

    @Autowired
    private InterfaceFileProcessorService service;

    @Autowired
    private InterfaceFileRepository interfaceFileRepository;

    @Autowired
    private InterfaceJobRepository interfaceJobRepository;

    @MockitoSpyBean
    private FileHandlerAPIService fileHandlerAPIService;

    @ParameterizedTest
    @EnumSource(PaymentType.class)
    @DisplayName("Processes five transactions and saves the interface job and file")
    @JiraStory("PO-6460")
    @JiraEpic("PO-3497")
    void process_savesJobAndFileForFiveTransactions(PaymentType paymentType) {
        stubSuccessfulFileHandler(paymentType);
        final LocalDateTime before = LocalDateTime.now(ZoneOffset.UTC);

        service.process(SOURCE_ID);

        LocalDateTime after = LocalDateTime.now(ZoneOffset.UTC);

        assertJobAndFileSaved(paymentType, before, after);
        assertFileHandlerCalledOnce();
    }

    @ParameterizedTest
    @CsvSource({
        "metadata, Unable to retrieve interface file from file-handler-service",
        "content, Unable to retrieve interface file content from file-handler service",
        "upload, unable to add interface file"
    })
    @DisplayName("Leaves no fines rows when a File Handler call fails")
    @JiraStory("PO-6460")
    @JiraEpic("PO-3497")
    void process_fileHandlerFailureLeavesNoFinesRows(String failingCall, String expectedMessage) {
        stubSuccessfulFileHandler(PaymentType.CASH);
        switch (failingCall) {
            case "metadata" -> wireMockServer.stubFor(get(urlEqualTo("/interface-files/987"))
                .atPriority(1).willReturn(fileHandlerError()));
            case "content" -> wireMockServer.stubFor(get(urlEqualTo("/interface-files/987/content"))
                .atPriority(1).willReturn(fileHandlerError()));
            case "upload" -> wireMockServer.stubFor(post(urlEqualTo("/interface-files"))
                .atPriority(1).willReturn(fileHandlerError()));
        }

        long filesBefore = interfaceFileRepository.count();
        long jobsBefore = interfaceJobRepository.count();

        DownstreamServiceUnavailableException failure = assertThrows(
            DownstreamServiceUnavailableException.class, () -> service.process(SOURCE_ID));

        assertThat(failure).hasMessage(expectedMessage);
        assertThat(failure.getCause())
            .isInstanceOf(FeignException.class)
            .hasMessageContaining("File Handler storage is temporarily unavailable. Please retry later.");
        assertThat(((FeignException) failure.getCause()).status()).isEqualTo(503);

        assertThat(interfaceFileRepository.count()).isEqualTo(filesBefore);
        assertThat(interfaceJobRepository.count()).isEqualTo(jobsBefore);
    }

    @Test
    @DisplayName("Uploads a source JSON file only once")
    @JiraStory("PO-6460")
    @JiraEpic("PO-3497")
    void process_doesNotUploadTheSameSourceTwice() {
        stubSuccessfulFileHandler(PaymentType.CHEQUE);

        service.process(SOURCE_ID);
        service.process(SOURCE_ID);

        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/interface-files")));
        assertThat(interfaceFileRepository.findBySourceJsonId(SOURCE_ID)).isPresent();
    }

    @Test
    @DisplayName("Concurrent deliveries create one transformed file and one job/file pair")
    @JiraStory("PO-6460")
    @JiraEpic("PO-3497")
    void process_concurrentDeliveriesUploadOnce() throws Exception {
        stubSuccessfulFileHandler(PaymentType.CASH);
        CyclicBarrier requestsPassedInitialCheck = new CyclicBarrier(2);
        doAnswer(invocation -> {
            requestsPassedInitialCheck.await(10, TimeUnit.SECONDS);
            return invocation.callRealMethod();
        }).when(fileHandlerAPIService).getInterfaceFile(eq(SystemUserEnum.OPAL_SYSTEM_USER), eq(SOURCE_ID));

        final long filesBefore = interfaceFileRepository.count();
        final long jobsBefore = interfaceJobRepository.count();
        
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> service.process(SOURCE_ID));
            var second = executor.submit(() -> service.process(SOURCE_ID));
            first.get(30, TimeUnit.SECONDS);
            second.get(30, TimeUnit.SECONDS);
        }

        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/interface-files")));
        assertThat(interfaceFileRepository.count()).isEqualTo(filesBefore + 1);
        assertThat(interfaceJobRepository.count()).isEqualTo(jobsBefore + 1);
        assertThat(interfaceFileRepository.findBySourceJsonId(SOURCE_ID)).isPresent();
    }

    @Test
    @DisplayName("An upload failure rolls back the claim so a retry can succeed")
    @JiraStory("PO-6460")
    @JiraEpic("PO-3497")
    void process_retriesAfterUploadFailure() {
        stubSuccessfulFileHandler(PaymentType.CASH);
        wireMockServer.stubFor(post(urlEqualTo("/interface-files"))
            .atPriority(1).willReturn(fileHandlerError()));
        long jobsBefore = interfaceJobRepository.count();

        assertThrows(DownstreamServiceUnavailableException.class, () -> service.process(SOURCE_ID));
        assertThat(interfaceFileRepository.findBySourceJsonId(SOURCE_ID)).isEmpty();
        assertThat(interfaceJobRepository.count()).isEqualTo(jobsBefore);

        wireMockServer.resetAll();
        stubSuccessfulFileHandler(PaymentType.CASH);
        service.process(SOURCE_ID);

        assertThat(interfaceFileRepository.findBySourceJsonId(SOURCE_ID)).isPresent();
        assertThat(interfaceJobRepository.count()).isEqualTo(jobsBefore + 1);
        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/interface-files")));
    }

    private void assertJobAndFileSaved(PaymentType paymentType, LocalDateTime before, LocalDateTime after) {
        InterfaceFileEntity finesFile = interfaceFileRepository.findBySourceJsonId(SOURCE_ID).orElseThrow();
        InterfaceJobEntity job = interfaceJobRepository.findById(finesFile.getInterfaceJob().getInterfaceJobId())
            .orElseThrow();

        assertThat(finesFile.getInterfaceFileId()).isPositive();
        assertThat(finesFile.getFileName()).isEqualTo("test_123");
        assertThat(finesFile.getSource()).isEqualTo("DWP");
        assertThat(finesFile.getSourceJsonId()).isEqualTo(SOURCE_ID);
        assertThat(finesFile.getTransformedJsonId()).isEqualTo(988L);
        assertThat(finesFile.getRecordCount()).isEqualTo((short) 5);
        assertThat(finesFile.getTotalAmount()).isEqualByComparingTo(new BigDecimal("500"));
        assertThat(finesFile.getCreatedDateTime()).isBetween(before, after);
        assertThat(job.getBusinessUnit().getBusinessUnitId()).isEqualTo((short) 6460);
        assertThat(job.getInterfaceName())
            .isEqualTo(paymentType == PaymentType.CASH ? "PAYMENTS_IN" : "PRESENTED_CHEQUES");
        assertThat(job.getStatus()).isEqualTo(InterfaceJobStatus.CREATED);
        assertThat(job.getCreatedDateTime()).isBetween(before, after);
        assertThat(job.getStartedDateTime()).isNull();
        assertThat(job.getCompletedDateTime()).isNull();
    }

    private void assertFileHandlerCalledOnce() {
        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/interface-files/987")));
        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/interface-files/987/content")));
        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/interface-files")));
    }

    private void stubSuccessfulFileHandler(PaymentType paymentType) {
        wireMockServer.stubFor(post(urlEqualTo("/"))
            .willReturn(okJson("{\"token_type\":\"Bearer\",\"access_token\":\"token-value\"}")));

        InterfaceFileObject sourceFile = InterfaceFileObject.builder()
            .interfaceFileId(SOURCE_ID)
            .fileName("test_123")
            .checksum("CS123")
            .filestoreUuid(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
            .source(InterfaceFileEnum.DWP)
            .target(InterfaceFileEnum.OPAL)
            .type(InterfaceFileTypeEnum.SOURCE_JSON)
            .domain(InterfaceFileObject.DomainEnum.FINES)
            .status(StatusEnum.SUCCESS)
            .build();

        wireMockServer.stubFor(get(urlEqualTo("/interface-files/987"))
            .willReturn(okJson(objectMapper.writeValueAsString(sourceFile))));

        wireMockServer.stubFor(get(urlEqualTo("/interface-files/987/content"))
            .willReturn(okJson(objectMapper.writeValueAsString(sourceData(paymentType)))));

        wireMockServer.stubFor(post(urlEqualTo("/interface-files"))
            .willReturn(okJson(objectMapper.writeValueAsString(
                InterfaceFileObject.builder().interfaceFileId(988L).build()))));
    }

    private ResponseDefinitionBuilder fileHandlerError() {
        return aResponse()
            .withStatus(503)
            .withHeader("Content-Type", "application/problem+json")
            .withBody("""
                {
                  "title": "File Handler unavailable",
                  "status": 503,
                  "detail": "File Handler storage is temporarily unavailable. Please retry later."
                }
                """);
    }

    private InterfaceFileCommonDataExtract sourceData(PaymentType paymentType) {
        String[] codes = paymentType == PaymentType.CASH
            ? new String[] {"00", "15", "68", "93", "99"}
            : new String[] {"11", "11", "11", "11", "11"};

        List<Transaction> transactions = Arrays.stream(codes)
            .map(code -> Transaction.builder()
                .transactionCode(code)
                .originatorDetails(OriginatorDetails.builder().accountReference("unmatched").name("Payer").build())
                .amount(100L)
                .build())
            .toList();

        return InterfaceFileCommonDataExtract.builder()
            .fileName("test_123")
            .destinationDetails(DestinationDetails.builder()
                .bankDetails(BankDetails.builder().sortCode("646001").accountNumber("00006460").build())
                .build())
            .paymentType(paymentType)
            .transactions(transactions)
            .build();
    }
}
