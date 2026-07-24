package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.sql.Connection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.qpid.jms.provider.amqp.message.AmqpJmsTextMessageFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.exception.ReportGenerationException;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Sql(scripts = "classpath:db/insertData/insert_into_interface_job_queue_processing.sql",
    executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_interface_job_queue_processing.sql",
    executionPhase = AFTER_TEST_METHOD)
@DisplayName("Interface Job Queue Consumer Integration Tests")
class InterfaceJobQueueConsumerIntegrationTest extends AbstractIntegrationTest {

    private static final Long INTERFACE_JOB_ID = 99000000401000L;
    private static final BigDecimal EXPECTED_PAYMENT_AMOUNT = new BigDecimal("123.45");

    @Autowired
    protected InterfaceJobRepository interfaceJobRepository;

    @Autowired
    protected InterfaceJobQueueIntegrationTestHelper interfaceJobQueueHelper;

    @Autowired
    private BlobServiceClient blobServiceClient;

    @Value("${opal.report.storage.container}")
    private String reportContainerName;

    private final InterfaceJobQueueListener listener;

    @Autowired
    private TransientFailureHelper transientFailureHelper;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    private TextMessage validTextMessage;


    @BeforeEach
    void setUpReportStorage() throws JMSException {
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(reportContainerName);
        if (!blobContainerClient.exists()) {
            blobContainerClient.create();
        }
        validTextMessage = new AmqpJmsTextMessageFacade().asJmsMessage();
        validTextMessage.setText("{\"interface_job_id\":" + INTERFACE_JOB_ID + "}");
    }

    @Autowired
    InterfaceJobQueueConsumerIntegrationTest(InterfaceJobQueueConsumerService interfaceJobQueueConsumerService) {
        this.listener = new InterfaceJobQueueListener(interfaceJobQueueConsumerService);
    }

    @Test
    @JiraStory("PO-2592") // INT.01 and INT.03
    @JiraEpic("PO-2468")
    void int01ValidProcessingMessageInvokesPaymentsInProcedureOnce() throws JMSException {
        listener.onMessage(validTextMessage);

        assertThat(interfaceJobRepository.findById(INTERFACE_JOB_ID))
            .map(InterfaceJobEntity::getStatus)
            .contains(InterfaceJobStatus.COMPLETED);
        interfaceJobQueueHelper.assertSideEffects();
        assertThat(interfaceJobQueueHelper.findPaymentAmountForDefendantAccount())
            .isEqualByComparingTo(EXPECTED_PAYMENT_AMOUNT);
    }

    @Test
    @JiraStory("PO-2592") // INT.02
    @JiraEpic("PO-2468")
    void int02NonProcessingMessageIsAbandonedImmediately() throws JMSException {
        InterfaceJobEntity interfaceJob = interfaceJobRepository.findById(INTERFACE_JOB_ID)
            .orElseThrow();
        interfaceJob.setStatus(InterfaceJobStatus.COMPLETED);
        interfaceJobRepository.saveAndFlush(interfaceJob);

        assertThatThrownBy(() -> listener.onMessage(validTextMessage))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Interface job " + INTERFACE_JOB_ID + " is not PROCESSING");

        InterfaceJobEntity savedJob = interfaceJobRepository.findById(INTERFACE_JOB_ID)
            .orElseThrow();
        assertThat(savedJob.getStatus()).isEqualTo(InterfaceJobStatus.COMPLETED);
        assertThat(savedJob.getCompletedDateTime()).isNull();
        assertThat(interfaceJobQueueHelper.countTillsCreatedForInterfaceFile()).isZero();
        assertThat(interfaceJobQueueHelper.countPaymentsCreatedForDefendantAccount()).isZero();
        assertThat(interfaceJobQueueHelper.countPreAllocatedCashTillReports()).isZero();
    }

    @Test
    @JiraStory("PO-2592") // INT.04
    @JiraEpic("PO-2468")
    void int04TillReturnedButReportFailureRollsBackAndIsRetryable() throws JMSException {
        // Remove the report container so report creation fails after the till is returned,
        // which drives the rollback-and-abandon branch.
        blobServiceClient.getBlobContainerClient(reportContainerName).deleteIfExists();

        assertThatThrownBy(() -> listener.onMessage(validTextMessage))
            .isInstanceOf(ReportGenerationException.class);

        InterfaceJobEntity savedJob = interfaceJobRepository.findById(INTERFACE_JOB_ID)
            .orElseThrow();
        assertThat(savedJob.getStatus()).isEqualTo(InterfaceJobStatus.PROCESSING);
        assertThat(savedJob.getCompletedDateTime()).isNull();
        assertThat(interfaceJobQueueHelper.countTillsCreatedForInterfaceFile()).isZero();
        assertThat(interfaceJobQueueHelper.countPaymentsCreatedForDefendantAccount()).isZero();
        assertThat(interfaceJobQueueHelper.countPreAllocatedCashTillReports()).isZero();
    }

