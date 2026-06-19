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
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.exception.ReportGenerationException;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.service.blobstore.ReportBlobStore;
import uk.gov.hmcts.opal.service.messaging.ReportQueueConsumerService;
import uk.gov.hmcts.opal.service.messaging.ReportQueueListener;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Transactional
class CashListReportServiceIntegrationTest extends AbstractIntegrationTest {

    private static final long REPORT_INSTANCE_ID = 99000000343000L;
    private static final long TILL_ID = 99000000343100L;

    @Autowired
    private ReportQueueConsumerService reportQueueConsumerService;

    private ReportQueueListener reportQueueListener;

    @Autowired
    private ReportInstanceRepository reportInstanceRepository;

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
        @JiraStory("PO-3435")
        @JiraEpic("PO-2116")
        void onMessage_generatesCashListReportData() throws JMSException {
            when(blobStore.storeReport(any(String.class))).thenReturn("cash-list-json");

            reportQueueListener.onMessage(textMessage("{\"instanceId\":" + REPORT_INSTANCE_ID + "}"));

            ReportInstanceEntity saved = reportInstanceRepository.findById(REPORT_INSTANCE_ID).orElseThrow();
            assertThat(saved.getGenerationStatus()).isEqualTo(READY);
            assertThat(saved.getLocation()).isEqualTo("cash-list-json");
            assertThat(saved.getNoOfRecords()).isEqualTo((short) 2);
            assertThat(saved.getCreatedTimestamp()).isEqualTo(LocalDateTime.of(2026, 5, 27, 10, 15, 30));
            assertThat(saved.getErrors()).isNull();

            ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
            verify(blobStore).storeReport(jsonCaptor.capture());
            String reportJson = jsonCaptor.getValue();
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
            assertThat(reportJson).contains("\"nameAdditionalInformation\":\"Auto - Suspense payment\"");
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
