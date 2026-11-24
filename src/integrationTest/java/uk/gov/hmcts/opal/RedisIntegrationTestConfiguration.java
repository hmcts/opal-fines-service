package uk.gov.hmcts.opal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@TestConfiguration(proxyBeanMethods = false)
public class RedisIntegrationTestConfiguration {

    @Bean
    @ConditionalOnProperty(name = "opal.redis.enabled", havingValue = "false", matchIfMissing = true)
    @SuppressWarnings("unchecked")
    public StringRedisTemplate cacheMissStringRedisTemplate() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        return redisTemplate;
    }

    @Bean
    @ConditionalOnProperty(name = "opal.redis.enabled", havingValue = "true")
    public StringRedisTemplate redisStringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }
}
