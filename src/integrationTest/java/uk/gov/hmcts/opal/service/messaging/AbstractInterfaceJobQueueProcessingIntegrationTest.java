package uk.gov.hmcts.opal.service.messaging;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;

@Sql(scripts = "classpath:db/insertData/insert_into_interface_job_queue_processing.sql",
    executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_interface_job_queue_processing.sql",
    executionPhase = AFTER_TEST_METHOD)
abstract class AbstractInterfaceJobQueueProcessingIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    protected InterfaceJobRepository interfaceJobRepository;

    @Autowired
    protected InterfaceJobQueueIntegrationTestHelper interfaceJobQueueHelper;

    @Autowired
    private BlobServiceClient blobServiceClient;

    @Value("${opal.report.storage.container}")
    private String reportContainerName;

    @BeforeEach
    void setUpReportStorage() {
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(reportContainerName);
        if (!blobContainerClient.exists()) {
            blobContainerClient.create();
        }
    }

    protected boolean reportBlobExists(String location) {
        return blobServiceClient.getBlobContainerClient(reportContainerName)
            .getBlobClient(location)
            .exists();
    }
}
