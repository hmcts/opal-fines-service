package uk.gov.hmcts.opal.sftp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "opal.sftp")
public class SftpProperties {

    private SftpConnection inbound;
    private SftpConnection outbound;

}
