package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.opal.service.report.ReportId.CASH_TILL;

import com.azure.storage.blob.BlobServiceClient;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.entity.InterfaceMessageEntity;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus;
import uk.gov.hmcts.opal.repository.InterfaceFileRepository;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.repository.InterfaceMessageRepository;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.repository.TillRepository;

@Service
class InterfaceJobQueueIntegrationTestHelper {

    private static final Long INTERFACE_JOB_ID = 99000000401000L;
    private static final Long INTERFACE_FILE_ID = 99000000401001L;
    private static final Long DEFENDANT_ACCOUNT_ID = 99000000401002L;
    private static final String SYSTEM_POSTED_BY_NAME = "interface-jobs";

    @Autowired
    private PaymentInRepository paymentInRepository;

    @Autowired
    private ReportInstanceRepository reportInstanceRepository;

    @Autowired
    private TillRepository tillRepository;

    @Autowired
    private InterfaceFileRepository interfaceFileRepository;

    @Autowired
    private InterfaceMessageRepository interfaceMessageRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private InterfaceJobRepository interfaceJobRepository;

    @Autowired
    private BlobServiceClient blobServiceClient;

    @Value("${opal.report.storage.container}")
    private String reportContainerName;

    @Transactional
    void replaceInterfaceFileRecords(Long interfaceFileId, String recordsJson) {
        InterfaceFileEntity interfaceFile = interfaceFileRepository.findById(interfaceFileId)
            .orElseThrow();
        interfaceFile.setRecords(recordsJson);
        interfaceFileRepository.saveAndFlush(interfaceFile);
    }

    Connection lockInterfaceJobForUpdate(Long interfaceJobId) throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT interface_job_id
                FROM interface_jobs
                WHERE interface_job_id = ?
                FOR UPDATE
                """)) {
            statement.setLong(1, interfaceJobId);
            try (ResultSet ignored = statement.executeQuery()) {
                if (!ignored.next()) {
                    throw new IllegalStateException("Interface job not found with id: " + interfaceJobId);
                }
            }
        } catch (SQLException ex) {
            connection.close();
            throw ex;
        }
        return connection;
    }

    public int countTillsCreatedForInterfaceFile() {
        return Math.toIntExact(tillsCreatedForInterfaceFile().size());
    }

    public int countPaymentsCreatedForDefendantAccount() {
        return Math.toIntExact(paymentsCreatedForDefendantAccount().size());
    }

    public BigDecimal findPaymentAmountForDefendantAccount() {
        return paymentsCreatedForDefendantAccount().stream()
            .findFirst()
            .map(PaymentInEntity::getPaymentAmount)
            .orElse(null);
    }

    public int countPreAllocatedCashTillReports() {
        return Math.toIntExact(findPreAllocatedCashTillReports().size());
    }

    public void assertNoSideEffects() {
        assertThat(countTillsCreatedForInterfaceFile()).isZero();
        assertThat(countPaymentsCreatedForDefendantAccount()).isZero();
        assertThat(countPreAllocatedCashTillReports()).isZero();
    }

    public void assertSideEffects() {
        assertThat(countTillsCreatedForInterfaceFile()).isEqualTo(1);
        assertThat(countPaymentsCreatedForDefendantAccount()).isEqualTo(1);
        assertThat(countPreAllocatedCashTillReports()).isEqualTo(1);
        assertThat(findPreAllocatedCashTillReports())
            .singleElement()
            .satisfies(report -> {
                assertThat(report.getGenerationStatus()).isEqualTo(ReportInstanceGenerationStatus.READY);
                assertThat(report.getLocation()).isNotBlank();
                assertThat(report.getErrors()).isNull();
                assertThat(reportBlobExists(report.getLocation())).isTrue();
                assertThat(reportReferencesTill(report,
                    tillsCreatedForInterfaceFile().stream().map(TillEntity::getTillId).collect(Collectors.toSet())))
                    .isTrue();
            });
    }

    public Set<InterfaceMessageEntity> findFailedInterfaceMessagesForJob() {
        return interfaceMessageRepository.findAll().stream()
            .filter(message -> INTERFACE_JOB_ID.equals(message.getInterfaceJobId()))
            .filter(message -> "Error".equals(message.getMessageType()))
            .collect(Collectors.toSet());
    }

    public void assertJobStatus(InterfaceJobStatus expectedStatus, boolean expectCompletedDateTime) {
        InterfaceJobEntity savedJob = interfaceJobRepository.findById(INTERFACE_JOB_ID)
            .orElseThrow();
        assertThat(savedJob.getStatus()).isEqualTo(expectedStatus);
        if (expectCompletedDateTime) {
            assertThat(savedJob.getCompletedDateTime()).isNotNull();
        } else {
            assertThat(savedJob.getCompletedDateTime()).isNull();
        }
    }

    public Set<ReportInstanceEntity> findPreAllocatedCashTillReports() {
        Set<Long> tillIds = tillsCreatedForInterfaceFile().stream()
            .map(TillEntity::getTillId)
            .collect(Collectors.toSet());

        return reportInstanceRepository.findAll().stream()
            .filter(report -> SYSTEM_POSTED_BY_NAME.equals(report.getRequestedByName()))
            .filter(report -> CASH_TILL.getReportId().equals(report.getReportId()))
            .filter(report -> reportReferencesTill(report, tillIds))
            .collect(Collectors.toSet());
    }

    private Set<TillEntity> tillsCreatedForInterfaceFile() {
        return tillRepository.findAll().stream()
            .filter(till -> INTERFACE_FILE_ID.equals(interfaceFileId(till)))
            .collect(Collectors.toSet());
    }

    private Long interfaceFileId(TillEntity till) {
        return Optional.ofNullable(till.getInterfaceFile())
            .map(InterfaceFileEntity::getInterfaceFileId).orElse(null);
    }

    private Set<PaymentInEntity> paymentsCreatedForDefendantAccount() {
        Set<Long> tillIds = tillsCreatedForInterfaceFile().stream()
            .map(TillEntity::getTillId)
            .collect(Collectors.toSet());

        return paymentInRepository.findAll().stream()
            .filter(payment -> payment.getTillEntity() != null)
            .filter(payment -> tillIds.contains(payment.getTillEntity().getTillId()))
            .filter(payment -> DEFENDANT_ACCOUNT_ID.toString().equals(payment.getAssociatedRecordId()))
            .collect(Collectors.toSet());
    }

    private boolean reportReferencesTill(ReportInstanceEntity report, Set<Long> tillIds) {
        return ToJsonString.toOptionalJsonNode(report.getReportParameters())
            .map(parameters -> parameters.get("till_id"))
            .map(JsonNode::asLong)
            .filter(tillIds::contains)
            .isPresent();
    }

    private boolean reportBlobExists(String location) {
        return blobServiceClient.getBlobContainerClient(reportContainerName)
            .getBlobClient(location)
            .exists();
    }
}
