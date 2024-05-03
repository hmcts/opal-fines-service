package uk.gov.hmcts.opal.sftp;

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
public class SftpService {

    public void uploadFile(DefaultSftpSessionFactory sessionFactory, byte[] fileBytes, String path, String fileName) {
        var template = new RemoteFileTemplate<>(sessionFactory);
        template.execute(session -> {
            session.write(new ByteArrayInputStream(fileBytes), path + "/" + fileName);
            log.info(format("File %s uploaded successfully.", fileName));
            return true;
        });
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

    public void createDirectoryIfNotExists(DefaultSftpSessionFactory sessionFactory, SftpLocation location) {
        try {
            String remoteDirectory = location.getPath();
            boolean directoryExists = sessionFactory.getSession().list(remoteDirectory).length > 0;
            if (!directoryExists) {
                sessionFactory.getSession().mkdir(remoteDirectory);
                log.info(format(
                    "%s SFTP directory %s created for ",
                    location.getDirection(),
                    remoteDirectory,
                    location.getDescription()
                ));
            }
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
        }
    }

}
