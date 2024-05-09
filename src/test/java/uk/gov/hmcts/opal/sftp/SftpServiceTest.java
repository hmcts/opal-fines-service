package uk.gov.hmcts.opal.sftp;

import lombok.SneakyThrows;
import org.apache.sshd.sftp.client.SftpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpSession;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.sftp.SftpLocation.AUTO_CHEQUES;

@ExtendWith(MockitoExtension.class)
class SftpServiceTest {

    @InjectMocks
    private SftpService sftpService;

    @Mock
    DefaultSftpSessionFactory sessionFactory;

    @Mock
    SftpSession sftpSession;

    @Test
    @SneakyThrows
    void testUploadFile() {
        byte[] fileBytes = "Test file content".getBytes();
        String path = "/test";
        String fileName = "test.txt";

        when(sessionFactory.getSession()).thenReturn(sftpSession);

        sftpService.uploadFile(sessionFactory, fileBytes, path, fileName);
        verify(sftpSession).write(any(InputStream.class), eq(path + "/" + fileName));
    }

    @Test
    void testDownloadFile() {
        String path = "/test";
        String fileName = "test.txt";

        when(sessionFactory.getSession()).thenReturn(sftpSession);
        when(sftpSession.finalizeRaw()).thenReturn(true);

        boolean result = sftpService.downloadFile(sessionFactory, path, fileName, inputStream -> {
        });

        assertTrue(result);
    }

    @Test
    @SneakyThrows
    void testDeleteFile() {
        String path = "/test";
        String fileName = "test.txt";

        when(sessionFactory.getSession()).thenReturn(sftpSession);
        when(sftpSession.remove(anyString())).thenReturn(true);
        assertTrue(sftpService.deleteFile(sessionFactory, path, fileName));
    }

    @Test
    void testCreateDirectoryIfNotExists() throws IOException {

        when(sessionFactory.getSession()).thenReturn(sftpSession);
        when(sftpSession.list(any())).thenReturn(new SftpClient.DirEntry[]{});
        when(sftpSession.mkdir(anyString())).thenReturn(true);

        sftpService.createDirectoryIfNotExists(sessionFactory, AUTO_CHEQUES);

        verify(sftpSession).mkdir(AUTO_CHEQUES.getPath());
    }

    @Test
    void testDirectoryExists() throws IOException {
        String path = "/non-existent";

        when(sessionFactory.getSession()).thenReturn(sftpSession);
        when(sftpSession.list(any())).thenReturn(new SftpClient.DirEntry[]{});

        assertFalse(sftpService.directoryExists(sessionFactory, path));
    }
}
