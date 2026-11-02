package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static uk.gov.hmcts.opal.entity.TillStatusEnum.Allocated;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.repository.jpa.ReportInstanceSpecs;
import uk.gov.hmcts.opal.service.blobstore.ReportBlobStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import uk.gov.hmcts.opal.entity.TillStatusEnum;
import uk.gov.hmcts.opal.service.opal.till.AllocateTillsStatusService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Sql("classpath:db/insertData/insert_into_allocate_tills_consumer.sql")
@Sql(scripts = "classpath:db/deleteData/delete_from_allocate_tills_consumer.sql",
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
@ExtendWith(MockitoExtension.class)
class AllocateTillsConsumerIntegrationTest extends AbstractIntegrationTest {

    private static final Long TILL_ID = 9999003432001L;
    private static final Long PAYMENT_IN_ID = 9999003432001L;
    private static final short BUSINESS_UNIT_ID = 9432;
    private static final UUID CASH_TILL_LOCATION = UUID.fromString("00000000-0000-0000-0000-000000003432");
    private static final UUID CASH_LIST_LOCATION = UUID.fromString("00000000-0000-0000-0000-000000003433");
    private static final String TILL_PAYLOAD = """
        {"till_id":9999003432001,
        "business_unit_id":9432,
        "posted_by":"USER01",
        "posted_by_name":"Opal User",
        "requested_by":42}
        """;

    private final AllocateTillsQueueConsumerService consumer;
    private final PaymentInRepository paymentInRepository;
    private final ReportInstanceRepository reportInstanceRepository;

    @MockitoSpyBean
    private TillRepository tillRepository;
    @MockitoBean
    private ReportBlobStore blobStore;
    @Mock
    private ServiceBusReceivedMessageContext messageContext;
    @Mock
    private ServiceBusReceivedMessage receivedMessage;
    @MockitoSpyBean
    private AllocateTillsStatusService statusService;

    @Test
    @JiraEpic("PO-2116")
    @JiraStory("PO-3432")
    void onMessage_allocatesTillAndGeneratesReports() {
        // Arrange
        when(blobStore.storeReport(any(InputStream.class))).thenReturn(CASH_TILL_LOCATION, CASH_LIST_LOCATION);

        // Act
        listen(TILL_PAYLOAD);

        // Assert
        assertThat(tillRepository.findById(TILL_ID).orElseThrow().getStatus()).isEqualTo(Allocated);
        assertThat(paymentInRepository.findById(PAYMENT_IN_ID).orElseThrow().isAllocated()).isTrue();
        verifySettlement(false);
        List<ReportInstanceEntity> reports = reports();
        assertThat(reports).extracting(report -> report.getReport().getReportId())
            .containsExactlyInAnyOrder("cash_till", "cash_list");
        assertThat(reports).allSatisfy(report -> {
            assertThat(report.getGenerationStatus()).isEqualTo(ReportInstanceGenerationStatus.READY);
            assertThat(report.getLocation()).isNotNull();
        });
        verify(blobStore, times(2)).storeReport(any(InputStream.class));
    }

    @ParameterizedTest
    @CsvSource({"1,true", "2,true", "1,false", "2,false"})
    @JiraStory("PO-3432")
    @JiraEpic("PO-2116")
    void reportFailureRollsBackAllocation(int failedReport, boolean transientFailure) {
        // Arrange
        RuntimeException cause = transientFailure
            ? new DataAccessResourceFailureException("Connection failed")
            : new IllegalArgumentException("Invalid report");

        if (failedReport == 1) {
            when(blobStore.storeReport(any(InputStream.class))).thenThrow(cause);
        } else {
            when(blobStore.storeReport(any(InputStream.class)))
                .thenReturn(CASH_TILL_LOCATION)
                .thenThrow(cause);
        }

        // Act
        listen(TILL_PAYLOAD);

        // Assert
        verify(blobStore, times(failedReport)).storeReport(any(InputStream.class));
        assertThat(paymentInRepository.findById(PAYMENT_IN_ID).orElseThrow().isAllocated()).isFalse();
        assertThat(tillRepository.findById(TILL_ID).orElseThrow().getStatus())
            .isEqualTo(transientFailure ? TillStatusEnum.Processing : TillStatusEnum.Failed);
        assertThat(reports()).isEmpty();
        verifySettlement(transientFailure);
    }

    @Test
    @JiraStory("PO-3432")
    @JiraEpic("PO-2116")
    void procedureValidationFailureMarksFailedAndCompletes() {
        // Act
        listen(TILL_PAYLOAD.replace("9432", "9433"));

        // Assert
        assertThat(tillRepository.findById(TILL_ID).orElseThrow().getStatus()).isEqualTo(TillStatusEnum.Failed);
        verifySettlement(false);
        verifyNoInteractions(blobStore);
        assertThat(reports()).isEmpty();
    }

    @Test
    @JiraStory("PO-3432")
    @JiraEpic("PO-2116")
    void procedureConnectionFailureAbandons() {
        // Arrange
        doThrow(new DataAccessResourceFailureException("Connection failed")).when(tillRepository)
            .allocateTill(TILL_ID, "USER01", "Opal User", (short) 9432);

        // Act
        listen(TILL_PAYLOAD);

        // Assert
        assertThat(tillRepository.findById(TILL_ID).orElseThrow().getStatus()).isEqualTo(TillStatusEnum.Processing);
        verifySettlement(true);
        verifyNoInteractions(blobStore);
        assertThat(reports()).isEmpty();
    }

    @Test
    @JiraStory("PO-3432")
    @JiraEpic("PO-2116")
    void failedStatusPersistenceFailureAbandons() {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid report")).when(blobStore)
            .storeReport(any(InputStream.class));
        doThrow(new DataAccessResourceFailureException("Connection failed")).when(statusService).markFailed(TILL_ID);

        // Act
        listen(TILL_PAYLOAD);

        // Assert
        verify(blobStore).storeReport(any(InputStream.class));
        verify(statusService).markFailed(TILL_ID);
        assertThat(tillRepository.findById(TILL_ID).orElseThrow().getStatus()).isEqualTo(TillStatusEnum.Processing);
        assertThat(reports()).isEmpty();
        verifySettlement(true);
    }

    @ParameterizedTest
    @EnumSource(value = TillStatusEnum.class, names = "Processing", mode = EnumSource.Mode.EXCLUDE)
    @JiraStory("PO-3432")
    @JiraEpic("PO-2116")
    void nonProcessingTillAbandons(TillStatusEnum status) {
        // Arrange
        TillEntity till = tillRepository.findById(TILL_ID).orElseThrow();
        till.setStatus(status);
        tillRepository.saveAndFlush(till);

        // Act
        listen(TILL_PAYLOAD);

        // Assert
        verifySettlement(true);
        verify(tillRepository, never()).allocateTill(TILL_ID, "USER01", "Opal User", BUSINESS_UNIT_ID);
        verify(statusService, never()).markFailed(TILL_ID);
        verifyNoInteractions(blobStore);
        assertThat(reports()).isEmpty();
        assertThat(tillRepository.findById(TILL_ID).orElseThrow().getStatus()).isEqualTo(status);
        assertThat(paymentInRepository.findById(PAYMENT_IN_ID).orElseThrow().isAllocated()).isFalse();
    }

    @Test
    @JiraStory("PO-3432")
    @JiraEpic("PO-2116")
    void nonTransientProcedureFailureRollsBackWrites() {
        // Arrange
        AtomicBoolean writeFlushed = failProcedureAfterPaymentWrite(new IllegalArgumentException("Invalid allocation"));

        // Act
        listen(TILL_PAYLOAD);

        // Assert
        assertThat(writeFlushed.get()).isTrue();
        assertThat(paymentInRepository.findById(PAYMENT_IN_ID).orElseThrow().isAllocated()).isFalse();
        assertThat(tillRepository.findById(TILL_ID).orElseThrow().getStatus()).isEqualTo(TillStatusEnum.Failed);
        verify(statusService).markFailed(TILL_ID);
        verifySettlement(false);
        verifyNoInteractions(blobStore);
        assertThat(reports()).isEmpty();
    }

    @Test
    @JiraStory("PO-3432")
    @JiraEpic("PO-2116")
    void transientProcedureFailureRollsBackWrites() {
        // Arrange
        AtomicBoolean writeFlushed = failProcedureAfterPaymentWrite(
            new DataAccessResourceFailureException("Connection failed"));

        // Act
        listen(TILL_PAYLOAD);

        // Assert
        assertThat(writeFlushed.get()).isTrue();
        assertThat(paymentInRepository.findById(PAYMENT_IN_ID).orElseThrow().isAllocated()).isFalse();
        assertThat(tillRepository.findById(TILL_ID).orElseThrow().getStatus()).isEqualTo(TillStatusEnum.Processing);
        verify(statusService, never()).markFailed(TILL_ID);
        verifySettlement(true);
        verifyNoInteractions(blobStore);
        assertThat(reports()).isEmpty();
    }

    private AtomicBoolean failProcedureAfterPaymentWrite(RuntimeException failure) {
        AtomicBoolean writeFlushed = new AtomicBoolean();
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
            PaymentInEntity payment = paymentInRepository.findById(PAYMENT_IN_ID).orElseThrow();
            payment.setAllocated(true);
            paymentInRepository.saveAndFlush(payment);
            writeFlushed.set(true);
            throw failure;
        }).when(tillRepository).allocateTill(TILL_ID, "USER01", "Opal User", BUSINESS_UNIT_ID);
        return writeFlushed;
    }

    private List<ReportInstanceEntity> reports() {
        return reportInstanceRepository.findAll(
            ReportInstanceSpecs.hasAnyBusinessUnitIn(List.of(BUSINESS_UNIT_ID)));
    }

    private void listen(String payload) {
        when(messageContext.getMessage()).thenReturn(receivedMessage);
        when(receivedMessage.getBody()).thenReturn(BinaryData.fromString(payload));
        new AllocateTillsQueueListener(consumer).onMessage(messageContext);
    }

    private void verifySettlement(boolean abandoned) {
        if (abandoned) {
            verify(messageContext).abandon();
            verify(messageContext, never()).complete();
        } else {
            verify(messageContext).complete();
            verify(messageContext, never()).abandon();
        }
    }
}
