package uk.gov.hmcts.opal.sftp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;

@Configuration
@RequiredArgsConstructor
public class SftpConfiguration {

    private final SftpProperties sftpProperties;

    @Bean
    public SftpConnection inboundConnection() {
        return sftpProperties.getInbound();
    }

    @Bean
    public DefaultSftpSessionFactory inboundSessionFactory() {
        DefaultSftpSessionFactory sessionFactory = new DefaultSftpSessionFactory();
        sessionFactory.setHost(inboundConnection().getHost());
        sessionFactory.setPort(inboundConnection().getPort());
        sessionFactory.setUser(inboundConnection().getUser());
        sessionFactory.setPassword(inboundConnection().getPassword());
        sessionFactory.setAllowUnknownKeys(true);

        return sessionFactory;
    }

    @Bean
    public SftpConnection outboundConnection() {
        return sftpProperties.getOutbound();
    }

    @Bean
    public DefaultSftpSessionFactory outboundSessionFactory() {
        DefaultSftpSessionFactory sessionFactory = new DefaultSftpSessionFactory();
        sessionFactory.setHost(outboundConnection().getHost());
        sessionFactory.setPort(outboundConnection().getPort());
        sessionFactory.setUser(outboundConnection().getUser());
        sessionFactory.setPassword(outboundConnection().getPassword());
        sessionFactory.setAllowUnknownKeys(true);

        return sessionFactory;
    }

}
