import java.io.IOException;

@FunctionalInterface
public interface BlobContentSource {

    byte[] download(String blobName) throws IOException;
}
