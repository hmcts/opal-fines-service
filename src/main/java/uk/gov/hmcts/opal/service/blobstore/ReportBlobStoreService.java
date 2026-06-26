package uk.gov.hmcts.opal.service.blobstore;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
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
        byte[] bytes = report.getBytes(StandardCharsets.UTF_8);
        blob.upload(new ByteArrayInputStream(bytes), bytes.length);
        log.info("Stored report at location: {}", location);
        return location;
    }

    public String getReport(String location) {
        BlobContainerClient container = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blob = container.getBlobClient(location);

        log.info("Reading report from location: {}", location);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            blob.downloadStream(outputStream);
            return outputStream.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read report from blob store at: " + location, e);
        }
    }
}
