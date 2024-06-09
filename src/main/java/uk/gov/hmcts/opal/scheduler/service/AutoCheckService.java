package uk.gov.hmcts.opal.scheduler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.sftp.SftpInboundService;

import java.io.InputStream;

import static uk.gov.hmcts.opal.sftp.SftpLocation.AUTO_CHEQUES;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoCheckService {

    private final SftpInboundService sftpInboundService;

    public void process(String fileName) {
        sftpInboundService.downloadFile(AUTO_CHEQUES.getPath(), fileName, this::processFile);
    }

    public void processFile(InputStream inputStream) {
        log.info("Process file contents of the stream.");
        //TODO: add file processing logic here.
    }
}
