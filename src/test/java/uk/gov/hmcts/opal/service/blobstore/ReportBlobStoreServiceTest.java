package uk.gov.hmcts.opal.service.blobstore;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.util.UuidProvider;

@ExtendWith(MockitoExtension.class)
class ReportBlobStoreServiceTest {

    public static final String CONTAINER = "container";
    private final String message = "I am a report";

    @Mock
    private BlobServiceClient blobServiceClient;

    @Mock
    private BlobContainerClient container;

    @Mock
    private BlobClient blob;

    @Mock
    private UuidProvider uuidProvider;

    private ReportBlobStoreService reportBlobStoreService;

    private UUID uuid;

    @BeforeEach
    void setUp() {
        reportBlobStoreService = new ReportBlobStoreService(blobServiceClient, uuidProvider, CONTAINER);
        when(blobServiceClient.getBlobContainerClient(CONTAINER)).thenReturn(container);
        uuid = UUID.randomUUID();
    }

    @Test
    void storeReport() throws IOException {
        //Arrange
        when(container.getBlobClient(anyString())).thenReturn(blob);
        when(uuidProvider.getUuid()).thenReturn(uuid);
        when(container.exists()).thenReturn(true);
        //Act
        UUID savedAt = reportBlobStoreService.storeReport(inputStream(message));
        //Assert
        ArgumentCaptor<InputStream> argument = ArgumentCaptor.forClass(InputStream.class);
        verify(blob).upload(argument.capture());
        String saved = new String(argument.getValue().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(message, saved);
        assertEquals(uuid, savedAt);
    }

    @Test
    void storeReport_containerDoesNotExist_throwError() {
        when(container.exists()).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> reportBlobStoreService.storeReport(inputStream(message)));
    }

    @Nested
    class GetReport {

        private static final UUID LOCATION = UUID.fromString("00000000-0000-0000-0000-000000000005");

        @Test
        void whenBlobExists_returnsContent_happyPath() {
            byte[] reportBytes = message.getBytes(StandardCharsets.UTF_8);
            when(container.getBlobClient(LOCATION.toString())).thenReturn(blob);
            doAnswer(invocation -> {
                OutputStream outputStream = invocation.getArgument(0);
                outputStream.write(reportBytes);
                return null;
            }).when(blob).downloadStream(any(OutputStream.class));

            byte[] result = reportBlobStoreService.getReport(LOCATION);

            assertAll(
                () -> assertEquals(message, new String(result, StandardCharsets.UTF_8)),
                () -> verify(container).getBlobClient(LOCATION.toString()),
                () -> verify(blob).downloadStream(any(OutputStream.class))
            );
        }

        @Test
        void whenBlobDownloadFails_throwsUncheckedIoException_sadPath() {
            IOException cause = new IOException("download failed");
            when(container.getBlobClient(LOCATION.toString())).thenReturn(blob);
            doAnswer(invocation -> {
                throw cause;
            }).when(blob).downloadStream(any(OutputStream.class));

            UncheckedIOException exception = assertThrows(
                UncheckedIOException.class,
                () -> reportBlobStoreService.getReport(LOCATION)
            );

            assertAll(
                () -> assertEquals("Failed to read report from blob store at: " + LOCATION, exception.getMessage()),
                () -> assertEquals(cause, exception.getCause())
            );
        }
    }

    private static InputStream inputStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Nested
    class DeleteReport {

        private static final UUID LOCATION = UUID.fromString("00000000-0000-0000-0000-000000000006");

        @Test
        void whenBlobExists_deletesBlob_happyPath() {
            when(container.getBlobClient(LOCATION.toString())).thenReturn(blob);

            reportBlobStoreService.deleteReport(LOCATION);

            assertAll(
                () -> verify(container).getBlobClient(LOCATION.toString()),
                () -> verify(blob).deleteIfExists()
            );
        }
    }
}
