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

@SpringBootTest(classes = {SftpOutboundService.class})
class SftpOutboundServiceTest {

    // Mocks
    @MockitoBean
    DefaultSftpSessionFactory outboundSessionFactory;

    @MockitoBean
    SftpService sftpService;

    @MockitoBean
    SftpConnection outboundConnection;

    @Autowired
    SftpOutboundService sftpOutboundService;


    @Test
    void testUploadFile() {
        byte[] fileBytes = {/* Some byte array */};
        String path = "test/path";
        String fileName = "test.txt";

        sftpOutboundService.uploadFile(fileBytes, path, fileName);

        verify(sftpService).uploadFile(outboundSessionFactory, fileBytes, path, fileName);
    }

    @Test
    void testDownloadFile() {
        String path = "test/path";
        String fileName = "test.txt";

        Consumer<InputStream> fileProcessor = inputStream -> {
        };
        sftpOutboundService.downloadFile(path, fileName, fileProcessor);

        verify(sftpService).downloadFile(outboundSessionFactory, path, fileName, fileProcessor);
    }

    @Test
    void testDeleteFile() {
        String path = "test/path";
        String fileName = "test.txt";

        when(sftpService.deleteFile(outboundSessionFactory, path, fileName)).thenReturn(true);

        boolean result = sftpOutboundService.deleteFile(path, fileName);

        verify(sftpService).deleteFile(outboundSessionFactory, path, fileName);

        assertTrue(result);
    }

    @Test
    void testCreateSftpLocationsWhenCreateSubLocationsIsTrue() {

        when(outboundConnection.isCreateSubLocations()).thenReturn(true);

        List<SftpLocation> outboundLocations = SftpLocation.getOutboundLocations();

        doNothing().when(sftpService).createDirectoryIfNotExists(any(), any());

        sftpOutboundService.createSftpLocations();

        for (SftpLocation location : outboundLocations) {
            verify(sftpService).createDirectoryIfNotExists(outboundSessionFactory, location);
        }
    }

    @Test
    void testCreateSftpLocationsWhenCreateSubLocationsIsFalse() {
        when(outboundConnection.isCreateSubLocations()).thenReturn(false);

        doNothing().when(sftpService).createDirectoryIfNotExists(any(), any());

        sftpOutboundService.createSftpLocations();

        verify(sftpService, never()).createDirectoryIfNotExists(any(), any());
    }
}
