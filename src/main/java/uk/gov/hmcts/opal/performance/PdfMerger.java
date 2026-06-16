import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class PdfMerger {

    private PdfMerger() {
    }

    public static void mergeDocuments(Path inputDirectory, Path outputFile, int repeatCount) {
        try {
            List<Path> inputFiles;
            try (Stream<Path> stream = Files.list(inputDirectory)) {
                inputFiles = stream
                       .filter(path -> path.toString().toLowerCase().endsWith(".pdf"))
                       .toList();
            }

            PDFMergerUtility merger = new PDFMergerUtility();

            for (int i = 0; i < repeatCount; i++) {
                for (Path inputFile : inputFiles) {
                    merger.addSource(inputFile.toFile());
                }
            }

            merger.setDestinationFileName(outputFile.toAbsolutePath().toString());
            merger.mergeDocuments(IOUtils.createTempFileOnlyStreamCache());

        } catch (IOException e) {
            throw new RuntimeException("Exception occurred while merging PDF files. ", e);
        }
    }

    public static byte[] mergeDocuments(List<byte[]> documents) {
        List<RandomAccessRead> inputStreams = new ArrayList<>(documents.size());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (byte[] document : documents) {
                if (document == null) {
                    throw new IllegalArgumentException("documents must not contain null entries");
                }
                inputStreams.add(new RandomAccessReadBuffer(document));
            }

            PDFMergerUtility merger = new PDFMergerUtility();
            merger.addSources(inputStreams);
            merger.setDestinationStream(outputStream);
            merger.mergeDocuments(IOUtils.createTempFileOnlyStreamCache());
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Exception occurred while merging PDF files. " + e);
        } finally {
            for (RandomAccessRead inputStream : inputStreams) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                    // Best-effort close for in-memory PDF sources.
                }
            }
        }
    }
}
