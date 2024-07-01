package uk.gov.hmcts.opal.scheduler.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.sftp.SftpOutboundService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.opal.sftp.SftpLocation.ALL_PAY_ARCHIVE;

@ExtendWith(MockitoExtension.class)
class AllPayArchiveServiceTest {

    @Mock
    private SftpOutboundService sftpOutboundService;

    private AllPayArchiveService allPayArchiveService;

    @BeforeEach
    void setUp() {
        allPayArchiveService = new AllPayArchiveService(sftpOutboundService);
    }

    @Test
    void testProcess() {
        String fileName = "test.txt";
        allPayArchiveService.process(fileName);
        verify(sftpOutboundService).downloadFile(eq(ALL_PAY_ARCHIVE.getPath()), eq(fileName), any());
    }

    @Test
    void testProcessFile() {
        String fileContents = "Test file contents";
        InputStream inputStream = new ByteArrayInputStream(fileContents.getBytes());
        allPayArchiveService.processFile(inputStream);

    }
}
