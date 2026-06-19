package uk.gov.hmcts.opal;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j(topic = "opal.TestService")
public class TestService {

    private static final String SETUP_TEST_DATA = "setupTestData";
    private static final String SETUP_JSON_TEST_DATA = "setupJsonTestData";
    private static final String SETUP_PDF_TEST_DATA = "setupPdfTestData";
    private static final String RUN_JSON_TEST = "runJsonTest";
    private static final String RUN_PDF_TEST = "runPdfTest";
    private static final String MERGED_JSON_FILE_NAME = "merged-output.json";
    private static final String MERGED_PDF_FILE_NAME = "merged-output.pdf";
    private static final String SAMPLE_JSON_DOCUMENT_RESOURCE = "sampleFiles/sample.json";
    private static final String SAMPLE_PDF_DOCUMENT_RESOURCE = "sampleFiles/sample.pdf";
    private static final int DEFAULT_SETUP_COUNT = 100;

    private final BlobServiceClient blobServiceClient;
    private final String containerName;

    public TestService(BlobServiceClient blobServiceClient,
                       @Value("${opal.report.storage.test-container:testcontainer}") String containerName) {
        this.blobServiceClient = blobServiceClient;
        this.containerName = containerName;
    }

    public BinaryData runTest(String testName) {
        return runTest(testName, null);
    }

    public BinaryData runTest(String testName, Integer count) {
        BlobContainerClient container = getBlobContainer();

        if (SETUP_TEST_DATA.equalsIgnoreCase(testName)) {
            return BinaryData.fromBytes(setupTestData(container, setupCount(count)));
        }
        if (SETUP_JSON_TEST_DATA.equalsIgnoreCase(testName)) {
            return BinaryData.fromBytes(setupJsonTestData(container, setupCount(count)));
        }
        if (SETUP_PDF_TEST_DATA.equalsIgnoreCase(testName)) {
            return BinaryData.fromBytes(setupPdfTestData(container, setupCount(count)));
        }
        if (RUN_JSON_TEST.equalsIgnoreCase(testName)) {
            return BinaryData.fromBytes(mergeJsonBlobs(container));
        }
        if (RUN_PDF_TEST.equalsIgnoreCase(testName)) {
            return BinaryData.fromBytes(mergePdfBlobs(container));
        }

        throw new IllegalArgumentException("Unknown test name: " + testName);
    }

    private byte[] setupTestData(BlobContainerClient container, int count) {
        TimedActivity activity = startActivity(
            "Setup Test Data",
            "jsonCount=" + count + ", pdfCount=" + count
        );
        setupJsonTestData(container, count);
        setupPdfTestData(container, count);
        completeActivity(
            activity,
            "jsonCount=" + count + ", pdfCount=" + count
        );

        return ("{\"jsonCount\":" + count + ",\"pdfCount\":" + count + "}")
            .getBytes(StandardCharsets.UTF_8);
    }

    private byte[] setupJsonTestData(BlobContainerClient container, int count) {
        TimedActivity activity = startActivity(
            "Setup JSON Test Blobs",
            "count=" + count
        );

        try {
            byte[] content = loadSampleJsonDocument();
            TimedActivity uploadActivity = startActivity(
                "Upload JSON Test Blobs",
                "count=" + count + ", bytesPerBlob=" + content.length
            );
            uploadGeneratedBlobs(container, "json", ".json", count, content);
            completeActivity(
                uploadActivity,
                "count=" + count + ", bytesPerBlob=" + content.length
            );
        } catch (IOException e) {
            throw new RuntimeException("Exception occurred while loading JSON test data.", e);
        }

        completeActivity(activity, "count=" + count);
        return setupResponse("json", count);
    }

    private byte[] setupPdfTestData(BlobContainerClient container, int count) {
        TimedActivity activity = startActivity(
            "Setup PDF Test Blobs",
            "count=" + count
        );

        try {
            byte[] content = loadSamplePdfDocument();
            TimedActivity uploadActivity = startActivity(
                "Upload PDF Test Blobs",
                "count=" + count + ", bytesPerBlob=" + content.length
            );
            uploadGeneratedBlobs(container, "pdf", ".pdf", count, content);
            completeActivity(
                uploadActivity,
                "count=" + count + ", bytesPerBlob=" + content.length
            );
        } catch (IOException e) {
            throw new RuntimeException("Exception occurred while loading PDF test data.", e);
        }

        completeActivity(activity, "count=" + count);
        return setupResponse("pdf", count);
    }

