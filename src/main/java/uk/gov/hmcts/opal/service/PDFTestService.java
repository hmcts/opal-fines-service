package uk.gov.hmcts.opal.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.PDFMergerUtility.DocumentMergeMode;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PDFTestService {

    private static final String PDF_CLASSPATH_PATTERN = "classpath*:/pdf/*.pdf";
    private static final String MERGED_FILE_NAME = "merged.pdf";
    private final ResourcePatternResolver resourcePatternResolver;

    public PDFTestService(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public void run(int maxPdfsToMerge, OutputStream destinationOutputStream) {
        if (maxPdfsToMerge <= 0) {
            throw new IllegalArgumentException("maxPdfsToMerge must be greater than zero");
        }
        Objects.requireNonNull(destinationOutputStream, "destinationOutputStream must not be null");

        PDFMergerUtility merger = new PDFMergerUtility();
        merger.setDocumentMergeMode(DocumentMergeMode.OPTIMIZE_RESOURCES_MODE);
        merger.setDestinationStream(destinationOutputStream);

        List<Resource> pdfResources = getMergeablePdfResources(maxPdfsToMerge);
        if (pdfResources.isEmpty()) {
            throw new IllegalStateException("No source PDFs found under classpath folder: /pdf");
        }

        List<Path> tempFiles = new ArrayList<>();
        try {
            for (Resource pdfResource : pdfResources) {
                addResourceAsSource(merger, pdfResource, tempFiles);
            }

            // Uses disk-backed buffering to avoid loading entire merged content in heap memory.
            Instant start = Instant.now();
            log.info("TMP: Merge start");
            merger.mergeDocuments(IOUtils.createTempFileOnlyStreamCache());
            Instant finish = Instant.now();
            long timeElapsed = Duration.between(start, finish).toMillis();
            log.info("TMP: Merge End: Duration: " + timeElapsed + " ms");
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to merge PDFs from classpath folder: /pdf", e);
        } finally {
            deleteTempFiles(tempFiles);
        }
    }

    public void run(OutputStream destinationOutputStream) {
        run(Integer.MAX_VALUE, destinationOutputStream);
    }

    private List<Resource> getMergeablePdfResources(int maxPdfsToMerge) {
        try {
            return Arrays.stream(resourcePatternResolver.getResources(PDF_CLASSPATH_PATTERN))
                .filter(Resource::isReadable)
                .filter(resource -> {
                    String filename = resource.getFilename();
                    return filename != null
                        && !filename.equalsIgnoreCase(MERGED_FILE_NAME)
                        && !filename.startsWith("!");
                })
                .sorted(Comparator.comparing(resource -> resource.getFilename().toLowerCase()))
                .filter(this::hasContent)
                .limit(maxPdfsToMerge)
                .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load PDFs from classpath folder: /pdf", e);
        }
    }

    private boolean hasContent(Resource resource) {
        try {
            return resource.contentLength() > 0;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to inspect PDF resource: " + resource.getDescription(), e);
        }
    }

    private void addResourceAsSource(PDFMergerUtility merger, Resource resource, List<Path> tempFiles)
        throws IOException {
        if (resource.isFile()) {
            merger.addSource(resource.getFile());
            return;
        }

        // In packaged deployments classpath resources live in the JAR, so copy each one to temp storage first.
        Path tempPdfFile = Files.createTempFile("pdf-merge-source-", ".pdf");
        tempFiles.add(tempPdfFile);
        try (InputStream inputStream = resource.getInputStream()) {
            Files.copy(inputStream, tempPdfFile, StandardCopyOption.REPLACE_EXISTING);
        }
        merger.addSource(tempPdfFile.toFile());
    }

    private void deleteTempFiles(List<Path> tempFiles) {
        for (Path tempFile : tempFiles) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
                // No-op: temp file cleanup failure should not fail the request.
            }
        }
    }

    public void fetch(String fileName, OutputStream outputStream) {
        Objects.requireNonNull(fileName, "fileName must not be null");
        Objects.requireNonNull(outputStream, "outputStream must not be null");

        Path requestedPath = Path.of(fileName);
        if (requestedPath.isAbsolute() || requestedPath.getNameCount() != 1) {
            throw new IllegalArgumentException("fileName must be a single filename without path segments");
        }

        String normalizedFileName = requestedPath.getFileName().toString();
        Resource resource = resourcePatternResolver.getResource("classpath:/pdf/" + normalizedFileName);
        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalStateException("PDF file does not exist or is not readable: " + normalizedFileName);
        }

        try (InputStream inputStream = resource.getInputStream()) {
            inputStream.transferTo(outputStream);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to stream PDF file from classpath folder: /pdf/"
                + normalizedFileName, e);
        }
    }
}