    @Test
    @JiraStory("PO-2592") // INT.05
    @JiraEpic("PO-2468")
    void int05TillReturnedNullMarksJobIgnoredAndCommits() throws JMSException {
        // amount_pence = 0 makes p_int_payments_in succeed without returning a till_id,
        // which drives the IGNORED branch.
        interfaceJobQueueHelper.replaceInterfaceFileRecords(99000000401001L, RECORD_TO_TRIGGER_IGNORED);

        listener.onMessage(validTextMessage);

        InterfaceJobEntity savedJob = interfaceJobRepository.findById(INTERFACE_JOB_ID)
            .orElseThrow();
        assertThat(savedJob.getStatus()).isEqualTo(InterfaceJobStatus.IGNORED);
        assertThat(savedJob.getCompletedDateTime()).isNotNull();
        assertThat(interfaceJobQueueHelper.countTillsCreatedForInterfaceFile()).isZero();
        assertThat(interfaceJobQueueHelper.countPaymentsCreatedForDefendantAccount()).isZero();
        assertThat(interfaceJobQueueHelper.countPreAllocatedCashTillReports()).isZero();
    }

    @Test
    @JiraStory("PO-2592") // INT.06
    @JiraEpic("PO-2468")
    void int06StoredProcedureFailurePersistsFailedMessageAndMarksJobFailed() throws JMSException {
        interfaceJobQueueHelper.replaceInterfaceFileRecords(99000000401001L, RECORD_TO_TRIGGER_FAILED);

        assertThatCode(() -> listener.onMessage(validTextMessage))
            .doesNotThrowAnyException();

        InterfaceJobEntity savedJob = interfaceJobRepository.findById(INTERFACE_JOB_ID)
            .orElseThrow();
        assertThat(savedJob.getStatus()).isEqualTo(InterfaceJobStatus.FAILED);
        assertThat(savedJob.getCompletedDateTime()).isNotNull();
        assertThat(interfaceJobQueueHelper.countTillsCreatedForInterfaceFile()).isZero();
        assertThat(interfaceJobQueueHelper.countPaymentsCreatedForDefendantAccount()).isZero();
        assertThat(interfaceJobQueueHelper.countPreAllocatedCashTillReports()).isZero();
        assertThat(interfaceJobQueueHelper.findFailedInterfaceMessagesForJob())
            .singleElement()
                .satisfies(message -> {
                    assertThat(message.getMessageType()).isEqualTo("Error");
                    assertThat(message.getMessageText()).contains("invalid input syntax for type bigint");
                });
    }

    @Test
    @JiraStory("PO-2592") // INT.07
    @JiraEpic("PO-2468")
    void int07TransientDbFailureRollsBackAndAbandonsMessage() throws Exception {
        // Hold the job row in a separate session so the consumer transaction blocks on update,
        // then apply a short lock timeout in the consumer transaction to force a transient failure.
        try (Connection ignored = interfaceJobQueueHelper.lockInterfaceJobForUpdate(INTERFACE_JOB_ID)) {
            Throwable thrown = catchThrowable(() -> new TransactionTemplate(platformTransactionManager)
                .executeWithoutResult(status -> {
                    jdbcTemplate.execute("SET lock_timeout = '1s'");
                    try {
                        listener.onMessage(validTextMessage);
                    } catch (JMSException ex) {
                        throw new RuntimeException(ex);
                    }
                }));

            assertThat(thrown).isInstanceOf(RuntimeException.class);
            assertThat(transientFailureHelper.isTransientFailure((RuntimeException) thrown)).isTrue();
        }

        interfaceJobQueueHelper.assertJobStatus(InterfaceJobStatus.PROCESSING, false);
        interfaceJobQueueHelper.assertNoSideEffects();
        assertThat(interfaceJobQueueHelper.findFailedInterfaceMessagesForJob()).isEmpty();
    }

    @Test
    @JiraStory("PO-2592") // INT.08
    @JiraEpic("PO-2468")
    void int08Scenario1CommitWaitsForReportSuccess() throws Exception {
        assertCommitBoundary(InterfaceJobStatus.COMPLETED,true);
    }

    @Test
    @JiraStory("PO-2592") // INT.08
    @JiraEpic("PO-2468")
    void int08Scenario2CommitWaitsForIgnoredOutcome() throws Exception {
        interfaceJobQueueHelper.replaceInterfaceFileRecords(99000000401001L, RECORD_TO_TRIGGER_IGNORED);

        assertCommitBoundary(InterfaceJobStatus.IGNORED, false
        );
    }

    //INT.09 Makes no sense to me.
    //If a message is redelivered it must have been a transient exception, and so nothing is written, let alone written twice,

