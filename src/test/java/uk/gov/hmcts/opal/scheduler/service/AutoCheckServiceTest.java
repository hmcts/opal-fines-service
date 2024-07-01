package uk.gov.hmcts.opal.scheduler.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.sftp.SftpInboundService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.opal.sftp.SftpLocation.AUTO_CHEQUES;

@ExtendWith(MockitoExtension.class)
class AutoCheckServiceTest {

    @Mock
    private SftpInboundService sftpInboundService;

    private AutoCheckService autoCheckService;

    @BeforeEach
    void setUp() {
        autoCheckService = new AutoCheckService(sftpInboundService);
    }

    @Test
    void testProcess() {
        String fileName = "test.txt";
        autoCheckService.process(fileName);
        verify(sftpInboundService).downloadFile(eq(AUTO_CHEQUES.getPath()), eq(fileName), any());
    }

    @Test
    void testProcessFile() {
        String fileContents = "Test file contents";
        InputStream inputStream = new ByteArrayInputStream(fileContents.getBytes());
        autoCheckService.processFile(inputStream);
    }
}
