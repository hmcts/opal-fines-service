package uk.gov.hmcts.opal.service.blobstore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.util.UuidProvider;

@ExtendWith(MockitoExtension.class)
public class ReportBlobStoreServiceTest {

    private final String MESSAGE = "I am a report";
    private final String CONTAINER_NAME = "container";

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
    public void setUp() {
        reportBlobStoreService = new ReportBlobStoreService(blobServiceClient, uuidProvider, CONTAINER_NAME);
        uuid = UUID.randomUUID();
        when(blobServiceClient.getBlobContainerClient(CONTAINER_NAME)).thenReturn(container);
        when(container.getBlobClient(anyString())).thenReturn(blob);
    }

    @Test
    public void storeReport() {
        //Arrange
        when(uuidProvider.getUuid()).thenReturn(uuid);
        when(container.exists()).thenReturn(true);
        when(blob.exists()).thenReturn(false);
        //Act
        String savedAt = reportBlobStoreService.storeReport(MESSAGE);
        //Assert
        ArgumentCaptor<ByteArrayInputStream> argument = ArgumentCaptor.forClass(ByteArrayInputStream.class);
        verify(blob).upload(argument.capture(), anyLong());
        String saved = new String(argument.getValue().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(MESSAGE, saved);
        assertEquals(savedAt, uuid.toString());
    }

    @Test
    public void storeReport_locationExists_generateNewLocation() {
        //Arrange
        UUID uuid2 = UUID.randomUUID();
        when(uuidProvider.getUuid()).thenReturn(uuid, uuid2);
        when(container.exists()).thenReturn(true);
        when(blob.exists()).thenReturn(true, false);
        //Act
        String savedAt = reportBlobStoreService.storeReport(MESSAGE);
        //Assert
        ArgumentCaptor<ByteArrayInputStream> argument = ArgumentCaptor.forClass(ByteArrayInputStream.class);
        verify(blob).upload(argument.capture(), anyLong());
        String saved = new String(argument.getValue().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(MESSAGE, saved);
        assertEquals(savedAt, uuid2.toString());
    }

}