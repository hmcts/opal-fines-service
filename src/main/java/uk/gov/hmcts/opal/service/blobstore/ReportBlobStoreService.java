package uk.gov.hmcts.opal.service.blobstore;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobStorageException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.exception.MissingStoredReportContentException;
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

    public UUID storeReport(InputStream report) {
        BlobContainerClient container = blobServiceClient.getBlobContainerClient(containerName);
        if (!container.exists()) {
            throw new IllegalArgumentException("Blob container does not exist");
        }
        UUID location = uuidProvider.getUuid();
        BlobClient blob = container.getBlobClient(String.valueOf(location));
        blob.upload(report);
        log.info("Stored report at location: {}", location);
        return location;
    }

    public byte[] getReport(UUID locationUUID) {
        String location = String.valueOf(locationUUID);
        BlobContainerClient container = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blob = container.getBlobClient(location);

        log.info("Reading report from location: {}", location);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            blob.downloadStream(outputStream);
            return outputStream.toByteArray();
        } catch (BlobStorageException blobStorageException) {
            if (blobStorageException.getStatusCode() == 404) {
                throw new MissingStoredReportContentException(locationUUID);
            }
            throw blobStorageException;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read report from blob store at: " + location, e);
        }
    }

    public void deleteReport(UUID locationUUID) {
        String location = String.valueOf(locationUUID);
        BlobContainerClient container = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blob = container.getBlobClient(location);

        log.info("Deleting report from location: {}", location);
        blob.deleteIfExists();
    }
}
