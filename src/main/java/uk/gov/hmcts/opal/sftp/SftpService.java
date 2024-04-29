package uk.gov.hmcts.opal.sftp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Consumer;

import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class SftpService {

    private final DefaultSftpSessionFactory inboundSessionFactory;
    private final DefaultSftpSessionFactory outboundSessionFactory;

    public void uploadOutboundFile(byte[] fileBytes, String path, String fileName) {
        uploadFile(outboundSessionFactory, fileBytes, path, fileName);
    }

    public void uploadFile(DefaultSftpSessionFactory sessionFactory, byte[] fileBytes, String path, String fileName) {
        var template = new RemoteFileTemplate<>(sessionFactory);
        template.execute(session -> {
            session.write(new ByteArrayInputStream(fileBytes), path + "/" + fileName);
            log.info(format("File %s uploaded successfully.", fileName));
            return true;
        });
    }

    public boolean downloadInboundFile(String path, String fileName, Consumer<InputStream> fileProcessor) {
        return downloadFile(inboundSessionFactory, path, fileName, fileProcessor);
    }

    public boolean downloadOutboundFile(String path, String fileName, Consumer<InputStream> fileProcessor) {
        return downloadFile(outboundSessionFactory, path, fileName, fileProcessor);
    }

    public boolean downloadFile(DefaultSftpSessionFactory sessionFactory,
                                String path,
                                String fileName,
                                Consumer<InputStream> fileProcessor) {
        var template = new RemoteFileTemplate<>(sessionFactory);
        return template.get(path + "/" + fileName, fileProcessor::accept);
    }

    public boolean deleteFile(DefaultSftpSessionFactory sessionFactory, String path, String fileName) {
        var template = new RemoteFileTemplate<>(sessionFactory);
        return template.execute(session -> session.remove(path + "/" + fileName));
    }

}
