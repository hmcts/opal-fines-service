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
    public DefaultSftpSessionFactory inboundSessionFactory() {
        DefaultSftpSessionFactory sessionFactory = new DefaultSftpSessionFactory();
        sessionFactory.setHost(sftpProperties.getInbound().getHost());
        sessionFactory.setPort(sftpProperties.getInbound().getPort());
        sessionFactory.setUser(sftpProperties.getInbound().getUser());
        sessionFactory.setPassword(sftpProperties.getInbound().getPassword());
        sessionFactory.setAllowUnknownKeys(true);

        return sessionFactory;
    }

    @Bean
    public DefaultSftpSessionFactory outboundSessionFactory() {
        DefaultSftpSessionFactory sessionFactory = new DefaultSftpSessionFactory();
        sessionFactory.setHost(sftpProperties.getOutbound().getHost());
        sessionFactory.setPort(sftpProperties.getOutbound().getPort());
        sessionFactory.setUser(sftpProperties.getOutbound().getUser());
        sessionFactory.setPassword(sftpProperties.getOutbound().getPassword());
        sessionFactory.setAllowUnknownKeys(true);

        return sessionFactory;
    }

}
