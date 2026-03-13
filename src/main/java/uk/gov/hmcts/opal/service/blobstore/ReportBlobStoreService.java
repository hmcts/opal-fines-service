package uk.gov.hmcts.opal.service.blobstore;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobStorageException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.util.UuidProvider;

@Service
@Slf4j(topic = "opal.ReportBlobStoreService")
public class ReportBlobStoreService implements ReportBlobStore {

    private final BlobServiceClient blobServiceClient;
    private final UuidProvider uuidProvider;
    private final String containerName;

    public ReportBlobStoreService(BlobServiceClient blobServiceClient, UuidProvider uuidProvider,
        @Value("${opal.report.storage.container}") String containerName) {
        this.blobServiceClient = blobServiceClient;
        this.containerName = containerName;
        this.uuidProvider = uuidProvider;
    }

    public String storeReport(String report) {
        BlobContainerClient container = blobServiceClient.getBlobContainerClient(containerName);
        if (!container.exists()) {
            throw new IllegalArgumentException("Blob container does not exist");
        }
        String location = String.valueOf(uuidProvider.getUuid());
        BlobClient blob = container.getBlobClient(location);
        if (!blob.exists()) {
            byte[] bytes = report.getBytes(StandardCharsets.UTF_8);
            blob.upload(new ByteArrayInputStream(bytes), bytes.length);
            log.info("Stored report at location: {}", location);
            return location;
        } else {
            return storeReport(report);
        }
    }
}
