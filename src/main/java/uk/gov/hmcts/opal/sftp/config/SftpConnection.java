package uk.gov.hmcts.opal.sftp.config;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SftpConnection {

    private String host;
    private int port;
    private String user;
    private String password;
    private String location;
}
