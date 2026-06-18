package uk.gov.hmcts.opal;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    private static final String RUN_JSON_TEST = "runJsonTest";
    private static final String RUN_PDF_TEST = "runPdfTest";
    private static final String SETUP_TEST_DATA = "setupTestData";
    private static final String SETUP_PDF_TEST_DATA = "setupPdfTestData";
    private static final String SETUP_JSON_TEST_DATA = "setupJsonTestData";
    private static final int DEFAULT_SETUP_COUNT = 100;
    private static final int PDF_PAGE_COUNT = 5;

    private final BlobServiceClient blobServiceClient;
    private final String containerName;
    private final String blobPrefix;

    public TestService(BlobServiceClient blobServiceClient,
                       @Value("${opal.report.storage.test-container:testcontainer}") String containerName,
                       @Value("${opal.report.storage.test-prefix:}") String blobPrefix) {
        this.blobServiceClient = blobServiceClient;
        this.containerName = containerName;
        this.blobPrefix = normaliseBlobPrefix(blobPrefix);
    }

    public BinaryData runTest(String testName) {
        return runTest(testName, null);
    }

    public BinaryData runTest(String testName, Integer count) {
        BlobContainerClient container = getBlobContainer();

        if (RUN_JSON_TEST.equalsIgnoreCase(testName)) {
            return BinaryData.fromBytes(mergeJsonBlobs(container));
        } else if (RUN_PDF_TEST.equalsIgnoreCase(testName)) {
            return BinaryData.fromBytes(mergePdfBlobs(container));
        } else if (SETUP_TEST_DATA.equalsIgnoreCase(testName)) {
            return BinaryData.fromBytes(setupTestData(container, setupCount(count)));
        } else if (SETUP_PDF_TEST_DATA.equalsIgnoreCase(testName)) {
            return BinaryData.fromBytes(setupPdfTestData(container, setupCount(count)));
        } else if (SETUP_JSON_TEST_DATA.equalsIgnoreCase(testName)) {
            return BinaryData.fromBytes(setupJsonTestData(container, setupCount(count)));
        } else {
            throw new IllegalArgumentException("Unknown test name: " + testName);
        }
    }

    private byte[] setupTestData(BlobContainerClient container, int count) {
        setupPdfTestData(container, count);
        setupJsonTestData(container, count);

        return ("{\"pdfCount\":" + count + ",\"jsonCount\":" + count + "}")
            .getBytes(StandardCharsets.UTF_8);
    }

    private byte[] setupJsonTestData(BlobContainerClient container, int count) {
        deleteExistingBlobs(container, ".json");

        for (int i = 1; i <= count; i++) {
            byte[] content = createJsonDocument(i);
            container.getBlobClient(generatedBlobName("json", i, ".json"))
                .upload(new ByteArrayInputStream(content), content.length, true);
        }

        return setupResponse("json", count);
    }

    private byte[] setupPdfTestData(BlobContainerClient container, int count) {
        deleteExistingBlobs(container, ".pdf");

        try {
            for (int i = 1; i <= count; i++) {
                byte[] content = createPdfDocument();
                container.getBlobClient(generatedBlobName("pdf", i, ".pdf"))
                    .upload(new ByteArrayInputStream(content), content.length, true);
            }
        } catch (IOException e) {
            throw new RuntimeException("Exception occurred while creating PDF test data.", e);
        }

        return setupResponse("pdf", count);
    }

    private byte[] mergeJsonBlobs(BlobContainerClient container) {
        List<String> blobNames = listBlobNames(container, ".json", true);
        return aggregateJsonBlobsFromContainer(container, blobNames);
    }

    private byte[] mergePdfBlobs(BlobContainerClient container) {
        List<String> blobNames = listBlobNames(container, ".pdf", true);
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

    private static int setupCount(Integer count) {
        if (count == null) {
            return DEFAULT_SETUP_COUNT;
        }
        if (count <= 0) {
            throw new IllegalArgumentException("count must be greater than zero");
        }
        return count;
    }

    private String blobName(String fileName) {
        return blobPrefix + fileName;
    }

    private String generatedBlobName(String type, int index, String extension) {
        return blobName(String.format(Locale.ROOT, "generated-%s-%05d%s", type, index, extension));
    }

    private void deleteExistingBlobs(BlobContainerClient container, String extension) {
        for (String blobName : listBlobNames(container, extension, false)) {
            container.getBlobClient(blobName).deleteIfExists();
        }
    }

    private static byte[] createJsonDocument(int index) {
        return ("{\"id\":" + index + ",\"name\":\"generated-json-" + index + "\"}")
            .getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] createPdfDocument() throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (int i = 0; i < PDF_PAGE_COUNT; i++) {
                document.addPage(new PDPage());
            }
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private static byte[] setupResponse(String type, int count) {
        return ("{\"type\":\"" + type + "\",\"count\":" + count + "}").getBytes(StandardCharsets.UTF_8);
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

    private static String normaliseBlobPrefix(String blobPrefix) {
        if (blobPrefix == null || blobPrefix.isBlank()) {
            return "";
        }
        return blobPrefix.endsWith("/") ? blobPrefix : blobPrefix + "/";
    }

    private List<String> listBlobNames(BlobContainerClient container, String extension, boolean requireBlobs) {
        List<String> blobNames = StreamSupport.stream(
                container.listBlobs(new ListBlobsOptions().setPrefix(blobPrefix), null).spliterator(),
                false
            )
            .map(BlobItem::getName)
            .filter(name -> name.toLowerCase(Locale.ROOT).endsWith(extension))
            .sorted()
            .toList();

        if (requireBlobs && blobNames.isEmpty()) {
            throw new IllegalArgumentException("No " + extension + " blobs found under prefix: " + blobPrefix);
        }

        return blobNames;
    }
}
