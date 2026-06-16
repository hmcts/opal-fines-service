import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Tag("integration")
class AzureBlobPdfMergeIntegrationTest {

    private static final String DEFAULT_PDF_PREFIX = "pdf/input/";
    private static final String DEFAULT_JSON_PREFIX = "json/input/";

    @Test
    void mergesAllPdfBlobsUnderPrefixAndWritesOutputFile() throws Exception {
        BlobContainerClient containerClient = createContainerClient();
        BlobContentSource contentSource = createContentSource(containerClient);
        String prefix = System.getenv().getOrDefault("AZURE_STORAGE_PDF_PREFIX", DEFAULT_PDF_PREFIX);

        List<String> blobNames = listBlobNames(containerClient, prefix, ".pdf");

        assumeTrue(!blobNames.isEmpty(), "No PDF blobs found under prefix: " + prefix);

        int expectedPageCount = 0;
        for (String blobName : blobNames) {
            try (PDDocument document = Loader.loadPDF(contentSource.download(blobName))) {
                expectedPageCount += document.getNumberOfPages();
            }
        }

        Path output = Path.of("target/test-output/azure-merged.pdf").toAbsolutePath().normalize();
        Files.createDirectories(output.getParent());
        Files.deleteIfExists(output);

        try (ParallelBlobBatchProcessor processor = new ParallelBlobBatchProcessor(contentSource, 10)) {
            Path mergedPath = processor.mergePdfBlobsToFile(blobNames, output);

            assertEquals(output, mergedPath);
            assertTrue(Files.exists(output));
        }

        try (PDDocument mergedDocument = Loader.loadPDF(output.toFile())) {
            assertEquals(expectedPageCount, mergedDocument.getNumberOfPages());
        }
    }

    @Test
    void aggregatesAllJsonBlobsUnderPrefixAndWritesOutputFile() throws Exception {
        BlobContainerClient containerClient = createContainerClient();
        BlobContentSource contentSource = createContentSource(containerClient);
        String prefix = System.getenv().getOrDefault("AZURE_STORAGE_JSON_PREFIX", DEFAULT_JSON_PREFIX);

        List<String> blobNames = listBlobNames(containerClient, prefix, ".json");

        assumeTrue(!blobNames.isEmpty(), "No JSON blobs found under prefix: " + prefix);

        Path output = outputPath("azure-merged.json");

        try (ParallelBlobBatchProcessor processor = new ParallelBlobBatchProcessor(contentSource, 10)) {
            Path mergedPath = processor.aggregateJsonBlobsToFile(blobNames, output);

            assertEquals(output, mergedPath);
            assertTrue(Files.exists(output));
        }

        String json = Files.readString(output);
        assertTrue(json.startsWith("{\"documents\":["));
        assertTrue(json.endsWith("]}"));
        assertEquals(blobNames.size(), countTopLevelArrayElements(json));
    }

    private static BlobContainerClient createContainerClient() {
        String connectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        String containerName = System.getenv("AZURE_STORAGE_CONTAINER");

        assumeTrue(connectionString != null && !connectionString.isBlank(),
                "AZURE_STORAGE_CONNECTION_STRING must be set to run this integration test");
        assumeTrue(containerName != null && !containerName.isBlank(),
                "AZURE_STORAGE_CONTAINER must be set to run this integration test");

        return new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient();
    }

    private static BlobContentSource createContentSource(BlobContainerClient containerClient) {
        return blobName -> containerClient.getBlobClient(blobName).downloadContent().toBytes();
    }

    private static List<String> listBlobNames(BlobContainerClient containerClient, String prefix, String extension) {
        return StreamSupport.stream(
                        containerClient
                                .listBlobs(new ListBlobsOptions().setPrefix(prefix), null)
                                .spliterator(),
                        false)
                .map(BlobItem::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).endsWith(extension))
                .sorted()
                .toList();
    }

    private static Path outputPath(String fileName) throws Exception {
        Path output = Path.of("target/test-output", fileName).toAbsolutePath().normalize();
        Files.createDirectories(output.getParent());
        Files.deleteIfExists(output);
        return output;
    }

    private static int countTopLevelArrayElements(String json) {
        String marker = "\"documents\":[";
        int markerIndex = json.indexOf(marker);
        assertTrue(markerIndex >= 0, "Missing documents array in merged JSON");

        int arrayStart = json.indexOf('[', markerIndex);
        int arrayEnd = findMatchingClosingBracket(json, arrayStart);
        String arrayContent = json.substring(arrayStart + 1, arrayEnd).trim();

        if (arrayContent.isEmpty()) {
            return 0;
        }

        int count = 0;
        int depth = 0;
        boolean inString = false;
        boolean escaping = false;
        boolean sawToken = false;

        for (int i = 0; i < arrayContent.length(); i++) {
            char ch = arrayContent.charAt(i);

            if (inString) {
                if (escaping) {
                    escaping = false;
                } else if (ch == '\\') {
                    escaping = true;
                } else if (ch == '"') {
                    inString = false;
                }
                continue;
            }

            if (Character.isWhitespace(ch)) {
                continue;
            }

            sawToken = true;

            if (ch == '"') {
                inString = true;
            } else if (ch == '{' || ch == '[') {
                depth++;
            } else if (ch == '}' || ch == ']') {
                depth--;
            } else if (ch == ',' && depth == 0) {
                count++;
            }
        }

        return sawToken ? count + 1 : 0;
    }

    private static int findMatchingClosingBracket(String text, int startIndex) {
        int depth = 0;
        boolean inString = false;
        boolean escaping = false;

        for (int i = startIndex; i < text.length(); i++) {
            char ch = text.charAt(i);

            if (inString) {
                if (escaping) {
                    escaping = false;
                } else if (ch == '\\') {
                    escaping = true;
                } else if (ch == '"') {
                    inString = false;
                }
                continue;
            }

            if (ch == '"') {
                inString = true;
            } else if (ch == '[') {
                depth++;
            } else if (ch == ']') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }

        throw new IllegalArgumentException("Unbalanced JSON array in merged output");
    }
}
