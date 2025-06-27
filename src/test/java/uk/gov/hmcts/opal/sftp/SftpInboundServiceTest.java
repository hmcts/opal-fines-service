package uk.gov.hmcts.opal.sftp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.sftp.config.SftpConnection;

import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {SftpInboundService.class})
class SftpInboundServiceTest {

    // Mocks
    @MockitoBean
    DefaultSftpSessionFactory inboundSessionFactory;

    @MockitoBean
    SftpService sftpService;

    @MockitoBean
    SftpConnection inboundConnection;

    @Autowired
    SftpInboundService sftpInboundService;


    @Test
    void testUploadFile() {
        byte[] fileBytes = {/* Some byte array */};
        String path = "test/path";
        String fileName = "test.txt";

        sftpInboundService.uploadFile(fileBytes, path, fileName);

        verify(sftpService).uploadFile(inboundSessionFactory, fileBytes, path, fileName);
    }

    @Test
    void testDownloadFile() {
        String path = "test/path";
        String fileName = "test.txt";

        Consumer<InputStream> fileProcessor = inputStream -> {
        };
        sftpInboundService.downloadFile(path, fileName, fileProcessor);

        verify(sftpService).downloadFile(inboundSessionFactory, path, fileName, fileProcessor);
    }

    @Test
    void testDeleteFile() {
        String path = "test/path";
        String fileName = "test.txt";

        when(sftpService.deleteFile(inboundSessionFactory, path, fileName)).thenReturn(true);

        boolean result = sftpInboundService.deleteFile(path, fileName);

        verify(sftpService).deleteFile(inboundSessionFactory, path, fileName);

        assertTrue(result);
    }

    @Test
    void testCreateSftpLocationsWhenCreateSubLocationsIsTrue() {

        when(inboundConnection.isCreateSubLocations()).thenReturn(true);

        List<SftpLocation> inboundLocations = SftpLocation.getInboundLocations();

        doNothing().when(sftpService).createDirectoryIfNotExists(any(), any());

        sftpInboundService.createSftpLocations();

        for (SftpLocation inboundLocation : inboundLocations) {
            verify(sftpService).createDirectoryIfNotExists(inboundSessionFactory, inboundLocation);
        }
    }

    @Test
    void testCreateSftpLocationsWhenCreateSubLocationsIsFalse() {
        when(inboundConnection.isCreateSubLocations()).thenReturn(false);

        doNothing().when(sftpService).createDirectoryIfNotExists(any(), any());

        sftpInboundService.createSftpLocations();

        verify(sftpService, never()).createDirectoryIfNotExists(any(), any());
    }
}