    private byte[] mergeJsonBlobs(BlobContainerClient container) {
        List<String> blobNames = listBlobNames(container, ".json", true);
        log.info("Found JSON blobs to merge. count={}", blobNames.size());
        byte[] mergedJson = aggregateJsonBlobsFromContainer(container, blobNames);
        uploadMergedBlob(container, MERGED_JSON_FILE_NAME, mergedJson);
        return mergedJson;
    }

    private byte[] mergePdfBlobs(BlobContainerClient container) {
        List<String> blobNames = listBlobNames(container, ".pdf", true);
        log.info("Found PDF blobs to merge. count={}", blobNames.size());
        try {
            byte[] mergedPdf = mergePdfBlobsFromContainer(container, blobNames);
            uploadMergedBlob(container, MERGED_PDF_FILE_NAME, mergedPdf);
            return mergedPdf;
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

    private static byte[] loadSampleJsonDocument() throws IOException {
        return loadSampleDocument(SAMPLE_JSON_DOCUMENT_RESOURCE, "loading sample jsons");
    }

    private static byte[] loadSamplePdfDocument() throws IOException {
        return loadSampleDocument(SAMPLE_PDF_DOCUMENT_RESOURCE, "loading sample pdfs");
    }

    private static byte[] loadSampleDocument(String sampleDocumentResource, String details) throws IOException {
        TimedActivity activity = startActivity("Load Sample Document", details);
        try (InputStream inputStream = TestService.class.getClassLoader().getResourceAsStream(sampleDocumentResource)) {
            if (inputStream == null) {
                throw new IOException("Sample document not found: " + sampleDocumentResource);
            }
            byte[] content = inputStream.readAllBytes();
            completeActivity(activity, details);
            return content;
        }
    }

    private static void uploadGeneratedBlobs(BlobContainerClient container, String type, String extension, int count,
                                             byte[] content) {
        for (int i = 1; i <= count; i++) {
            String blobName = generatedBlobName(type, i, extension);
            container.getBlobClient(blobName)
                .upload(new ByteArrayInputStream(content), content.length, true);
        }
    }

    private static String generatedBlobName(String type, int index, String extension) {
        return String.format(Locale.ROOT, "generated-%s-%05d%s", type, index, extension);
    }

    private static byte[] setupResponse(String type, int count) {
        return ("{\"type\":\"" + type + "\",\"count\":" + count + "}").getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] aggregateJsonBlobsFromContainer(BlobContainerClient container, List<String> blobNames) {
        List<byte[]> jsonDocuments = new ArrayList<>(blobNames.size());
        TimedActivity downloadActivity = startActivity("Download JSON Blobs", "blobCount=" + blobNames.size());
        for (String blobName : blobNames) {
            try (ByteArrayOutputStream blobOutputStream = new ByteArrayOutputStream()) {
                container.getBlobClient(blobName).downloadStream(blobOutputStream);
                jsonDocuments.add(blobOutputStream.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException("Exception occurred while downloading JSON files.", e);
            }
        }
        completeActivity(downloadActivity, "blobCount=" + blobNames.size());

        TimedActivity mergeActivity = startActivity("Merge JSON Blobs", "blobCount=" + jsonDocuments.size());
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.writeBytes("{\"documents\":[".getBytes(StandardCharsets.UTF_8));

            for (int i = 0; i < jsonDocuments.size(); i++) {
                if (i > 0) {
                    outputStream.write(',');
                }
                outputStream.writeBytes(jsonDocuments.get(i));
            }

            outputStream.writeBytes("]}".getBytes(StandardCharsets.UTF_8));
            byte[] mergedJson = outputStream.toByteArray();
            completeActivity(
                mergeActivity,
                "blobCount=" + jsonDocuments.size() + ", outputBytes=" + mergedJson.length
            );
            return mergedJson;
        } catch (IOException e) {
            throw new RuntimeException("Exception occurred while merging JSON files.", e);
        }
    }

    private static byte[] mergePdfBlobsFromContainer(BlobContainerClient container, List<String> blobNames)
        throws IOException {
        Path tempDirectory = Files.createTempDirectory("opal-pdf-merge-");
        TimedActivity downloadActivity = startActivity(
            "Download PDF Blobs",
            "blobCount=" + blobNames.size() + ", tempDirectory=" + tempDirectory
        );

        try {
            List<Path> inputFiles = new ArrayList<>(blobNames.size());
            for (int i = 0; i < blobNames.size(); i++) {
                String blobName = blobNames.get(i);
                Path inputFile = tempDirectory.resolve("input-" + i + ".pdf");
                try (OutputStream outputStream = Files.newOutputStream(inputFile)) {
                    container.getBlobClient(blobName).downloadStream(outputStream);
                }
                inputFiles.add(inputFile);
            }
            completeActivity(
                downloadActivity,
                "blobCount=" + blobNames.size() + ", tempDirectory=" + tempDirectory
            );

            TimedActivity mergeActivity = startActivity("Merge PDF Blobs", "blobCount=" + inputFiles.size());
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                PDFMergerUtility merger = new PDFMergerUtility();
                for (Path inputFile : inputFiles) {
                    merger.addSource(inputFile.toFile());
                }
                merger.setDestinationStream(outputStream);
                merger.mergeDocuments(IOUtils.createTempFileOnlyStreamCache());
                byte[] mergedPdf = outputStream.toByteArray();
                completeActivity(
                    mergeActivity,
                    "blobCount=" + inputFiles.size() + ", outputBytes=" + mergedPdf.length
                );
                return mergedPdf;
            }
        } finally {
            log.debug("Deleting temporary PDF merge directory: {}", tempDirectory);
            deleteDirectory(tempDirectory);
        }
    }

    private static void uploadMergedBlob(BlobContainerClient container, String fileName, byte[] content) {
        TimedActivity activity = startActivity(
            "Upload Merged Output Blob",
            "name=" + fileName + ", bytes=" + content.length
        );
        container.getBlobClient(fileName)
            .upload(new ByteArrayInputStream(content), content.length, true);
        completeActivity(
            activity,
            "name=" + fileName + ", bytes=" + content.length
        );
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

    private static TimedActivity startActivity(String description, String details) {
        Instant startedAt = Instant.now();
        log.info("Activity started. Description={}, timestamp={}, details={}", description, startedAt, details);
        return new TimedActivity(description, startedAt, System.nanoTime());
    }

    private static void completeActivity(TimedActivity activity, String details) {
        Instant completedAt = Instant.now();
        long durationMs = Duration.ofNanos(System.nanoTime() - activity.startedNanos).toMillis();
        log.info(
            "Activity completed. Description={}, startTimestamp={}, endTimestamp={}, durationMs={}, details={}",
            activity.description,
            activity.startedAt,
            completedAt,
            durationMs,
            details
        );
    }

    private static class TimedActivity {
        private final String description;
        private final Instant startedAt;
        private final long startedNanos;

        private TimedActivity(String description, Instant startedAt, long startedNanos) {
            this.description = description;
            this.startedAt = startedAt;
            this.startedNanos = startedNanos;
        }
    }

    private static List<String> listBlobNames(BlobContainerClient container, String extension, boolean requireBlobs) {
        List<String> blobNames = StreamSupport.stream(
                container.listBlobs().spliterator(),
                false
            )
            .map(BlobItem::getName)
            .filter(name -> !name.contains("/"))
            .filter(name -> name.toLowerCase(Locale.ROOT).endsWith(extension))
            .filter(name -> !MERGED_JSON_FILE_NAME.equals(name))
            .filter(name -> !MERGED_PDF_FILE_NAME.equals(name))
            .sorted()
            .toList();

        if (requireBlobs && blobNames.isEmpty()) {
            throw new IllegalArgumentException("No root-level " + extension + " blobs found");
        }

        return blobNames;
    }
}