    @Test
    @JiraStory("PO-2592") // INT.10
    @JiraEpic("PO-2468")
    void int10StoredProcedureFailureOnlyUpdatesDocumentedFields() throws JMSException {
        final InterfaceJobEntity beforeJob = interfaceJobRepository.findById(INTERFACE_JOB_ID)
            .orElseThrow();
        final String interfaceNameBefore = beforeJob.getInterfaceName();
        final LocalDateTime createdDateTimeBefore = beforeJob.getCreatedDateTime();
        final LocalDateTime startedDateTimeBefore = beforeJob.getStartedDateTime();

        // amount_pence = "abc" forces the non-transient failure path so we can verify
        // only the documented failure fields are updated.
        interfaceJobQueueHelper.replaceInterfaceFileRecords(99000000401001L, RECORD_TO_TRIGGER_FAILED);

        assertThatCode(() -> listener.onMessage(validTextMessage))
            .doesNotThrowAnyException();

        InterfaceJobEntity afterJob = interfaceJobRepository.findById(INTERFACE_JOB_ID)
            .orElseThrow();
        assertThat(afterJob.getInterfaceJobId()).isEqualTo(INTERFACE_JOB_ID);
        assertThat(afterJob.getInterfaceName()).isEqualTo(interfaceNameBefore);
        assertThat(afterJob.getCreatedDateTime()).isEqualTo(createdDateTimeBefore);
        assertThat(afterJob.getStartedDateTime()).isEqualTo(startedDateTimeBefore);
        assertThat(afterJob.getStatus()).isEqualTo(InterfaceJobStatus.FAILED);
        assertThat(afterJob.getCompletedDateTime()).isNotNull();

        assertThat(interfaceJobQueueHelper.countTillsCreatedForInterfaceFile()).isZero();
        assertThat(interfaceJobQueueHelper.countPaymentsCreatedForDefendantAccount()).isZero();
        assertThat(interfaceJobQueueHelper.countPreAllocatedCashTillReports()).isZero();

        assertThat(interfaceJobQueueHelper.findFailedInterfaceMessagesForJob())
            .singleElement()
            .satisfies(message -> {
                assertThat(message.getInterfaceJobId()).isEqualTo(INTERFACE_JOB_ID);
                assertThat(message.getInterfaceFileId()).isEqualTo(99000000401001L);
                assertThat(message.getMessageType()).isEqualTo("Error");
                assertThat(message.getMessageText()).contains("invalid input syntax for type bigint");
                assertThat(message.getMessageText()).doesNotContain("p_int_payments_in");
                assertThat(message.getMessageText()).doesNotContain("org.postgresql");
                assertThat(message.getRecordIndex()).isNull();
                assertThat(message.getRecordDetail()).isNull();
                assertThat(message.getMessageData()).isNull();
            });
    }


    //INT.11
    //We have coverge apart from some queue aspect we can't write a test for
    // What is still not covered is a true JMS/session commit failure on message completion.
    // That would need broker-level fault injection, not just the current consumer harness.

    //INT.12
    //Unable to write this test as uses Queue directly.

    private void assertCommitBoundary(InterfaceJobStatus expectedStatus,
                                      boolean expectReportSideEffects) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> listenerFuture = null;
        try (Connection ignored = interfaceJobQueueHelper.lockInterfaceJobForUpdate(INTERFACE_JOB_ID)) {
            // Hold the job row in a separate session so the consumer work finishes but cannot
            // commit yet; that lets the test observe the pre-commit state explicitly.
            listenerFuture = executor.submit(() -> {
                try {
                    listener.onMessage(validTextMessage);
                } catch (JMSException ex) {
                    throw new RuntimeException(ex);
                }
            });

            Thread.sleep(500);
            interfaceJobQueueHelper.assertJobStatus(InterfaceJobStatus.PROCESSING, false);
            interfaceJobQueueHelper.assertNoSideEffects();
            assertThat(listenerFuture.isDone()).isFalse();
        }

        assertThat(listenerFuture).isNotNull();
        listenerFuture.get(5, TimeUnit.SECONDS);
        try {
            interfaceJobQueueHelper.assertJobStatus(expectedStatus, true);
            if (expectReportSideEffects) {
                interfaceJobQueueHelper.assertSideEffects();
            } else {
                interfaceJobQueueHelper.assertNoSideEffects();
            }
        } finally {
            executor.shutdownNow();
        }
    }

    // amount_pence = 0 makes p_int_payments_in succeed without returning a till_id,
    // which drives the IGNORED branch.
    private String RECORD_TO_TRIGGER_IGNORED = """
                [{
                    "receiving_sort_code":"123456",
                    "receiving_bank_account_number":"01234567",
                    "receiving_account_type":"5",
                    "transaction_code":"68",
                    "originator_sort_code":"654321",
                    "originator_bank_account_number":"98765432",
                    "amount_pence":0,
                    "originator_name":"Test Payer",
                    "originator_reference":"99000001A",
                    "originator_beneficiary_name":"Test Court"
                }]
                """;

    // amount_pence = "abc" is intentionally invalid so the stored procedure fails
    // with a non-transient database error and the FAILED-message path is exercised.
    private String RECORD_TO_TRIGGER_FAILED = """
                [{
                    "receiving_sort_code":"123456",
                    "receiving_bank_account_number":"01234567",
                    "receiving_account_type":"5",
                    "transaction_code":"68",
                    "originator_sort_code":"654321",
                    "originator_bank_account_number":"98765432",
                    "amount_pence":"abc",
                    "originator_name":"Test Payer",
                    "originator_reference":"99000001A",
                    "originator_beneficiary_name":"Test Court"
                }]
                """;
}
