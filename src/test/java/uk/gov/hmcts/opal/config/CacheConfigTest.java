package uk.gov.hmcts.opal.config;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(classes = {CacheConfig.class, CacheConfigTest.TestConfig.class})
@Isolated
class CacheConfigTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.url", () -> "localhost:6379");
        registry.add("opal.redis.ttl.hours", () -> "8");
        registry.add("opal.redis.enabled", () -> "false");
    }

    @Test
    void whenRedisEnabled_thenReturnRedisCacheManager() {
        assertTrue(cacheManager instanceof RedisCacheManager);
    }

    @Test
    void whenRedisDisabled_thenReturnSimpleCacheManager() {
        CacheConfig config = new CacheConfig();
        CacheManager simpleCacheManager = config.simpleCacheManager();
        assertTrue(simpleCacheManager instanceof ConcurrentMapCacheManager);
    }

    @Test
    void testRedisConnectionFactory() {
        assertNotNull(redisConnectionFactory);
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public RedisConnectionFactory redisConnectionFactory() {
            return mock(RedisConnectionFactory.class);
        }

        @Bean
        @Primary
        public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
            return RedisCacheManager.builder(redisConnectionFactory).build();
        }
    }

    @Test
    void redisConnectionFactory_shouldEnableSsl_whenUsingRedissUrl() throws Exception {
        CacheConfig config = new CacheConfig();
        setField(config, "redisUrl", "rediss://:password@opal-stg.redis.cache.windows.net:6380?tls=true");
        setField(config, "redisEnabled", true);
        setField(config, "redisTtlDuration", Duration.ofMinutes(5));

        LettuceConnectionFactory factory = (LettuceConnectionFactory) config.redisConnectionFactory();

        assertThat(factory.isUseSsl()).isTrue();
    }

    @Test
    void redisConnectionFactory_shouldDisableSsl_whenUsingRedisUrl() throws Exception {
        CacheConfig config = new CacheConfig();
        setField(config, "redisUrl", "redis://localhost:6379");
        setField(config, "redisEnabled", true);
        setField(config, "redisTtlDuration", Duration.ofMinutes(5));

        LettuceConnectionFactory factory = (LettuceConnectionFactory) config.redisConnectionFactory();

        assertThat(factory.isUseSsl()).isFalse();
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
