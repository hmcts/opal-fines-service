package uk.gov.hmcts.opal.sftp;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.sftp.config.SftpConnection;

import java.io.InputStream;
import java.util.function.Consumer;

import static uk.gov.hmcts.opal.sftp.SftpLocation.getInboundLocations;

@Slf4j
@Service
@RequiredArgsConstructor
public class SftpInboundService {

    private final DefaultSftpSessionFactory inboundSessionFactory;
    private final SftpService sftpService;
    private final SftpConnection inboundConnection;

    public void uploadFile(byte[] fileBytes, String path, String fileName) {
        sftpService.uploadFile(inboundSessionFactory, fileBytes, path, fileName);
    }

    public boolean downloadFile(String path, String fileName, Consumer<InputStream> fileProcessor) {
        return sftpService.downloadFile(inboundSessionFactory, path, fileName, fileProcessor);
    }

    public boolean deleteFile(String path, String fileName) {
        return sftpService.deleteFile(inboundSessionFactory, path, fileName);
    }

    @PostConstruct
    public void createSftpLocations() {
        if (inboundConnection.isCreateSubLocations()) {
            getInboundLocations()
                .forEach(sftpLocation ->
                             this.sftpService.createDirectoryIfNotExists(inboundSessionFactory, sftpLocation)
                );
        }
    }
}
