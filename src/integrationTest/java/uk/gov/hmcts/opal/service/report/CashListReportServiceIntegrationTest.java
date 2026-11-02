package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.ERROR;
import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.READY;

import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceRequestReports;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.exception.ReportGenerationException;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.service.blobstore.ReportBlobStore;
import uk.gov.hmcts.opal.service.messaging.ReportQueueConsumerService;
import uk.gov.hmcts.opal.service.messaging.ReportQueueListener;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@Transactional
@RequiredArgsConstructor
@Tag("ExtendedTest")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class CashListReportServiceIntegrationTest extends AbstractIntegrationTest {

    private static final long REPORT_INSTANCE_ID = 99000000343000L;
    private static final long TILL_ID = 99000000343100L;
    private static final short BUSINESS_UNIT_ID = 1777;
    private static final short OTHER_BUSINESS_UNIT_ID = 1778;
    private static final int REQUEST_TILL_NUMBER = 17;
    private static final UUID CASH_LIST_JSON_LOCATION = UUID.fromString("00000000-0000-0000-0000-000000000004");

    private final ReportQueueConsumerService reportQueueConsumerService;

    private ReportQueueListener reportQueueListener;

    private final ReportInstanceRepository reportInstanceRepository;
    private final TillRepository tillRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final CashListReportService cashListReportService;
    private final GenericReportService genericReportService;

    @MockitoBean
    private ReportBlobStore blobStore;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setUp() {
        Instant fixedInstant = Instant.parse("2026-05-27T10:15:30Z");
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        reportQueueListener = new ReportQueueListener(reportQueueConsumerService);
    }

    @Nested
    @Sql(scripts = "classpath:db/insertData/insert_into_cash_list_report_data.sql",
        executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/deleteData/delete_from_cash_list_report_data.sql",
        executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    class GenerateCashListSuccessCases {

        @Test
        @JiraStory("PO-3432")
        @JiraEpic("PO-2116")
        void generateReportData_selectsTillWithinBusinessUnit() {
            // Arrange
            TillEntity originalTill = tillRepository.findById(TILL_ID).orElseThrow();
            BusinessUnitEntity otherUnit = businessUnitRepository.findById(OTHER_BUSINESS_UNIT_ID).orElseThrow();
            final TillEntity otherTill = tillRepository.saveAndFlush(
                TillEntity.builder()
                    .businessUnit(otherUnit)
                    .tillNumber(originalTill.getTillNumber())
                    .ownedBy("OTHER")
                    .createdDate(LocalDateTime.of(2026, 5, 27, 9, 0))
                    .build());
            String reportParameters = "{\"till_number\":" + originalTill.getTillNumber() + "}";
            ReportInstanceEntity originalInstance = ReportInstanceEntity.builder()
                .businessUnit(List.of(BUSINESS_UNIT_ID))
                .reportParameters(reportParameters)
                .build();
            ReportInstanceEntity otherInstance = ReportInstanceEntity.builder()
                .businessUnit(List.of(OTHER_BUSINESS_UNIT_ID))
                .reportParameters(reportParameters)
                .build();

            // Act
            CashListReportData originalReport = cashListReportService.generateReportData(originalInstance);
            CashListReportData otherReport = cashListReportService.generateReportData(otherInstance);

            // Assert
            assertThat(originalReport.getTillDetails().getTillId()).isEqualTo(TILL_ID);
            assertThat(originalReport.getEntries()).hasSize(2);
            assertThat(otherReport.getTillDetails().getTillId()).isEqualTo(otherTill.getTillId());
        }

        @Test
        @JiraStory("PO-3432")
        @JiraEpic("PO-2116")
        void addReportInstance_generatesCashListUsingTillNumber() {
            // Arrange
            TillEntity till = tillRepository.findById(TILL_ID).orElseThrow();
            till.setTillNumber((short) REQUEST_TILL_NUMBER);
            tillRepository.saveAndFlush(till);
            when(blobStore.storeReport(any(InputStream.class))).thenReturn(CASH_LIST_JSON_LOCATION);

            CreateReportInstanceRequestReports request = CreateReportInstanceRequestReports.builder()
                .reportId("cash_list")
                .businessUnitIds(List.of(BUSINESS_UNIT_ID))
                .reportParameters(Map.of("till_number", REQUEST_TILL_NUMBER))
                .reportName("Cash List test")
                .build();

            // Act
            CreateReportInstanceResponseReports response =
                genericReportService.addReportInstance(request, 42L, "Opal User", false);

            // Assert
            ReportInstanceEntity saved = reportInstanceRepository.findById(response.getReportInstanceId())
                .orElseThrow();
            assertThat(saved.getGenerationStatus()).isEqualTo(READY);
            assertThat(saved.getLocation()).isEqualTo(CASH_LIST_JSON_LOCATION);
            assertThat(saved.getNoOfRecords()).isEqualTo((short) 2);
            assertThat(objectMapper.readTree(saved.getReportParameters()))
                .isEqualTo(objectMapper.valueToTree(Map.of("till_number", REQUEST_TILL_NUMBER)));

            ArgumentCaptor<InputStream> jsonCaptor = ArgumentCaptor.forClass(InputStream.class);
            verify(blobStore).storeReport(jsonCaptor.capture());
            JsonNode storedContent = objectMapper.readTree(jsonCaptor.getValue());
            CashListReportData reportData = objectMapper.treeToValue(
                storedContent.path("reportData"), CashListReportData.class);

            assertThat(reportData.getTillDetails().getTillId()).isEqualTo(TILL_ID);
            assertThat(reportData.getTillDetails().getTillNumber()).isEqualTo((short) REQUEST_TILL_NUMBER);
            assertThat(reportData.getTotal()).isEqualByComparingTo("165.50");
        }

        @Test
        @JiraStory("PO-3435")
        @JiraEpic("PO-2116")
        @JiraTestKey("PO-7808")
        void onMessage_generatesCashListReportData() throws JMSException, IOException {
            when(blobStore.storeReport(any(InputStream.class))).thenReturn(CASH_LIST_JSON_LOCATION);

            reportQueueListener.onMessage(textMessage("{\"instanceId\":" + REPORT_INSTANCE_ID + "}"));

            ReportInstanceEntity saved = reportInstanceRepository.findById(REPORT_INSTANCE_ID).orElseThrow();
            assertThat(saved.getGenerationStatus()).isEqualTo(READY);
            assertThat(saved.getLocation()).isEqualTo(CASH_LIST_JSON_LOCATION);
            assertThat(saved.getNoOfRecords()).isEqualTo((short) 2);
            assertThat(saved.getCreatedTimestamp()).isEqualTo(LocalDateTime.of(2026, 5, 27, 10, 15, 30));
            assertThat(saved.getErrors()).isNull();

            ArgumentCaptor<InputStream> jsonCaptor = ArgumentCaptor.forClass(InputStream.class);
            verify(blobStore).storeReport(jsonCaptor.capture());
            String reportJson = new String(jsonCaptor.getValue().readAllBytes(), StandardCharsets.UTF_8);
            assertThat(reportJson).contains("\"tillId\":" + TILL_ID);
            assertThat(reportJson).contains("\"entry\":1");
            assertThat(reportJson).contains("\"type\":\"FA\"");
            assertThat(reportJson).contains("\"accountNumber\":\"ACC123\"");
            assertThat(reportJson).contains("\"name\":\"DOE Jane\"");
            assertThat(reportJson).contains("\"entry\":2");
            assertThat(reportJson).contains("\"type\":\"SA\"");
            assertThat(reportJson).contains("\"suspense\":\"UN\"");
            assertThat(reportJson).contains("\"accountNumber\":\"Suspense Ref\"");
            assertThat(reportJson).contains("\"name\":\"1\"");
            assertThat(reportJson).contains("\"nameAdditionalInformation\":\"Auto - PAYMENTS_IN\"");
            assertThat(reportJson).contains("\"total\":165.50");
        }
    }

    @Nested
    @Sql(scripts = "classpath:db/insertData/insert_into_cash_list_missing_till_report_data.sql",
        executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/deleteData/delete_from_cash_list_report_data.sql",
        executionPhase = ExecutionPhase.AFTER_TEST_METHOD
    )
    class GenerateCashListFailureCases {

        @Test
        @JiraStory("PO-3435")
        @JiraEpic("PO-2116")
        @JiraTestKey("PO-7809")
        void onMessage_setsReportInstanceToErrorWhenCashListGenerationFails() {
            assertThatThrownBy(
                () -> reportQueueListener.onMessage(textMessage("{\"instanceId\":" + REPORT_INSTANCE_ID + "}")))
                .isInstanceOf(ReportGenerationException.class)
                .hasMessageContaining("Error generating report instance");

            ReportInstanceEntity saved = reportInstanceRepository.findById(REPORT_INSTANCE_ID).orElseThrow();
            assertThat(saved.getGenerationStatus()).isEqualTo(ERROR);
            assertThat(saved.getErrors()).isNotNull();
            assertThat(saved.getErrors().error()).contains(
                "Cash List report till not found for till_id: 999999999");
        }
    }

    private static TextMessage textMessage(String payload) throws JMSException {
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(payload);
        return textMessage;
    }
}
