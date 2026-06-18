package uk.gov.hmcts.opal;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    private static final String RUN_JSON_TEST = "runJsonTest";
    private static final String RUN_PDF_TEST = "runPdfTest";

    private final BlobServiceClient blobServiceClient;
    private final String containerName;
    private final String jsonPrefix;
    private final String pdfPrefix;

    public TestService(BlobServiceClient blobServiceClient,
                       @Value("${opal.report.storage.container}") String containerName,
                       @Value("${opal.report.storage.test-json-prefix:json/input/}") String jsonPrefix,
                       @Value("${opal.report.storage.test-pdf-prefix:pdf/input/}") String pdfPrefix) {
        this.blobServiceClient = blobServiceClient;
        this.containerName = containerName;
        this.jsonPrefix = jsonPrefix;
        this.pdfPrefix = pdfPrefix;
    }

    public BinaryData runTest(String testName) {
        BlobContainerClient container = getBlobContainer();

        if (RUN_JSON_TEST.equalsIgnoreCase(testName)) {
            return BinaryData.fromBytes(mergeJsonBlobs(container));
        } else if (RUN_PDF_TEST.equalsIgnoreCase(testName)) {
            return BinaryData.fromBytes(mergePdfBlobs(container));
        }

        throw new IllegalArgumentException("Unsupported test name: " + testName);
    }

    private byte[] mergeJsonBlobs(BlobContainerClient container) {
        List<String> blobNames = listBlobNames(container, jsonPrefix);
        return aggregateJsonBlobsFromContainer(container, blobNames);
    }

    private byte[] mergePdfBlobs(BlobContainerClient container) {
        List<String> blobNames = listBlobNames(container, pdfPrefix);
        try {
            return mergePdfBlobsFromContainer(container, blobNames);
        } catch (IOException e) {
            throw new RuntimeException("Exception occurred while merging PDF files.", e);
        }
    }

    private BlobContainerClient getBlobContainer() {
        BlobContainerClient container = blobServiceClient.getBlobContainerClient(containerName);
        if (!container.exists()) {
            throw new IllegalArgumentException("Blob container does not exist");
        }
        return container;
    }

    private static byte[] aggregateJsonBlobsFromContainer(BlobContainerClient container, List<String> blobNames) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.writeBytes("{\"documents\":[".getBytes(StandardCharsets.UTF_8));

            for (int i = 0; i < blobNames.size(); i++) {
                if (i > 0) {
                    outputStream.write(',');
                }
                container.getBlobClient(blobNames.get(i)).downloadStream(outputStream);
            }

            outputStream.writeBytes("]}".getBytes(StandardCharsets.UTF_8));
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Exception occurred while merging JSON files.", e);
        }
    }

    private static byte[] mergePdfBlobsFromContainer(BlobContainerClient container, List<String> blobNames)
        throws IOException {
        Path tempDirectory = Files.createTempDirectory("opal-pdf-merge-");

        try {
            PDFMergerUtility merger = new PDFMergerUtility();
            for (int i = 0; i < blobNames.size(); i++) {
                Path inputFile = tempDirectory.resolve("input-" + i + ".pdf");
                try (OutputStream outputStream = Files.newOutputStream(inputFile)) {
                    container.getBlobClient(blobNames.get(i)).downloadStream(outputStream);
                }
                merger.addSource(inputFile.toFile());
            }

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                merger.setDestinationStream(outputStream);
                merger.mergeDocuments(IOUtils.createTempFileOnlyStreamCache());
                return outputStream.toByteArray();
            }
        } finally {
            deleteDirectory(tempDirectory);
        }
    }

    private static void deleteDirectory(Path directory) {
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // Best-effort cleanup of temporary PDF files.
                }
            });
        } catch (IOException ignored) {
            // Best-effort cleanup of temporary PDF files.
        }
    }

    private static List<String> listBlobNames(BlobContainerClient container, String prefix) {
        List<String> blobNames = StreamSupport.stream(
                container.listBlobs(new ListBlobsOptions().setPrefix(prefix), null).spliterator(),
                false
            )
            .map(BlobItem::getName)
            .sorted()
            .toList();

        if (blobNames.isEmpty()) {
            throw new IllegalArgumentException("No blobs found under prefix: " + prefix);
        }

        return blobNames;
    }
}
