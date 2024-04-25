package uk.gov.hmcts.opal.sftp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class SftpService {

    private final DefaultSftpSessionFactory inboundSessionFactory;
    private final DefaultSftpSessionFactory outboundSessionFactory;

    public void uploadFile(byte[] fileBytes, String path, String fileName) {
        var template = new RemoteFileTemplate<>(inboundSessionFactory);
        template.execute(session -> {
            session.write(new ByteArrayInputStream(fileBytes), path + "/" + fileName);
            return null;
        });
    }

    public boolean downloadFile(String path, String fileName, Consumer<InputStream> fileProcessor) {
        var template = new RemoteFileTemplate<>(outboundSessionFactory);
        return template.get(path + "/" + fileName, fileProcessor::accept);
    }

    public boolean deleteFile(DefaultSftpSessionFactory sessionFactory, String path, String fileName) {
        var template = new RemoteFileTemplate<>(sessionFactory);
        return template.execute(session -> session.remove(path + "/" + fileName));
    }

}
