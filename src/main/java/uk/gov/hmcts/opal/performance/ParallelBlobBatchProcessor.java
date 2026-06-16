import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class ParallelBlobBatchProcessor implements AutoCloseable {

    @FunctionalInterface
    public interface BlobListingSource {
        List<String> listBlobNames(String prefix) throws IOException;
    }

    private final BlobContentSource blobContentSource;
    private final ExecutorService executorService;


    public ParallelBlobBatchProcessor( BlobContentSource blobContentSource, int parallelism) {
        this.blobContentSource = blobContentSource;
        this.executorService = Executors.newFixedThreadPool(parallelism);
    }

    public Path mergePdfBlobsToFile(List<String> blobNames, Path outputFile) {
        writeOutput(outputFile, mergePdfBlobs(blobNames));
        return outputFile;
    }

    public byte[] mergePdfBlobs(List<String> blobNames) {
        List<byte[]> pdfDocuments = downloadAll(blobNames);
        return PdfMerger.mergeDocuments(pdfDocuments);
    }


    public Path aggregateJsonBlobsToFile(List<String> blobNames, Path outputFile) {
        writeOutput(outputFile, aggregateJsonBlobs(blobNames));
        return outputFile;
    }

    public byte[] aggregateJsonBlobs(List<String> blobNames) {
        List<byte[]> jsonDocuments = downloadAll(blobNames);

        StringBuilder builder = new StringBuilder();
        builder.append("{\"documents\":[");

        for (int i = 0; i < jsonDocuments.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(new String(jsonDocuments.get(i), StandardCharsets.UTF_8));
        }

        builder.append("]}");
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private List<byte[]> downloadAll(List<String> blobNames) {

        List<Callable<byte[]>> tasks = new ArrayList<>(blobNames.size());
        for (String blobName : blobNames) {
            final String resolvedBlobName = Objects.requireNonNull(blobName, "blobNames must not contain null entries");
            tasks.add(() -> blobContentSource.download(resolvedBlobName));
        }

        try {
            List<Future<byte[]>> futures = executorService.invokeAll(tasks);
            List<byte[]> results = new ArrayList<>(futures.size());

            for (Future<byte[]> future : futures) {
                results.add(future.get());
            }

            return results;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while downloading blob content.", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Exception occurred while downloading blob content.", e.getCause());
        }
    }

    private static void writeOutput(Path outputFile, byte[] content) {
        try {
            Path parent = outputFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(outputFile, content);
        } catch (IOException e) {
            throw new RuntimeException("Exception occurred while writing merged output to " + outputFile, e);
        }
    }

    @Override
    public void close() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
