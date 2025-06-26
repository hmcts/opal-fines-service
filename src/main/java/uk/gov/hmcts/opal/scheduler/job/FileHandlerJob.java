package uk.gov.hmcts.opal.scheduler.job;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.scheduler.model.CronJob;
import uk.gov.hmcts.opal.sftp.SftpOutboundService;

import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.format;
import static java.time.LocalTime.now;

@Component
@Getter
@Slf4j(topic = "opal.FileHandlerJob")
@DisallowConcurrentExecution
public class FileHandlerJob implements CronJob {

    @Value("${opal.schedule.file-handler-job.cron}")
    private String cronExpression;

    private final SftpOutboundService sftpOutboundService;

    public FileHandlerJob(SftpOutboundService sftpOutboundService) {
        this.sftpOutboundService = sftpOutboundService;
    }

    @SneakyThrows
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        log.debug(":execute: Job ** {} ** starting @ {}",
            context.getJobDetail().getKey().getName(),
            context.getFireTime()
        );

        String fileName = format("test-file-%s.txt", now());
        this.uploadFile("My file contents here...", fileName);

        sftpOutboundService.downloadFile("", fileName, this::logInputStream);

        log.debug(":execute:Job ** {} ** completed.  Next job scheduled @ {}",
            context.getJobDetail().getKey().getName(),
            context.getNextFireTime()
        );
    }

    public void uploadFile(String contents, String fileName) {
        sftpOutboundService.uploadFile(format("%s %s", contents, now()).getBytes(), "", fileName);
    }


    public void logInputStream(InputStream inputStream) {
        try {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                // Do something with the read bytes
                String contents = new String(buffer, 0, bytesRead);
                log.debug(contents);
            }
        } catch (IOException exception) {
            log.error(exception.getMessage(), exception);
        }
    }

}
