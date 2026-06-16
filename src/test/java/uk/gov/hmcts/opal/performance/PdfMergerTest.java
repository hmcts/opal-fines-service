import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class PdfMergerTest {

    @Test
    void mergesPhysicalFilesFromDirectory() throws Exception {

        int repeatCounter = 2000;
        Path inputDirectory = Path.of(Objects.requireNonNull(
                getClass().getClassLoader().getResource("pdf/input")
        ).toURI());
        Path output = Path.of("target/test-output/merged.pdf").toAbsolutePath().normalize();

        Files.createDirectories(output.getParent());
        Files.deleteIfExists(output);

        PdfMerger.mergeDocuments(inputDirectory, output, repeatCounter);

        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);

        try (PDDocument doc = Loader.loadPDF(output.toFile())) {
            assertEquals(10 * repeatCounter, doc.getNumberOfPages());
        }

    }
}
