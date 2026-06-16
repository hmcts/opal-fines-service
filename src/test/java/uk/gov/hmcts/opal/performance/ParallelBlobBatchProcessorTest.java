import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParallelBlobBatchProcessorTest {

    @Test
    void mergesHundredPdfBlobsFromBlobPrefixUsingTenThreadsAndWritesOutputFile() throws Exception {
        Map<String, byte[]> blobs = new HashMap<>();
        List<String> blobNames = java.util.stream.IntStream.range(0, 100)
                .mapToObj(i -> "pdf/input/" + i + ".pdf")
                .toList();

        byte[] templatePdf = createPdf();
        int templatePageCount;
        try (PDDocument document = Loader.loadPDF(templatePdf)) {
            templatePageCount = document.getNumberOfPages();
        }

        for (String blobName : blobNames) {
            blobs.put(blobName, templatePdf.clone());
        }

        Path output = Path.of("src/test/resources/pdf/output/merged.pdf").toAbsolutePath().normalize();
        Files.createDirectories(output.getParent());
        Files.deleteIfExists(output);

        try (ParallelBlobBatchProcessor processor =
                     new ParallelBlobBatchProcessor( blobs::get, 10)) {
            Path mergedPath = processor.mergePdfBlobsToFile(blobNames, output);

            assertEquals(output, mergedPath);
            assertTrue(Files.exists(output));

            try (PDDocument document = Loader.loadPDF(output.toFile())) {
                assertEquals(templatePageCount * 100, document.getNumberOfPages());
            }
        }
    }

    @Test
    void aggregatesFiveHundredJsonBlobsIntoSingleJsonObjectAndWritesOutputFile() throws Exception {
        Map<String, byte[]> blobs = new HashMap<>();
        List<String> blobNames = java.util.stream.IntStream.range(0, 500)
                .mapToObj(i -> "json-" + i + ".json")
                .toList();

        for (int i = 0; i < blobNames.size(); i++) {
            String blobName = blobNames.get(i);
            blobs.put(blobName, ("{\"id\":\"" + blobName + "\",\"index\":" + i + "}").getBytes(StandardCharsets.UTF_8));
        }

        Path output = Path.of("src/test/resources/pdf/output/merged.json").toAbsolutePath().normalize();
        Files.createDirectories(output.getParent());
        Files.deleteIfExists(output);

        try (ParallelBlobBatchProcessor processor = new ParallelBlobBatchProcessor(blobs::get, 10)) {
            Path mergedPath = processor.aggregateJsonBlobsToFile(blobNames, output);

            assertEquals(output, mergedPath);
            assertTrue(Files.exists(output));

            String json = Files.readString(output, StandardCharsets.UTF_8);
            assertTrue(json.startsWith("{\"documents\":["));
            assertTrue(json.endsWith("]}"));
            assertEquals(500, countOccurrences(json));
        }
    }

    private static byte[] createPdf() throws IOException {
        try (InputStream inputStream = ParallelBlobBatchProcessorTest.class.getClassLoader()
                .getResourceAsStream("pdf/input/AEOFD Attachment of Earnings Order - Employer_BIL.pdf")) {

            assert inputStream != null;
            return inputStream.readAllBytes();
        }
    }

    private static int countOccurrences(String text) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf("\"id\"", index)) >= 0) {
            count++;
            index += "\"id\"".length();
        }
        return count;
    }
}
